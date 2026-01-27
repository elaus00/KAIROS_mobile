package com.example.kairos_mobile.domain.usecase.ai

import com.example.kairos_mobile.domain.model.InsightType
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * MatchKeywordsUseCase 유닛 테스트
 *
 * 테스트 대상:
 * - invoke() 메서드를 통한 키워드 매칭
 * - matchTypes() 메서드를 통한 최적 타입 추출
 */
class MatchKeywordsUseCaseTest {

    private lateinit var useCase: MatchKeywordsUseCase

    @Before
    fun setup() {
        useCase = MatchKeywordsUseCase()
    }

    // ==================== invoke() 테스트 ====================

    @Test
    fun `일정 관련 텍스트는 SCHEDULE 타입 반환`() {
        // Given
        val text = "내일 오후 3시에 팀 회의가 있습니다"

        // When
        val result = useCase(text)

        // Then
        assertTrue(result.contains(InsightType.SCHEDULE))
    }

    @Test
    fun `할일 관련 텍스트는 TODO 타입 반환`() {
        // Given
        val text = "이번 주까지 보고서 작업 완료해야 함"

        // When
        val result = useCase(text)

        // Then
        assertTrue(result.contains(InsightType.TODO))
    }

    @Test
    fun `아이디어 관련 텍스트는 IDEA 타입 반환`() {
        // Given
        val text = "새로운 기능에 대한 아이디어가 떠올랐다"

        // When
        val result = useCase(text)

        // Then
        assertTrue(result.contains(InsightType.IDEA))
    }

    @Test
    fun `메모 관련 텍스트는 NOTE 타입 반환`() {
        // Given
        val text = "중요한 내용 메모해두기"

        // When
        val result = useCase(text)

        // Then
        assertTrue(result.contains(InsightType.NOTE))
    }

    @Test
    fun `복합 키워드 텍스트는 여러 타입 반환`() {
        // Given
        val text = "회의에서 나온 아이디어를 메모"

        // When
        val result = useCase(text)

        // Then
        assertTrue(result.size >= 2)
    }

    @Test
    fun `빈 텍스트는 빈 리스트 반환`() {
        // Given
        val text = ""

        // When
        val result = useCase(text)

        // Then
        assertTrue(result.isEmpty())
    }

    @Test
    fun `관련 없는 텍스트는 빈 리스트 반환`() {
        // Given
        val text = "안녕하세요 좋은 아침입니다"

        // When
        val result = useCase(text)

        // Then
        assertTrue(result.isEmpty())
    }

    // ==================== 추가 테스트 ====================

    @Test
    fun `가장 적합한 타입이 첫 번째로 반환`() {
        // Given
        val text = "다음 주 미팅 일정 조정"

        // When
        val result = useCase(text)

        // Then
        assertEquals(InsightType.SCHEDULE, result.firstOrNull())
    }

    @Test
    fun `매칭 없으면 빈 리스트 반환`() {
        // Given
        val text = "안녕하세요"

        // When
        val result = useCase(text)

        // Then
        assertTrue(result.isEmpty())
    }

    @Test
    fun `영어 텍스트도 정상 동작`() {
        // Given
        val text = "Meeting scheduled for tomorrow"

        // When
        val result = useCase(text)

        // Then
        assertTrue(result.contains(InsightType.SCHEDULE))
    }
}
