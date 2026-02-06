package com.example.kairos_mobile.domain.usecase.schedule

import com.example.kairos_mobile.domain.repository.ScheduleRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 일정이 있는 날짜 목록 조회 UseCase
 * 캘린더에서 도트 표시용
 */
@Singleton
class GetDatesWithSchedulesUseCase @Inject constructor(
    private val scheduleRepository: ScheduleRepository
) {
    /**
     * @param rangeStartMs 조회 범위 시작 epoch ms
     * @param rangeEndMs 조회 범위 끝 epoch ms
     * @return epoch day 목록
     */
    operator fun invoke(rangeStartMs: Long, rangeEndMs: Long): Flow<List<Long>> {
        return scheduleRepository.getDatesWithSchedules(rangeStartMs, rangeEndMs)
    }
}
