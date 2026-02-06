package com.example.kairos_mobile.data.repository

import com.example.kairos_mobile.data.local.database.dao.ScheduleDao
import com.example.kairos_mobile.data.mapper.ScheduleMapper
import com.example.kairos_mobile.domain.model.Schedule
import com.example.kairos_mobile.domain.repository.ScheduleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 일정 Repository 구현체
 */
@Singleton
class ScheduleRepositoryImpl @Inject constructor(
    private val scheduleDao: ScheduleDao,
    private val scheduleMapper: ScheduleMapper
) : ScheduleRepository {

    override suspend fun createSchedule(schedule: Schedule) {
        scheduleDao.insert(scheduleMapper.toEntity(schedule))
    }

    override suspend fun getScheduleByCaptureId(captureId: String): Schedule? {
        return scheduleDao.getByCaptureId(captureId)?.let { scheduleMapper.toDomain(it) }
    }

    override fun getSchedulesByDate(dateStartMs: Long, dateEndMs: Long): Flow<List<Schedule>> {
        return scheduleDao.getSchedulesByDate(dateStartMs, dateEndMs)
            .map { entities -> entities.map { scheduleMapper.toDomain(it) } }
    }

    override fun getDatesWithSchedules(rangeStartMs: Long, rangeEndMs: Long): Flow<List<Long>> {
        return scheduleDao.getDatesWithSchedules(rangeStartMs, rangeEndMs)
    }

    override suspend fun deleteByCaptureId(captureId: String) {
        scheduleDao.deleteByCaptureId(captureId)
    }
}
