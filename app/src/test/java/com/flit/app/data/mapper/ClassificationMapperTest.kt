package com.flit.app.data.mapper

import com.flit.app.data.remote.dto.v2.ClassifyResponse
import com.flit.app.data.remote.dto.v2.EntityDto
import com.flit.app.data.remote.dto.v2.ScheduleInfoDto
import com.flit.app.data.remote.dto.v2.SplitItemDto
import com.flit.app.data.remote.dto.v2.TodoInfoDto
import com.flit.app.domain.model.ClassifiedType
import com.flit.app.domain.model.ConfidenceLevel
import com.flit.app.domain.model.DeadlineSource
import com.flit.app.domain.model.EntityType
import com.flit.app.domain.model.NoteSubType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * ClassificationMapper 유닛 테스트
 * 서버 API 응답(DTO)→도메인 모델 변환 검증
 */
class ClassificationMapperTest {

    private val mapper = ClassificationMapper()

    // === classified_type 파싱 ===

    @Test
    fun `SCHEDULE 타입 정상 매핑`() {
        val response = makeResponse(classifiedType = "SCHEDULE")
        val result = mapper.toDomain(response)
        assertEquals(ClassifiedType.SCHEDULE, result.type)
    }

    @Test
    fun `TODO 타입 정상 매핑`() {
        val response = makeResponse(classifiedType = "TODO")
        val result = mapper.toDomain(response)
        assertEquals(ClassifiedType.TODO, result.type)
    }

    @Test
    fun `NOTES 타입 정상 매핑`() {
        val response = makeResponse(classifiedType = "NOTES")
        val result = mapper.toDomain(response)
        assertEquals(ClassifiedType.NOTES, result.type)
    }

    @Test
    fun `TEMP 타입 정상 매핑`() {
        val response = makeResponse(classifiedType = "TEMP")
        val result = mapper.toDomain(response)
        assertEquals(ClassifiedType.TEMP, result.type)
    }

    @Test
    fun `알 수 없는 타입은 TEMP로 폴백`() {
        val response = makeResponse(classifiedType = "UNKNOWN_TYPE")
        val result = mapper.toDomain(response)
        assertEquals(ClassifiedType.TEMP, result.type)
    }

    // === note_sub_type 파싱 ===

    @Test
    fun `INBOX 서브타입 매핑`() {
        val response = makeResponse(classifiedType = "NOTES", noteSubType = "INBOX")
        val result = mapper.toDomain(response)
        assertEquals(NoteSubType.INBOX, result.subType)
    }

    @Test
    fun `IDEA 서브타입 매핑`() {
        val response = makeResponse(classifiedType = "NOTES", noteSubType = "IDEA")
        val result = mapper.toDomain(response)
        assertEquals(NoteSubType.IDEA, result.subType)
    }

    @Test
    fun `BOOKMARK 서브타입 매핑`() {
        val response = makeResponse(classifiedType = "NOTES", noteSubType = "BOOKMARK")
        val result = mapper.toDomain(response)
        assertEquals(NoteSubType.BOOKMARK, result.subType)
    }

    @Test
    fun `note_sub_type null이면 subType도 null`() {
        val response = makeResponse(classifiedType = "SCHEDULE", noteSubType = null)
        val result = mapper.toDomain(response)
        assertNull(result.subType)
    }

    @Test
    fun `알 수 없는 서브타입은 INBOX로 폴백`() {
        val response = makeResponse(classifiedType = "NOTES", noteSubType = "UNKNOWN")
        val result = mapper.toDomain(response)
        assertEquals(NoteSubType.INBOX, result.subType)
    }

    // === confidence 파싱 ===

    @Test
    fun `HIGH 신뢰도 매핑`() {
        val response = makeResponse(confidence = "HIGH")
        val result = mapper.toDomain(response)
        assertEquals(ConfidenceLevel.HIGH, result.confidence)
    }

    @Test
    fun `LOW 신뢰도 매핑`() {
        val response = makeResponse(confidence = "LOW")
        val result = mapper.toDomain(response)
        assertEquals(ConfidenceLevel.LOW, result.confidence)
    }

