package com.example.kairos_mobile.domain.usecase.schedule

import com.example.kairos_mobile.domain.model.Schedule
import com.example.kairos_mobile.domain.repository.ScheduleRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 특정 날짜의 일정 조회 UseCase
 */
@Singleton
class GetSchedulesByDateUseCase @Inject constructor(
    private val scheduleRepository: ScheduleRepository
) {
    /**
     * @param dateStartMs 날짜 시작 epoch ms (00:00:00)
     * @param dateEndMs 날짜 끝 epoch ms (23:59:59)
     */
    operator fun invoke(dateStartMs: Long, dateEndMs: Long): Flow<List<Schedule>> {
        return scheduleRepository.getSchedulesByDate(dateStartMs, dateEndMs)
    }
}
