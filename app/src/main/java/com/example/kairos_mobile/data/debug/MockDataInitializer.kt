package com.example.kairos_mobile.data.debug

import android.util.Log
import com.example.kairos_mobile.data.local.database.dao.CaptureDao
import com.example.kairos_mobile.data.local.database.dao.NoteDao
import com.example.kairos_mobile.data.local.database.dao.ScheduleDao
import com.example.kairos_mobile.data.local.database.dao.TodoDao
import com.example.kairos_mobile.data.local.database.entities.CaptureEntity
import com.example.kairos_mobile.data.local.database.entities.NoteEntity
import com.example.kairos_mobile.data.local.database.entities.ScheduleEntity
import com.example.kairos_mobile.data.local.database.entities.TodoEntity
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.ZoneId
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Mock 데이터 초기화 클래스 (테스트 용도)
 * 앱 시작 시 데이터가 비어있으면 샘플 데이터 삽입
 */
@Singleton
class MockDataInitializer @Inject constructor(
    private val captureDao: CaptureDao,
    private val scheduleDao: ScheduleDao,
    private val todoDao: TodoDao,
    private val noteDao: NoteDao
) {
    companion object {
        private const val TAG = "MockDataInitializer"
    }

    /**
     * Mock 데이터 초기화
     * 각 테이블이 비어있을 때만 데이터 삽입
     */
    suspend fun initializeMockData() {
        Log.d(TAG, "Mock 데이터 초기화 시작")

        val existingCaptureCount = captureDao.getActiveCount().first()
        if (existingCaptureCount > 0) {
            Log.d(TAG, "기존 데이터가 존재하여 Mock 데이터 삽입을 건너뜁니다. (captures=$existingCaptureCount)")
            return
        }

        seedMockCapturesAndDerivedEntities()

        Log.d(TAG, "Mock 데이터 초기화 완료")
    }

    private suspend fun seedMockCapturesAndDerivedEntities() {
        val now = System.currentTimeMillis()
        val zone = ZoneId.systemDefault()
        val today = LocalDate.now()

        val scheduleCaptureId = UUID.randomUUID().toString()
        val todoCaptureId = UUID.randomUUID().toString()
        val noteCaptureId = UUID.randomUUID().toString()

        val captures = listOf(
            CaptureEntity(
                id = scheduleCaptureId,
                originalText = "내일 오전 10시에 제품 회의실에서 주간 회의",
                aiTitle = "주간 제품 회의",
                classifiedType = "SCHEDULE",
                confidence = "HIGH",
                source = "APP",
                isConfirmed = false,
                createdAt = now - 10_000,
                updatedAt = now - 10_000,
                classificationCompletedAt = now - 10_000
            ),
            CaptureEntity(
                id = todoCaptureId,
                originalText = "오늘 저녁까지 배포 체크리스트 정리하기",
                aiTitle = "배포 체크리스트 정리",
                classifiedType = "TODO",
                confidence = "MEDIUM",
                source = "APP",
                isConfirmed = false,
                createdAt = now - 8_000,
                updatedAt = now - 8_000,
                classificationCompletedAt = now - 8_000
            ),
            CaptureEntity(
                id = noteCaptureId,
                originalText = "신규 온보딩 개선 아이디어 메모",
                aiTitle = "온보딩 개선 메모",
                classifiedType = "NOTES",
                noteSubType = "INBOX",
                confidence = "MEDIUM",
                source = "APP",
                isConfirmed = false,
                createdAt = now - 6_000,
                updatedAt = now - 6_000,
                classificationCompletedAt = now - 6_000
            )
        )

        captures.forEach { captureDao.insert(it) }

        val scheduleStart = today.plusDays(1)
            .atTime(10, 0)
            .atZone(zone)
            .toInstant()
            .toEpochMilli()
        val scheduleEnd = today.plusDays(1)
            .atTime(11, 0)
            .atZone(zone)
            .toInstant()
            .toEpochMilli()

        scheduleDao.insert(
            ScheduleEntity(
                id = UUID.randomUUID().toString(),
                captureId = scheduleCaptureId,
                startTime = scheduleStart,
                endTime = scheduleEnd,
                location = "제품 회의실",
                isAllDay = false,
                confidence = "HIGH",
                createdAt = now - 10_000,
                updatedAt = now - 10_000
            )
        )

        val todoDeadline = today
            .atTime(23, 59)
            .atZone(zone)
            .toInstant()
            .toEpochMilli()

        todoDao.insert(
            TodoEntity(
                id = UUID.randomUUID().toString(),
                captureId = todoCaptureId,
                deadline = todoDeadline,
                isCompleted = false,
                completedAt = null,
                sortOrder = 0,
                createdAt = now - 8_000,
                updatedAt = now - 8_000
            )
        )

        noteDao.insert(
            NoteEntity(
                id = UUID.randomUUID().toString(),
                captureId = noteCaptureId,
                folderId = "system-inbox",
                createdAt = now - 6_000,
                updatedAt = now - 6_000
            )
        )

        Log.d(TAG, "Mock 데이터 삽입 완료: captures=3, schedules=1, todos=1, notes=1")
    }
}
