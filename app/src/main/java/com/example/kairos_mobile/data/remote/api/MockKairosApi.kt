package com.example.kairos_mobile.data.remote.api

import com.example.kairos_mobile.data.remote.dto.v2.AnalyticsEventDto
import com.example.kairos_mobile.data.remote.dto.v2.CalendarEventRequest
import com.example.kairos_mobile.data.remote.dto.v2.CalendarEventResponse
import com.example.kairos_mobile.data.remote.dto.v2.ClassifyRequest
import com.example.kairos_mobile.data.remote.dto.v2.ClassifyResponse
import com.example.kairos_mobile.data.remote.dto.v2.EntityDto
import com.example.kairos_mobile.data.remote.dto.v2.HealthResponse
import com.example.kairos_mobile.data.remote.dto.v2.ScheduleInfoDto
import com.example.kairos_mobile.data.remote.dto.v2.SplitItemDto
import com.example.kairos_mobile.data.remote.dto.v2.TodoInfoDto
import kotlinx.coroutines.delay
import retrofit2.Response
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

/**
 * Mock KAIROS API 구현체
 * 실제 백엔드 대신 사용하는 로컬 Mock
 */
@Singleton
class MockKairosApi @Inject constructor() : KairosApi {

    /**
     * Mock AI 분류 — 키워드 기반 분류 로직
     * 멀티 인텐트 감지 시 splitItems 포함
     */
    override suspend fun classify(
        request: ClassifyRequest
    ): Response<ClassifyResponse> {
        delay(500)

        val text = request.text.lowercase()

        // 멀티 인텐트 패턴 감지 ("~하고", "~그리고")
        val splitItems = detectMultiIntent(request.text)
        if (splitItems != null) {
            // 멀티 인텐트: 첫 번째 split 기준으로 대표 분류 생성
            val firstSplit = splitItems.first()
            val response = ClassifyResponse(
                classifiedType = firstSplit.classifiedType,
                noteSubType = firstSplit.noteSubType,
                confidence = firstSplit.confidence,
                aiTitle = request.text.take(30),
                tags = extractTags(request.text),
                entities = extractEntities(text),
                scheduleInfo = firstSplit.scheduleInfo,
                todoInfo = firstSplit.todoInfo,
                splitItems = splitItems
            )
            return Response.success(response)
        }

        // 단일 의도 분류
        val (classifiedType, noteSubType, scheduleInfo, todoInfo) = classifyByKeywords(text)

        // 제목 추출 (첫 줄, 최대 30자)
        val aiTitle = request.text
            .lines()
            .firstOrNull()
            ?.take(30)
            ?: "제목 없음"

        // 태그 추출
        val tags = extractTags(request.text)

        // 엔티티 추출
        val entities = extractEntities(text)

        // 신뢰도 결정
        val confidence = when {
            text.contains("해야") || text.contains("todo") -> "HIGH"
            text.contains("회의") || text.contains("약속") -> "HIGH"
            text.length > 100 -> "MEDIUM"
            else -> "MEDIUM"
        }

        val response = ClassifyResponse(
            classifiedType = classifiedType,
            noteSubType = noteSubType,
            confidence = confidence,
            aiTitle = aiTitle,
            tags = tags,
            entities = entities,
            scheduleInfo = scheduleInfo,
            todoInfo = todoInfo
        )

        return Response.success(response)
    }

    /**
     * Mock 서버 상태 확인
     */
    override suspend fun health(): Response<HealthResponse> {
        delay(100)

        val response = HealthResponse(
            success = true,
            status = "healthy",
            version = "2.0.0",
            environment = "mock",
            timestamp = java.time.Instant.now().toString()
        )

        return Response.success(response)
    }

    /**
     * Mock 분석 이벤트 배치 전송
     */
    override suspend fun analyticsEvents(events: List<AnalyticsEventDto>): Response<Unit> {
        delay(100)
        return Response.success(Unit)
    }

    /**
     * Mock Google Calendar 이벤트 생성
     */
    override suspend fun createCalendarEvent(request: CalendarEventRequest): Response<CalendarEventResponse> {
        delay(300)
        val response = CalendarEventResponse(
            googleEventId = "mock_event_${UUID.randomUUID().toString().take(8)}",
            status = "confirmed"
        )
        return Response.success(response)
    }

    /**
     * Mock Google Calendar 이벤트 삭제
     */
    override suspend fun deleteCalendarEvent(eventId: String): Response<Unit> {
        delay(200)
        return Response.success(Unit)
    }

    // ========== Helper Methods ==========

    /**
     * 멀티 인텐트 패턴 감지
     * "~하고", "~그리고" 구분자로 텍스트를 분할하여 각 의도를 개별 분류한다.
     * 분할 결과가 2개 이상이면 splitItems 반환, 아니면 null.
     */
    private fun detectMultiIntent(originalText: String): List<SplitItemDto>? {
        // "하고 ", " 그리고 " 패턴으로 분할
        val splitPattern = "(?:하고\\s|\\s그리고\\s)".toRegex()
        val parts = originalText.split(splitPattern).map { it.trim() }.filter { it.isNotEmpty() }

        if (parts.size < 2) return null

        return parts.map { part ->
            val lowerPart = part.lowercase()
            val (type, subType, schedInfo, todoInf) = classifyByKeywords(lowerPart)
            val confidence = when {
                lowerPart.contains("해야") || lowerPart.contains("todo") -> "HIGH"
                lowerPart.contains("회의") || lowerPart.contains("약속") -> "HIGH"
                else -> "MEDIUM"
            }
            SplitItemDto(
                splitText = part,
                classifiedType = type,
                noteSubType = subType,
                confidence = confidence,
                aiTitle = part.take(30),
                tags = extractTags(part),
                entities = extractEntities(lowerPart),
                scheduleInfo = schedInfo,
                todoInfo = todoInf
            )
        }
    }

