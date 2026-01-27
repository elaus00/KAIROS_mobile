package com.example.kairos_mobile.domain.usecase

import com.example.kairos_mobile.domain.model.Capture
import com.example.kairos_mobile.domain.model.CaptureSource
import com.example.kairos_mobile.domain.model.CaptureType
import com.example.kairos_mobile.domain.model.Result
import com.example.kairos_mobile.domain.model.SearchQuery
import com.example.kairos_mobile.domain.repository.CaptureRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * SearchCapturesUseCase 유닛 테스트
 *
 * 테스트 대상:
 * - 검색 쿼리 검증
 * - 페이징 파라미터 검증
 * - Repository 호출 및 결과 반환
 */
class SearchCapturesUseCaseTest {

    private lateinit var repository: CaptureRepository
    private lateinit var useCase: SearchCapturesUseCase

    @Before
    fun setup() {
        repository = mockk()
        useCase = SearchCapturesUseCase(repository)
    }

    // ==================== 정상 케이스 ====================

    @Test
    fun `검색 성공 시 캡처 리스트 반환`() = runTest {
        // Given
        val query = SearchQuery(text = "회의")
        val expectedCaptures = listOf(
            Capture(id = "1", content = "팀 회의 내용"),
            Capture(id = "2", content = "프로젝트 회의록")
        )
        coEvery { repository.searchCaptures(any(), eq(0), eq(20)) } returns Result.Success(expectedCaptures)

        // When
        val result = useCase(query)

        // Then
        assertTrue(result is Result.Success)
        assertEquals(expectedCaptures, (result as Result.Success).data)
    }

    @Test
    fun `빈 검색어로도 검색 가능`() = runTest {
        // Given
        val query = SearchQuery(text = "")
        val expectedCaptures = listOf(Capture(id = "1", content = "테스트"))
        coEvery { repository.searchCaptures(any(), eq(0), eq(20)) } returns Result.Success(expectedCaptures)

        // When
        val result = useCase(query)

        // Then
        assertTrue(result is Result.Success)
    }

    @Test
    fun `타입 필터로 검색`() = runTest {
        // Given
        val query = SearchQuery(
            text = "",
            types = setOf(CaptureType.SCHEDULE, CaptureType.TODO)
        )
        val expectedCaptures = listOf(
            Capture(id = "1", content = "일정")
        )
        coEvery { repository.searchCaptures(any(), eq(0), eq(20)) } returns Result.Success(expectedCaptures)

        // When
        val result = useCase(query)

        // Then
        assertTrue(result is Result.Success)
    }

    @Test
    fun `소스 필터로 검색`() = runTest {
        // Given
        val query = SearchQuery(
            text = "",
            sources = setOf(CaptureSource.IMAGE, CaptureSource.VOICE)
        )
        coEvery { repository.searchCaptures(any(), eq(0), eq(20)) } returns Result.Success(emptyList())

        // When
        val result = useCase(query)

        // Then
        assertTrue(result is Result.Success)
    }

    @Test
    fun `커스텀 페이징 파라미터로 검색`() = runTest {
        // Given
        val query = SearchQuery(text = "test")
        val offset = 20
        val limit = 10
        coEvery { repository.searchCaptures(any(), eq(offset), eq(limit)) } returns Result.Success(emptyList())

        // When
        val result = useCase(query, offset = offset, limit = limit)

        // Then
        assertTrue(result is Result.Success)
    }

    // ==================== 에러 케이스 ====================

    @Test
    fun `음수 offset은 에러 반환`() = runTest {
        // Given
        val query = SearchQuery(text = "test")

        // When
        val result = useCase(query, offset = -1, limit = 20)

        // Then
        assertTrue(result is Result.Error)
        assertTrue((result as Result.Error).exception is IllegalArgumentException)
    }

    @Test
    fun `0 이하 limit은 에러 반환`() = runTest {
        // Given
        val query = SearchQuery(text = "test")

        // When
        val result = useCase(query, offset = 0, limit = 0)

        // Then
        assertTrue(result is Result.Error)
        assertTrue((result as Result.Error).exception is IllegalArgumentException)
    }

    @Test
    fun `음수 limit은 에러 반환`() = runTest {
        // Given
        val query = SearchQuery(text = "test")

        // When
        val result = useCase(query, offset = 0, limit = -5)

        // Then
        assertTrue(result is Result.Error)
    }

    @Test
    fun `Repository 에러 시 에러 전파`() = runTest {
        // Given
        val query = SearchQuery(text = "test")
        val exception = RuntimeException("Database error")
        coEvery { repository.searchCaptures(any(), any(), any()) } returns Result.Error(exception)

        // When
        val result = useCase(query)

        // Then
        assertTrue(result is Result.Error)
        assertEquals("Database error", (result as Result.Error).exception.message)
    }

    // ==================== 경계값 테스트 ====================

    @Test
    fun `offset 0, limit 1 최소값 허용`() = runTest {
        // Given
        val query = SearchQuery(text = "test")
        coEvery { repository.searchCaptures(any(), eq(0), eq(1)) } returns Result.Success(emptyList())

        // When
        val result = useCase(query, offset = 0, limit = 1)

        // Then
        assertTrue(result is Result.Success)
    }

    @Test
    fun `큰 offset 값 허용`() = runTest {
        // Given
        val query = SearchQuery(text = "test")
        coEvery { repository.searchCaptures(any(), eq(10000), eq(20)) } returns Result.Success(emptyList())

        // When
        val result = useCase(query, offset = 10000)

        // Then
        assertTrue(result is Result.Success)
    }
}