    @Test
    fun `알 수 없는 신뢰도는 MEDIUM으로 폴백`() {
        val response = makeResponse(confidence = "VERY_HIGH")
        val result = mapper.toDomain(response)
        assertEquals(ConfidenceLevel.MEDIUM, result.confidence)
    }

    // === 기본 필드 ===

    @Test
    fun `ai_title과 tags 정상 매핑`() {
        val response = makeResponse(
            aiTitle = "금요일 저녁 약속",
            tags = listOf("약속", "저녁")
        )
        val result = mapper.toDomain(response)
        assertEquals("금요일 저녁 약속", result.aiTitle)
        assertEquals(listOf("약속", "저녁"), result.tags)
    }

    // === entities 파싱 ===

    @Test
    fun `엔티티 타입 매핑 - PERSON, PLACE, DATE, TIME, AMOUNT, OTHER`() {
        val entities = listOf(
            EntityDto("PERSON", "홍길동"),
            EntityDto("PLACE", "강남역", "강남역"),
            EntityDto("DATE", "금요일", "2026-02-13"),
            EntityDto("TIME", "19시", "19:00"),
            EntityDto("AMOUNT", "5만원", "50000"),
            EntityDto("OTHER", "기타")
        )
        val response = makeResponse(entities = entities)
        val result = mapper.toDomain(response)

        assertEquals(6, result.entities.size)
        assertEquals(EntityType.PERSON, result.entities[0].type)
        assertEquals(EntityType.PLACE, result.entities[1].type)
        assertEquals(EntityType.DATE, result.entities[2].type)
        assertEquals(EntityType.TIME, result.entities[3].type)
        assertEquals(EntityType.AMOUNT, result.entities[4].type)
        assertEquals(EntityType.OTHER, result.entities[5].type)
    }

    @Test
    fun `LOCATION 엔티티는 PLACE로 매핑`() {
        val entities = listOf(EntityDto("LOCATION", "서울역"))
        val response = makeResponse(entities = entities)
        val result = mapper.toDomain(response)
        assertEquals(EntityType.PLACE, result.entities[0].type)
    }

    @Test
    fun `알 수 없는 엔티티 타입은 OTHER로 폴백`() {
        val entities = listOf(EntityDto("CUSTOM_TYPE", "value"))
        val response = makeResponse(entities = entities)
        val result = mapper.toDomain(response)
        assertEquals(EntityType.OTHER, result.entities[0].type)
    }

    @Test
    fun `엔티티 normalized_value가 null이면 null 유지`() {
        val entities = listOf(EntityDto("PERSON", "누군가", null))
        val response = makeResponse(entities = entities)
        val result = mapper.toDomain(response)
        assertNull(result.entities[0].normalizedValue)
    }

    // === schedule_info 파싱 ===

    @Test
    fun `schedule_info ISO 8601 시간 파싱`() {
        val scheduleInfo = ScheduleInfoDto(
            startTime = "2026-02-13T19:00:00+09:00",
            endTime = "2026-02-13T21:00:00+09:00",
            location = "강남역",
            isAllDay = false
        )
        val response = makeResponse(classifiedType = "SCHEDULE", scheduleInfo = scheduleInfo)
        val result = mapper.toDomain(response)

        val info = result.scheduleInfo!!
        assertTrue(info.startTime!! > 0)
        assertTrue(info.endTime!! > info.startTime!!)
        assertEquals("강남역", info.location)
        assertEquals(false, info.isAllDay)
    }

    @Test
    fun `schedule_info null이면 scheduleInfo도 null`() {
        val response = makeResponse(classifiedType = "TODO", scheduleInfo = null)
        val result = mapper.toDomain(response)
        assertNull(result.scheduleInfo)
    }

    @Test
    fun `schedule_info 종일 이벤트`() {
        val scheduleInfo = ScheduleInfoDto(
            startTime = "2026-02-13T00:00:00+09:00",
            isAllDay = true
        )
        val response = makeResponse(classifiedType = "SCHEDULE", scheduleInfo = scheduleInfo)
        val result = mapper.toDomain(response)
        assertEquals(true, result.scheduleInfo!!.isAllDay)
    }

