package com.example.kairos_mobile.data.local.database.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import com.example.kairos_mobile.data.local.database.KairosDatabase
import com.example.kairos_mobile.data.local.database.entities.CaptureQueueEntity
import com.example.kairos_mobile.domain.model.SyncStatus
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * CaptureQueueDao Instrumented 테스트
 *
 * 테스트 대상:
 * - 캡처 CRUD 연산
 * - 동기화 상태 관리
 * - 검색 쿼리
 * - 페이징
 */
@RunWith(AndroidJUnit4::class)
class CaptureQueueDaoTest {

    private lateinit var database: KairosDatabase
    private lateinit var captureQueueDao: CaptureQueueDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, KairosDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        captureQueueDao = database.captureQueueDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    // ==================== Insert 테스트 ====================

    @Test
    fun 캡처_삽입_및_조회() = runTest {
        // Given
        val capture = createCapture(id = "1", content = "테스트 캡처")

        // When
        captureQueueDao.insertCapture(capture)

        // Then
        val result = captureQueueDao.getCaptureById("1")
        assertNotNull(result)
        assertEquals("테스트 캡처", result?.content)
    }

    @Test
    fun 중복_ID_삽입_시_교체() = runTest {
        // Given
        val original = createCapture(id = "1", content = "원본")
        val updated = createCapture(id = "1", content = "수정됨")

        // When
        captureQueueDao.insertCapture(original)
        captureQueueDao.insertCapture(updated)

        // Then
        val result = captureQueueDao.getCaptureById("1")
        assertEquals("수정됨", result?.content)
    }

    // ==================== 상태별 조회 테스트 ====================

