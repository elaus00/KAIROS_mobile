package com.example.kairos_mobile.domain.util

import com.example.kairos_mobile.domain.model.CaptureType
import org.junit.Assert.*
import org.junit.Test

/**
 * KeywordMatcher 유닛 테스트
 *
 * 테스트 대상:
 * - 키워드 기반 CaptureType 매칭 로직
 * - 한글/영어 키워드 지원
 * - 우선순위 정렬
 */
class KeywordMatcherTest {

    // ==================== matchTypes() 테스트 ====================

    @Test
    fun `빈 텍스트는 빈 리스트 반환`() {
        // Given
        val text = ""

        // When
        val result = KeywordMatcher.matchTypes(text)

        // Then
        assertTrue(result.isEmpty())
    }

    @Test
    fun `1글자 텍스트는 빈 리스트 반환`() {
        // Given
        val text = "아"

        // When
        val result = KeywordMatcher.matchTypes(text)

        // Then
        assertTrue(result.isEmpty())
    }

    @Test
    fun `회의 키워드는 SCHEDULE 타입 매칭`() {
        // Given
        val text = "내일 팀 회의가 있습니다"

        // When
        val result = KeywordMatcher.matchTypes(text)

        // Then
        assertTrue(result.contains(CaptureType.SCHEDULE))
    }

    @Test
    fun `일정 관련 한글 키워드들은 SCHEDULE 타입 매칭`() {
        val testCases = listOf(
            "미팅 준비해야 함",
            "일정 확인",
            "약속 시간 변경",
            "내일 만남"
        )

        testCases.forEach { text ->
            val result = KeywordMatcher.matchTypes(text)
            assertTrue(
                "텍스트 '$text'는 SCHEDULE을 포함해야 함",
                result.contains(CaptureType.SCHEDULE)
            )
        }
    }

    @Test
    fun `TODO 키워드는 TODO 타입 매칭`() {
        // Given
        val text = "이번 주까지 해야 할 작업"

        // When
        val result = KeywordMatcher.matchTypes(text)

        // Then
        assertTrue(result.contains(CaptureType.TODO))
    }

    @Test
    fun `할일 관련 한글 키워드들은 TODO 타입 매칭`() {
        val testCases = listOf(
            "해야지 오늘 안에",
            "과제 완료해야함",
            "작업 마무리",
            "진행 중인 일"
        )

        testCases.forEach { text ->
            val result = KeywordMatcher.matchTypes(text)
            assertTrue(
                "텍스트 '$text'는 TODO를 포함해야 함",
                result.contains(CaptureType.TODO)
            )
        }
    }

    @Test
    fun `아이디어 키워드는 IDEA 타입 매칭`() {
        // Given
        val text = "새로운 아이디어가 떠올랐다"

        // When
        val result = KeywordMatcher.matchTypes(text)

        // Then
        assertTrue(result.contains(CaptureType.IDEA))
    }

    @Test
    fun `아이디어 관련 한글 키워드들은 IDEA 타입 매칭`() {
        val testCases = listOf(
            "생각해봤는데 이게 좋을 것 같아",
            "개선 방안 제안",
            "어떨까요 이런 방식은"
        )

        testCases.forEach { text ->
            val result = KeywordMatcher.matchTypes(text)
            assertTrue(
                "텍스트 '$text'는 IDEA를 포함해야 함",
                result.contains(CaptureType.IDEA)
            )
        }
    }

    @Test
    fun `메모 키워드는 NOTE 타입 매칭`() {
        // Given
        val text = "메모해두기: 중요한 정보"

        // When
        val result = KeywordMatcher.matchTypes(text)

        // Then
        assertTrue(result.contains(CaptureType.NOTE))
    }