    /**
     * 키워드 기반 분류 결과 반환
     */
    private fun classifyByKeywords(
        text: String
    ): ClassifyResult {
        return when {
            // 할 일
            text.contains("해야") ||
                text.contains("하기") ||
                text.contains("todo") ||
                text.contains("마감") -> {
                val deadline = extractDeadlineMs(text)
                ClassifyResult(
                    classifiedType = "TODO",
                    noteSubType = null,
                    scheduleInfo = null,
                    todoInfo = TodoInfoDto(
                        deadline = deadline,
                        deadlineSource = if (deadline != null) "AI" else null
                    )
                )
            }

            // 일정
            text.contains("회의") ||
                text.contains("약속") ||
                text.contains("미팅") ||
                text.contains("schedule") -> {
                val startTime = extractScheduleTimeMs(text)
                ClassifyResult(
                    classifiedType = "SCHEDULE",
                    noteSubType = null,
                    scheduleInfo = ScheduleInfoDto(
                        startTime = startTime,
                        endTime = startTime + 3600_000L,
                        location = extractLocation(text),
                        isAllDay = false
                    ),
                    todoInfo = null
                )
            }

            // 아이디어
            text.contains("아이디어") ||
                text.contains("생각") ||
                text.contains("idea") ||
                text.startsWith("만약") -> {
                ClassifyResult(
                    classifiedType = "NOTES",
                    noteSubType = "IDEA",
                    scheduleInfo = null,
                    todoInfo = null
                )
            }

            // URL → 북마크
            text.contains("http://") ||
                text.contains("https://") -> {
                ClassifyResult(
                    classifiedType = "NOTES",
                    noteSubType = "BOOKMARK",
                    scheduleInfo = null,
                    todoInfo = null
                )
            }

            // 노트 (일반 메모)
            else -> {
                ClassifyResult(
                    classifiedType = "NOTES",
                    noteSubType = "INBOX",
                    scheduleInfo = null,
                    todoInfo = null
                )
            }
        }
    }

    /**
     * 텍스트에서 해시태그 및 키워드 기반 태그 추출
     */
    private fun extractTags(content: String): List<String> {
        val tags = mutableListOf<String>()

        // # 태그 추출
        val hashTagRegex = "#([가-힣a-zA-Z0-9_]+)".toRegex()
        hashTagRegex.findAll(content).forEach {
            tags.add(it.groupValues[1])
        }

        // 키워드 기반 자동 태그
        val lower = content.lowercase()
        if (lower.contains("프로젝트")) tags.add("project")
        if (lower.contains("업무")) tags.add("work")
        if (lower.contains("학습") || lower.contains("공부")) tags.add("study")

        return tags.distinct().take(5)
    }

    /**
     * 텍스트에서 엔티티 추출
     */
    private fun extractEntities(text: String): List<EntityDto> {
        val entities = mutableListOf<EntityDto>()

        // 날짜 엔티티 추출
        if (text.contains("내일") || text.contains("오늘") || text.contains("모레")) {
            entities.add(
                EntityDto(
                    type = "DATE",
                    value = when {
                        text.contains("오늘") -> "오늘"
                        text.contains("내일") -> "내일"
                        else -> "모레"
                    },
                    normalizedValue = LocalDate.now().let { today ->
                        when {
                            text.contains("오늘") -> today.toString()
                            text.contains("내일") -> today.plusDays(1).toString()
                            else -> today.plusDays(2).toString()
                        }
                    }
                )
            )
        }

        // 장소 엔티티 추출
        val locationKeywords = listOf("카페", "사무실", "회의실", "학교")
        for (keyword in locationKeywords) {
            if (text.contains(keyword)) {
                entities.add(
                    EntityDto(
                        type = "LOCATION",
                        value = keyword,
                        normalizedValue = null
                    )
                )
                break
            }
        }

        return entities
    }

    /**
     * 텍스트에서 마감일 epoch ms 추출
     */
    private fun extractDeadlineMs(text: String): Long? {
        val today = LocalDate.now()
        val date = when {
            text.contains("오늘") -> today
            text.contains("내일") -> today.plusDays(1)
            text.contains("모레") -> today.plusDays(2)
            text.contains("이번 주") -> today.plusDays((7 - today.dayOfWeek.value).toLong())
            text.contains("다음 주") -> today.plusWeeks(1)
            else -> return null
        }
        return date.atTime(23, 59, 59)
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
    }

    /**
     * 텍스트에서 일정 시작 시각 epoch ms 추출
     */
    private fun extractScheduleTimeMs(text: String): Long {
        val today = LocalDate.now()
        val date = when {
            text.contains("내일") -> today.plusDays(1)
            text.contains("모레") -> today.plusDays(2)
            else -> today
        }

        // 시간 추출 (기본 오후 2시)
        val time = when {
            text.contains("오전") -> LocalTime.of(10, 0)
            text.contains("오후") -> LocalTime.of(14, 0)
            text.contains("저녁") -> LocalTime.of(18, 0)
            else -> LocalTime.of(14, 0)
        }

        return date.atTime(time)
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
    }

    /**
     * 텍스트에서 장소 추출
     */
    private fun extractLocation(text: String): String? {
        val locationKeywords = listOf("카페", "사무실", "회의실", "학교", "도서관")
        for (keyword in locationKeywords) {
            if (text.contains(keyword)) return keyword
        }
        return null
    }

    /**
     * 분류 결과 내부 클래스
     */
    private data class ClassifyResult(
        val classifiedType: String,
        val noteSubType: String?,
        val scheduleInfo: ScheduleInfoDto?,
        val todoInfo: TodoInfoDto?
    )
}
