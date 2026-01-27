package com.example.kairos_mobile.data.local.database.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import com.example.kairos_mobile.data.local.database.KairosDatabase
import com.example.kairos_mobile.data.local.database.entities.NotificationEntity
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * NotificationDao Instrumented 테스트
 *
 * 테스트 대상:
 * - 알림 CRUD 연산
 * - 읽음 상태 관리
 * - 쿼리 동작
 */
@RunWith(AndroidJUnit4::class)
class NotificationDaoTest {

    private lateinit var database: KairosDatabase
    private lateinit var notificationDao: NotificationDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, KairosDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        notificationDao = database.notificationDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    // ==================== Insert 테스트 ====================

    @Test
    fun 알림_삽입_및_조회() = runTest {
        // Given
        val notification = createNotification(id = "1", title = "테스트 알림")

        // When
        notificationDao.insertNotification(notification)

        // Then
        val result = notificationDao.getNotificationById("1")
        assertNotNull(result)
        assertEquals("테스트 알림", result?.title)
    }

    @Test
    fun 여러_알림_삽입() = runTest {
        // Given
        val notifications = listOf(
            createNotification(id = "1", title = "알림 1"),
            createNotification(id = "2", title = "알림 2"),
            createNotification(id = "3", title = "알림 3")
        )

        // When
        notificationDao.insertNotifications(notifications)

        // Then
        notificationDao.getAllNotifications().test {
            val result = awaitItem()
            assertEquals(3, result.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun 중복_ID_삽입_시_교체() = runTest {
        // Given
        val original = createNotification(id = "1", title = "원본")
        val updated = createNotification(id = "1", title = "수정됨")

        // When
        notificationDao.insertNotification(original)
        notificationDao.insertNotification(updated)

        // Then
        val result = notificationDao.getNotificationById("1")
        assertEquals("수정됨", result?.title)
    }

    // ==================== 조회 테스트 ====================

    @Test
    fun 모든_알림_최신순_정렬() = runTest {
        // Given
        val oldNotification = createNotification(id = "1", title = "이전", timestamp = 1000L)
        val newNotification = createNotification(id = "2", title = "최신", timestamp = 2000L)

        notificationDao.insertNotifications(listOf(oldNotification, newNotification))

        // When & Then
        notificationDao.getAllNotifications().test {
            val result = awaitItem()
            assertEquals(2, result.size)
            assertEquals("최신", result[0].title)  // 최신이 먼저
            assertEquals("이전", result[1].title)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun 읽지_않은_알림만_조회() = runTest {
        // Given
        val unreadNotification = createNotification(id = "1", title = "읽지 않음", isRead = false)
        val readNotification = createNotification(id = "2", title = "읽음", isRead = true)

        notificationDao.insertNotifications(listOf(unreadNotification, readNotification))

        // When & Then
        notificationDao.getUnreadNotifications().test {
            val result = awaitItem()
            assertEquals(1, result.size)
            assertEquals("읽지 않음", result[0].title)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun 읽지_않은_알림_개수_조회() = runTest {
        // Given
        val notifications = listOf(
            createNotification(id = "1", isRead = false),
            createNotification(id = "2", isRead = false),
            createNotification(id = "3", isRead = true)
        )
        notificationDao.insertNotifications(notifications)

        // When & Then
        notificationDao.getUnreadCount().test {
            val count = awaitItem()
            assertEquals(2, count)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun 존재하지_않는_알림_조회_시_null() = runTest {
        // When
        val result = notificationDao.getNotificationById("non-existent")

        // Then
        assertNull(result)
    }

    // ==================== 읽음 처리 테스트 ====================

    @Test
    fun 특정_알림_읽음_처리() = runTest {
        // Given
        val notification = createNotification(id = "1", isRead = false)
        notificationDao.insertNotification(notification)

        // When
        notificationDao.markAsRead("1")

        // Then
        val result = notificationDao.getNotificationById("1")
        assertTrue(result?.isRead == true)
    }

    @Test
    fun 모든_알림_읽음_처리() = runTest {
        // Given
        val notifications = listOf(
            createNotification(id = "1", isRead = false),
            createNotification(id = "2", isRead = false),
            createNotification(id = "3", isRead = false)
        )
        notificationDao.insertNotifications(notifications)

        // When
        notificationDao.markAllAsRead()

        // Then
        notificationDao.getUnreadCount().test {
            val count = awaitItem()
            assertEquals(0, count)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ==================== 삭제 테스트 ====================

    @Test
    fun 특정_알림_삭제() = runTest {
        // Given
        val notification = createNotification(id = "1")
        notificationDao.insertNotification(notification)

        // When
        notificationDao.deleteNotification("1")

        // Then
        val result = notificationDao.getNotificationById("1")
        assertNull(result)
    }

    @Test
    fun 읽은_알림_모두_삭제() = runTest {
        // Given
        val notifications = listOf(
            createNotification(id = "1", isRead = true),
            createNotification(id = "2", isRead = true),
            createNotification(id = "3", isRead = false)
        )
        notificationDao.insertNotifications(notifications)

        // When
        notificationDao.deleteReadNotifications()

        // Then
        notificationDao.getAllNotifications().test {
            val result = awaitItem()
            assertEquals(1, result.size)
            assertEquals("3", result[0].id)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun 오래된_알림_삭제() = runTest {
        // Given
        val currentTime = System.currentTimeMillis()
        val oldTime = currentTime - (31L * 24 * 60 * 60 * 1000)  // 31일 전
        val recentTime = currentTime - (1L * 24 * 60 * 60 * 1000)  // 1일 전

        val notifications = listOf(
            createNotification(id = "1", timestamp = oldTime),  // 오래됨
            createNotification(id = "2", timestamp = recentTime)  // 최근
        )
        notificationDao.insertNotifications(notifications)

        val cutoffTime = currentTime - (30L * 24 * 60 * 60 * 1000)  // 30일 전

        // When
        notificationDao.deleteOldNotifications(cutoffTime)

        // Then
        notificationDao.getAllNotifications().test {
            val result = awaitItem()
            assertEquals(1, result.size)
            assertEquals("2", result[0].id)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun 모든_알림_삭제() = runTest {
        // Given
        val notifications = listOf(
            createNotification(id = "1"),
            createNotification(id = "2"),
            createNotification(id = "3")
        )
        notificationDao.insertNotifications(notifications)

        // When
        notificationDao.deleteAll()

        // Then
        notificationDao.getAllNotifications().test {
            val result = awaitItem()
            assertTrue(result.isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ==================== Flow 업데이트 테스트 ====================

    @Test
    fun 알림_추가_시_Flow_업데이트() = runTest {
        notificationDao.getAllNotifications().test {
            // 초기 상태 (빈 리스트)
            assertEquals(0, awaitItem().size)

            // 알림 추가
            notificationDao.insertNotification(createNotification(id = "1"))
            assertEquals(1, awaitItem().size)

            // 추가 알림
            notificationDao.insertNotification(createNotification(id = "2"))
            assertEquals(2, awaitItem().size)

            cancelAndIgnoreRemainingEvents()
        }
    }

    // ==================== 헬퍼 함수 ====================

    private fun createNotification(
        id: String,
        title: String = "테스트 알림",
        message: String = "테스트 메시지",
        timestamp: Long = System.currentTimeMillis(),
        isRead: Boolean = false,
        type: String = "INFO"
    ) = NotificationEntity(
        id = id,
        title = title,
        message = message,
        timestamp = timestamp,
        isRead = isRead,
        relatedCaptureId = null,
        type = type
    )
}