    @Test
    fun `영어 키워드도 정상 매칭`() {
        val testCases = mapOf(
            "meeting tomorrow at 3pm" to CaptureType.SCHEDULE,
            "I have an idea for improvement" to CaptureType.IDEA,
            "todo: finish the report" to CaptureType.TODO,
            "note this for later" to CaptureType.NOTE
        )

        testCases.forEach { (text, expectedType) ->
            val result = KeywordMatcher.matchTypes(text)
            assertTrue(
                "Text '$text' should contain $expectedType",
                result.contains(expectedType)
            )
        }
    }

    @Test
    fun `대소문자 구분 없이 매칭`() {
        // Given
        val upperCase = "MEETING TOMORROW"
        val lowerCase = "meeting tomorrow"
        val mixedCase = "MeEtInG ToMoRrOw"

        // When & Then
        listOf(upperCase, lowerCase, mixedCase).forEach { text ->
            val result = KeywordMatcher.matchTypes(text)
            assertTrue(
                "텍스트 '$text'는 SCHEDULE을 포함해야 함",
                result.contains(CaptureType.SCHEDULE)
            )
        }
    }

    @Test
    fun `여러 타입이 동시에 매칭될 수 있음`() {
        // Given - 회의(SCHEDULE) + 해야(TODO)
        val text = "내일 회의에서 해야 할 발표 준비"

        // When
        val result = KeywordMatcher.matchTypes(text)

        // Then
        assertTrue(result.size >= 2)
        assertTrue(result.contains(CaptureType.SCHEDULE))
        assertTrue(result.contains(CaptureType.TODO))
    }

    @Test
    fun `최대 3개까지만 반환`() {
        // Given - 모든 타입의 키워드가 포함된 텍스트
        val text = "회의에서 아이디어를 메모하고 해야 할 작업 정리"

        // When
        val result = KeywordMatcher.matchTypes(text)

        // Then
        assertTrue(result.size <= 3)
    }

    @Test
    fun `많이 매칭된 타입이 우선`() {
        // Given - SCHEDULE 키워드 여러개 (회의, 일정, 시간)
        val text = "팀 회의 일정 시간 조정 필요"

        // When
        val result = KeywordMatcher.matchTypes(text)

        // Then - SCHEDULE이 첫 번째여야 함
        assertEquals(CaptureType.SCHEDULE, result.firstOrNull())
    }

    @Test
    fun `매칭되지 않는 텍스트는 빈 리스트 반환`() {
        // Given
        val text = "안녕하세요 반갑습니다"

        // When
        val result = KeywordMatcher.matchTypes(text)

        // Then
        assertTrue(result.isEmpty())
    }

    // ==================== matchesType() 테스트 ====================

    @Test
    fun `matchesType - 특정 타입 매칭 확인`() {
        // Given
        val text = "내일 미팅 있어요"

        // When & Then
        assertTrue(KeywordMatcher.matchesType(text, CaptureType.SCHEDULE))
        assertFalse(KeywordMatcher.matchesType(text, CaptureType.IDEA))
    }

    @Test
    fun `matchesType - 짧은 텍스트는 false`() {
        // Given
        val text = "a"

        // When & Then
        assertFalse(KeywordMatcher.matchesType(text, CaptureType.SCHEDULE))
    }

    // ==================== getBestMatch() 테스트 ====================

    @Test
    fun `getBestMatch - 가장 적합한 타입 반환`() {
        // Given
        val text = "내일 팀 미팅 일정"

        // When
        val result = KeywordMatcher.getBestMatch(text)

        // Then
        assertEquals(CaptureType.SCHEDULE, result)
    }

    @Test
    fun `getBestMatch - 매칭 없으면 null 반환`() {
        // Given
        val text = "안녕하세요"

        // When
        val result = KeywordMatcher.getBestMatch(text)

        // Then
        assertNull(result)
    }

    @Test
    fun `getBestMatch - 빈 텍스트는 null 반환`() {
        // Given
        val text = ""

        // When
        val result = KeywordMatcher.getBestMatch(text)

        // Then
        assertNull(result)
    }
}