    @Test
    fun PENDING_상태_캡처만_조회() = runTest {
        // Given
        val pendingCapture = createCapture(id = "1", syncStatus = SyncStatus.PENDING.name)
        val syncedCapture = createCapture(id = "2", syncStatus = SyncStatus.SYNCED.name)

        captureQueueDao.insertCapture(pendingCapture)
        captureQueueDao.insertCapture(syncedCapture)

        // When & Then
        captureQueueDao.getCapturesByStatus(SyncStatus.PENDING.name).test {
            val result = awaitItem()
            assertEquals(1, result.size)
            assertEquals("1", result[0].id)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun 대기중_캡처_개수_조회() = runTest {
        // Given
        val captures = listOf(
            createCapture(id = "1", syncStatus = SyncStatus.PENDING.name),
            createCapture(id = "2", syncStatus = SyncStatus.PENDING.name),
            createCapture(id = "3", syncStatus = SyncStatus.SYNCED.name)
        )
        captures.forEach { captureQueueDao.insertCapture(it) }

        // When & Then
        captureQueueDao.getPendingCount(SyncStatus.PENDING.name).test {
            val count = awaitItem()
            assertEquals(2, count)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ==================== 업데이트 테스트 ====================

    @Test
    fun 동기화_상태_업데이트() = runTest {
        // Given
        val capture = createCapture(id = "1", syncStatus = SyncStatus.PENDING.name)
        captureQueueDao.insertCapture(capture)

        // When
        captureQueueDao.updateSyncStatus("1", SyncStatus.SYNCED.name)

        // Then
        val result = captureQueueDao.getCaptureById("1")
        assertEquals(SyncStatus.SYNCED.name, result?.syncStatus)
    }

    @Test
    fun 동기화_상태_및_에러_메시지_업데이트() = runTest {
        // Given
        val capture = createCapture(id = "1", syncStatus = SyncStatus.PENDING.name)
        captureQueueDao.insertCapture(capture)

        // When
        captureQueueDao.updateSyncStatusWithError("1", SyncStatus.FAILED.name, "네트워크 오류")

        // Then
        val result = captureQueueDao.getCaptureById("1")
        assertEquals(SyncStatus.FAILED.name, result?.syncStatus)
        assertEquals("네트워크 오류", result?.errorMessage)
    }

    @Test
    fun 재시도_카운트_증가() = runTest {
        // Given
        val capture = createCapture(id = "1")
        captureQueueDao.insertCapture(capture)
        assertEquals(0, captureQueueDao.getCaptureById("1")?.retryCount)

        // When
        val currentTime = System.currentTimeMillis()
        captureQueueDao.incrementRetryCount("1", currentTime)

        // Then
        val result = captureQueueDao.getCaptureById("1")
        assertEquals(1, result?.retryCount)
        assertEquals(currentTime, result?.lastRetryTimestamp)
    }

    // ==================== 삭제 테스트 ====================

    @Test
    fun 특정_캡처_삭제() = runTest {
        // Given
        val capture = createCapture(id = "1")
        captureQueueDao.insertCapture(capture)

        // When
        captureQueueDao.deleteCapture("1")

        // Then
        val result = captureQueueDao.getCaptureById("1")
        assertNull(result)
    }

    @Test
    fun 상태별_캡처_삭제() = runTest {
        // Given
        val captures = listOf(
            createCapture(id = "1", syncStatus = SyncStatus.SYNCED.name),
            createCapture(id = "2", syncStatus = SyncStatus.SYNCED.name),
            createCapture(id = "3", syncStatus = SyncStatus.PENDING.name)
        )
        captures.forEach { captureQueueDao.insertCapture(it) }

        // When
        captureQueueDao.deleteCapturesByStatus(SyncStatus.SYNCED.name)

        // Then
        captureQueueDao.getTotalCount().test {
            val count = awaitItem()
            assertEquals(1, count)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun 모든_캡처_삭제() = runTest {
        // Given
        val captures = listOf(
            createCapture(id = "1"),
            createCapture(id = "2"),
            createCapture(id = "3")
        )
        captures.forEach { captureQueueDao.insertCapture(it) }

        // When
        captureQueueDao.deleteAll()

        // Then
        captureQueueDao.getTotalCount().test {
            val count = awaitItem()
            assertEquals(0, count)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ==================== 최근 캡처 조회 테스트 ====================

    @Test
    fun 최근_캡처_최신순_정렬() = runTest {
        // Given
        val oldCapture = createCapture(id = "1", content = "이전", timestamp = 1000L)
        val newCapture = createCapture(id = "2", content = "최신", timestamp = 2000L)

        captureQueueDao.insertCapture(oldCapture)
        captureQueueDao.insertCapture(newCapture)

        // When & Then
        captureQueueDao.getRecentCaptures(10).test {
            val result = awaitItem()
            assertEquals(2, result.size)
            assertEquals("최신", result[0].content)
            assertEquals("이전", result[1].content)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun 최근_캡처_limit_적용() = runTest {
        // Given
        (1..10).forEach { i ->
            captureQueueDao.insertCapture(createCapture(id = "$i", timestamp = i.toLong()))
        }

        // When & Then
        captureQueueDao.getRecentCaptures(5).test {
            val result = awaitItem()
            assertEquals(5, result.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ==================== 페이징 조회 테스트 ====================

    @Test
    fun 페이징_조회() = runTest {
        // Given - 10개 캡처
        (1..10).forEach { i ->
            captureQueueDao.insertCapture(
                createCapture(id = "$i", timestamp = (11 - i).toLong())  // 역순 timestamp
            )
        }

        // When - 첫 페이지 (5개)
        captureQueueDao.getAllCapturesPaged(limit = 5, offset = 0).test {
            val firstPage = awaitItem()
            assertEquals(5, firstPage.size)
            assertEquals("1", firstPage[0].id)  // 최신 (timestamp 10)

            cancelAndIgnoreRemainingEvents()
        }

        // When - 두 번째 페이지
        captureQueueDao.getAllCapturesPaged(limit = 5, offset = 5).test {
            val secondPage = awaitItem()
            assertEquals(5, secondPage.size)
            assertEquals("6", secondPage[0].id)

            cancelAndIgnoreRemainingEvents()
        }
    }

    // ==================== 전체 개수 조회 테스트 ====================

    @Test
    fun 전체_캡처_개수() = runTest {
        // Given
        (1..7).forEach { i ->
            captureQueueDao.insertCapture(createCapture(id = "$i"))
        }

        // When & Then
        captureQueueDao.getTotalCount().test {
            val count = awaitItem()
            assertEquals(7, count)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ==================== Flow 업데이트 테스트 ====================

    @Test
    fun 캡처_추가_시_Flow_업데이트() = runTest {
        captureQueueDao.getRecentCaptures(10).test {
            // 초기 상태
            assertEquals(0, awaitItem().size)

            // 캡처 추가
            captureQueueDao.insertCapture(createCapture(id = "1"))
            assertEquals(1, awaitItem().size)

            // 추가 캡처
            captureQueueDao.insertCapture(createCapture(id = "2"))
            assertEquals(2, awaitItem().size)

            cancelAndIgnoreRemainingEvents()
        }
    }

    // ==================== 헬퍼 함수 ====================

    private fun createCapture(
        id: String,
        content: String = "테스트 캡처",
        timestamp: Long = System.currentTimeMillis(),
        syncStatus: String = SyncStatus.PENDING.name,
        classificationType: String? = null,
        source: String = "TEXT"
    ) = CaptureQueueEntity(
        id = id,
        content = content,
        timestamp = timestamp,
        syncStatus = syncStatus,
        classificationType = classificationType,
        destinationPath = null,
        title = null,
        tags = null,
        confidence = null,
        metadata = null,
        errorMessage = null,
        retryCount = 0,
        lastRetryTimestamp = null,
        source = source
    )
}