    // === todo_info 파싱 ===

    @Test
    fun `todo_info 마감일과 deadline_source 매핑`() {
        val todoInfo = TodoInfoDto(
            deadline = "2026-02-14T09:00:00+09:00",
            deadlineSource = "AI_EXTRACTED"
        )
        val response = makeResponse(classifiedType = "TODO", todoInfo = todoInfo)
        val result = mapper.toDomain(response)

        val info = result.todoInfo!!
        assertTrue(info.deadline!! > 0)
        assertEquals(DeadlineSource.AI_EXTRACTED, info.deadlineSource)
    }

    @Test
    fun `todo_info AI_SUGGESTED deadline_source 매핑`() {
        val todoInfo = TodoInfoDto(deadlineSource = "AI_SUGGESTED")
        val response = makeResponse(classifiedType = "TODO", todoInfo = todoInfo)
        val result = mapper.toDomain(response)
        assertEquals(DeadlineSource.AI_SUGGESTED, result.todoInfo!!.deadlineSource)
    }

    @Test
    fun `todo_info deadline null이면 null 유지`() {
        val todoInfo = TodoInfoDto(deadline = null, deadlineSource = null)
        val response = makeResponse(classifiedType = "TODO", todoInfo = todoInfo)
        val result = mapper.toDomain(response)
        assertNull(result.todoInfo!!.deadline)
        assertNull(result.todoInfo!!.deadlineSource)
    }

    // === split_items 파싱 ===

    @Test
    fun `split_items null이면 splitItems도 null`() {
        val response = makeResponse(splitItems = null)
        val result = mapper.toDomain(response)
        assertNull(result.splitItems)
    }

    @Test
    fun `split_items 2개 분할 정상 매핑`() {
        val splitItems = listOf(
            SplitItemDto(
                splitText = "금요일 미팅",
                classifiedType = "SCHEDULE",
                confidence = "HIGH",
                aiTitle = "금요일 미팅",
                tags = listOf("미팅"),
                scheduleInfo = ScheduleInfoDto(
                    startTime = "2026-02-13T09:00:00+09:00",
                    isAllDay = false
                )
            ),
            SplitItemDto(
                splitText = "카페 알아보기",
                classifiedType = "TODO",
                confidence = "MEDIUM",
                aiTitle = "카페 알아보기",
                tags = listOf("알아볼것"),
                todoInfo = TodoInfoDto(deadline = null, subType = "알아볼것")
            )
        )
        val response = makeResponse(splitItems = splitItems)
        val result = mapper.toDomain(response)

        assertEquals(2, result.splitItems!!.size)

        val first = result.splitItems!![0]
        assertEquals("금요일 미팅", first.splitText)
        assertEquals(ClassifiedType.SCHEDULE, first.classifiedType)
        assertEquals(ConfidenceLevel.HIGH, first.confidence)
        assertTrue(first.scheduleInfo!!.startTime!! > 0)

        val second = result.splitItems!![1]
        assertEquals("카페 알아보기", second.splitText)
        assertEquals(ClassifiedType.TODO, second.classifiedType)
        assertEquals(ConfidenceLevel.MEDIUM, second.confidence)
    }

    // === 헬퍼 ===

    private fun makeResponse(
        classifiedType: String = "TODO",
        noteSubType: String? = null,
        confidence: String = "HIGH",
        aiTitle: String = "테스트 제목",
        tags: List<String> = emptyList(),
        entities: List<EntityDto> = emptyList(),
        scheduleInfo: ScheduleInfoDto? = null,
        todoInfo: TodoInfoDto? = null,
        splitItems: List<SplitItemDto>? = null
    ) = ClassifyResponse(
        classifiedType = classifiedType,
        noteSubType = noteSubType,
        confidence = confidence,
        aiTitle = aiTitle,
        tags = tags,
        entities = entities,
        scheduleInfo = scheduleInfo,
        todoInfo = todoInfo,
        splitItems = splitItems
    )
}
