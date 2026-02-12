package com.flit.app.data.debug

import android.util.Log
import com.flit.app.data.local.database.dao.CaptureDao
import com.flit.app.data.local.database.dao.CaptureSearchDao
import com.flit.app.data.local.database.dao.NoteDao
import com.flit.app.data.local.database.dao.ScheduleDao
import com.flit.app.data.local.database.dao.TodoDao
import com.flit.app.data.local.database.entities.CaptureEntity
import com.flit.app.data.local.database.entities.CaptureSearchFts
import com.flit.app.data.local.database.entities.NoteEntity
import com.flit.app.data.local.database.entities.ScheduleEntity
import com.flit.app.data.local.database.entities.TodoEntity
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
    private val captureSearchDao: CaptureSearchDao,
    private val scheduleDao: ScheduleDao,
    private val todoDao: TodoDao,
    private val noteDao: NoteDao
) {
    companion object {
        private const val TAG = "MockDataInitializer"
        private const val BENCHMARK_NOTE_COUNT = 120
        private const val SYSTEM_IDEAS_FOLDER_ID = "system-ideas"
        private const val BENCHMARK_CAPTURE_PREFIX = "benchmark-capture"
        private const val BENCHMARK_NOTE_PREFIX = "benchmark-note"
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

    /**
     * Benchmark 전용 데이터 초기화
     * Notes/Search 스크롤 벤치에서 충분한 리스트 길이를 보장한다.
     */
    suspend fun initializeBenchmarkData() {
        val currentIdeasNoteCount = noteDao.getNoteCountByFolder(SYSTEM_IDEAS_FOLDER_ID).first()
        if (currentIdeasNoteCount >= BENCHMARK_NOTE_COUNT) {
            Log.d(TAG, "Benchmark 데이터가 이미 충분합니다. (ideasNotes=$currentIdeasNoteCount)")
            return
        }

        if (captureDao.getActiveCount().first() == 0) {
            seedMockCapturesAndDerivedEntities()
        }

        val targetInsertCount = BENCHMARK_NOTE_COUNT - currentIdeasNoteCount
        val now = System.currentTimeMillis()

        repeat(targetInsertCount) { index ->
            val sequence = currentIdeasNoteCount + index + 1
            val captureId = "$BENCHMARK_CAPTURE_PREFIX-$sequence"
            val noteId = "$BENCHMARK_NOTE_PREFIX-$sequence"
            val timestamp = now - (sequence * 1_000L)

            val capture = CaptureEntity(
                id = captureId,
                originalText = "벤치 검색 성능 측정용 샘플 노트 $sequence. 스크롤과 검색 지표 수집을 위한 데이터입니다.",
                aiTitle = "벤치 노트 $sequence",
                classifiedType = "NOTES",
                noteSubType = "IDEA",
                confidence = "HIGH",
                source = "APP",
                isConfirmed = true,
                createdAt = timestamp,
                updatedAt = timestamp,
                classificationCompletedAt = timestamp
            )

            insertCaptureWithSearchIndex(capture)
            noteDao.insert(
                NoteEntity(
                    id = noteId,
                    captureId = captureId,
                    folderId = SYSTEM_IDEAS_FOLDER_ID,
                    createdAt = timestamp,
                    updatedAt = timestamp
                )
            )
        }

        Log.d(TAG, "Benchmark 데이터 삽입 완료: ideasNotes +$targetInsertCount")
    }

    private suspend fun seedMockCapturesAndDerivedEntities() {
        val now = System.currentTimeMillis()
        val zone = ZoneId.systemDefault()
        val today = LocalDate.now()

        // 일정 캡처 3건
        val scheduleCaptureId1 = UUID.randomUUID().toString()
        val scheduleCaptureId2 = UUID.randomUUID().toString()
        val scheduleCaptureId3 = UUID.randomUUID().toString()

        // 할일 캡처 3건
        val todoCaptureId1 = UUID.randomUUID().toString()
        val todoCaptureId2 = UUID.randomUUID().toString()
        val todoCaptureId3 = UUID.randomUUID().toString()

        // 노트 캡처 4건
        val noteInboxCaptureId1 = UUID.randomUUID().toString()
        val noteInboxCaptureId2 = UUID.randomUUID().toString()
        val noteIdeaCaptureId1 = UUID.randomUUID().toString()
        val noteIdeaCaptureId2 = UUID.randomUUID().toString()

        val captures = listOf(
            // ── 일정 3건 ──
            CaptureEntity(
                id = scheduleCaptureId1,
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
                id = scheduleCaptureId2,
                originalText = "오늘 오후 1시에 팀 점심 식사 강남역 근처",
                aiTitle = "팀 점심 식사",
                classifiedType = "SCHEDULE",
                confidence = "HIGH",
                source = "APP",
                isConfirmed = true,
                createdAt = now - 9_000,
                updatedAt = now - 9_000,
                classificationCompletedAt = now - 9_000
            ),
            CaptureEntity(
                id = scheduleCaptureId3,
                originalText = "이번 주 금요일 종일 팀 워크숍 진행",
                aiTitle = "팀 워크숍",
                classifiedType = "SCHEDULE",
                confidence = "MEDIUM",
                source = "APP",
                isConfirmed = true,
                createdAt = now - 7_000,
                updatedAt = now - 7_000,
                classificationCompletedAt = now - 7_000
            ),
            // ── 할일 3건 ──
            CaptureEntity(
                id = todoCaptureId1,
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
                id = todoCaptureId2,
                originalText = "디자인 시스템 컴포넌트 리뷰 요청",
                aiTitle = "디자인 시스템 리뷰",
                classifiedType = "TODO",
                confidence = "LOW",
                source = "APP",
                isConfirmed = true,
                createdAt = now - 5_000,
                updatedAt = now - 5_000,
                classificationCompletedAt = now - 5_000
            ),
            CaptureEntity(
                id = todoCaptureId3,
                originalText = "API 문서 업데이트 완료함",
                aiTitle = "API 문서 업데이트",
                classifiedType = "TODO",
                confidence = "HIGH",
                source = "APP",
                isConfirmed = true,
                createdAt = now - 20_000,
                updatedAt = now - 4_000,
                classificationCompletedAt = now - 20_000
            ),
            // ── 노트 4건 ──
            CaptureEntity(
                id = noteInboxCaptureId1,
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
            ),
            CaptureEntity(
                id = noteInboxCaptureId2,
                originalText = "프로젝트 회고 때 공유할 개선점 정리",
                aiTitle = "프로젝트 회고 개선점",
                classifiedType = "NOTES",
                noteSubType = "INBOX",
                confidence = "MEDIUM",
                source = "APP",
                isConfirmed = true,
                createdAt = now - 3_000,
                updatedAt = now - 3_000,
                classificationCompletedAt = now - 3_000
            ),
            CaptureEntity(
                id = noteIdeaCaptureId1,
                originalText = "캡처 화면에서 음성 입력 지원하면 어떨까",
                aiTitle = "음성 입력 아이디어",
                classifiedType = "NOTES",
                noteSubType = "IDEA",
                confidence = "MEDIUM",
                source = "APP",
                isConfirmed = true,
                createdAt = now - 2_000,
                updatedAt = now - 2_000,
                classificationCompletedAt = now - 2_000
            ),
            CaptureEntity(
                id = noteIdeaCaptureId2,
                originalText = "위젯에서 최근 할일 3개 바로 보여주는 기능",
                aiTitle = "위젯 할일 미리보기",
                classifiedType = "NOTES",
                noteSubType = "IDEA",
                confidence = "HIGH",
                source = "WIDGET",
                isConfirmed = true,
                createdAt = now - 1_000,
                updatedAt = now - 1_000,
                classificationCompletedAt = now - 1_000
            )
        )

        captures.forEach { insertCaptureWithSearchIndex(it) }

        // ── 일정 엔티티 3건 ──
        val scheduleStart1 = today.plusDays(1).atTime(10, 0).atZone(zone).toInstant().toEpochMilli()
        val scheduleEnd1 = today.plusDays(1).atTime(11, 0).atZone(zone).toInstant().toEpochMilli()
        scheduleDao.insert(
            ScheduleEntity(
                id = UUID.randomUUID().toString(),
                captureId = scheduleCaptureId1,
                startTime = scheduleStart1,
                endTime = scheduleEnd1,
                location = "제품 회의실",
                isAllDay = false,
                confidence = "HIGH",
                createdAt = now - 10_000,
                updatedAt = now - 10_000
            )
        )

        val scheduleStart2 = today.atTime(13, 0).atZone(zone).toInstant().toEpochMilli()
        val scheduleEnd2 = today.atTime(14, 0).atZone(zone).toInstant().toEpochMilli()
        scheduleDao.insert(
            ScheduleEntity(
                id = UUID.randomUUID().toString(),
                captureId = scheduleCaptureId2,
                startTime = scheduleStart2,
                endTime = scheduleEnd2,
                location = "강남역 근처",
                isAllDay = false,
                confidence = "HIGH",
                createdAt = now - 9_000,
                updatedAt = now - 9_000
            )
        )

        val friday = today.plusDays((5 - today.dayOfWeek.value.toLong() + 7) % 7)
        val scheduleStart3 = friday.atStartOfDay(zone).toInstant().toEpochMilli()
        scheduleDao.insert(
            ScheduleEntity(
                id = UUID.randomUUID().toString(),
                captureId = scheduleCaptureId3,
                startTime = scheduleStart3,
                endTime = null,
                location = null,
                isAllDay = true,
                confidence = "MEDIUM",
                createdAt = now - 7_000,
                updatedAt = now - 7_000
            )
        )

        // ── 할일 엔티티 3건 ──
        val todoDeadline = today.atTime(23, 59).atZone(zone).toInstant().toEpochMilli()
        todoDao.insert(
            TodoEntity(
                id = UUID.randomUUID().toString(),
                captureId = todoCaptureId1,
                deadline = todoDeadline,
                isCompleted = false,
                completedAt = null,
                sortOrder = 0,
                createdAt = now - 8_000,
                updatedAt = now - 8_000
            )
        )

        // 마감 없는 할일
        todoDao.insert(
            TodoEntity(
                id = UUID.randomUUID().toString(),
                captureId = todoCaptureId2,
                deadline = null,
                isCompleted = false,
                completedAt = null,
                sortOrder = 1,
                createdAt = now - 5_000,
                updatedAt = now - 5_000
            )
        )

        // 완료된 할일
        todoDao.insert(
            TodoEntity(
                id = UUID.randomUUID().toString(),
                captureId = todoCaptureId3,
                deadline = today.minusDays(1).atTime(18, 0).atZone(zone).toInstant().toEpochMilli(),
                isCompleted = true,
                completedAt = now - 4_000,
                sortOrder = 2,
                createdAt = now - 20_000,
                updatedAt = now - 4_000
            )
        )

        // ── 노트 엔티티 4건 ──
        noteDao.insert(
            NoteEntity(
                id = UUID.randomUUID().toString(),
                captureId = noteInboxCaptureId1,
                folderId = "system-inbox",
                createdAt = now - 6_000,
                updatedAt = now - 6_000
            )
        )

        noteDao.insert(
            NoteEntity(
                id = UUID.randomUUID().toString(),
                captureId = noteInboxCaptureId2,
                folderId = "system-inbox",
                createdAt = now - 3_000,
                updatedAt = now - 3_000
            )
        )

        noteDao.insert(
            NoteEntity(
                id = UUID.randomUUID().toString(),
                captureId = noteIdeaCaptureId1,
                folderId = SYSTEM_IDEAS_FOLDER_ID,
                createdAt = now - 2_000,
                updatedAt = now - 2_000
            )
        )

        noteDao.insert(
            NoteEntity(
                id = UUID.randomUUID().toString(),
                captureId = noteIdeaCaptureId2,
                folderId = SYSTEM_IDEAS_FOLDER_ID,
                createdAt = now - 1_000,
                updatedAt = now - 1_000
            )
        )

        Log.d(TAG, "Mock 데이터 삽입 완료: captures=10, schedules=3, todos=3, notes=4")
    }

    private suspend fun insertCaptureWithSearchIndex(capture: CaptureEntity) {
        captureDao.insert(capture)
        captureSearchDao.insert(
            CaptureSearchFts(
                captureId = capture.id,
                titleText = capture.aiTitle ?: "",
                originalText = capture.originalText,
                tagText = "",
                entityText = ""
            )
        )
    }
}
