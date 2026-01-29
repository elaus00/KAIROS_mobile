package com.example.kairos_mobile.domain.usecase.capture

import com.example.kairos_mobile.domain.model.Capture
import com.example.kairos_mobile.domain.repository.CaptureRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 모든 캡처 조회 Use Case
 *
 * 비즈니스 로직:
 * - 페이징을 지원하는 전체 캡처 목록 조회
 * - Archive 화면에서 사용
 */
@Singleton
class GetAllCapturesUseCase @Inject constructor(
    private val repository: CaptureRepository
) {
    /**
     * 모든 캡처 조회
     *
     * @param offset 페이징 시작 위치
     * @param limit 페이지 크기
     * @return 캡처 리스트 Flow
     */
    operator fun invoke(
        offset: Int = 0,
        limit: Int = 20
    ): Flow<List<Capture>> {
        return repository.getAllCaptures(offset, limit)
    }

    /**
     * 날짜별로 그룹화된 캡처 조회
     *
     * @return 날짜 키와 캡처 리스트 맵
     */
    fun getCapturesGroupedByDate(): Flow<Map<String, List<Capture>>> {
        return repository.getCapturesGroupedByDate()
    }
}
