package com.flit.app.data.repository

import com.flit.app.data.local.database.dao.ScheduleDao
import com.flit.app.data.mapper.ScheduleMapper
import com.flit.app.domain.model.Schedule
import com.flit.app.domain.repository.ScheduleRepository
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

    override suspend fun getScheduleById(id: String): Schedule? {
        return scheduleDao.getById(id)?.let { scheduleMapper.toDomain(it) }
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

    override suspend fun updateSchedule(schedule: Schedule) {
        scheduleDao.update(scheduleMapper.toEntity(schedule))
    }

    override suspend fun deleteByCaptureId(captureId: String) {
        scheduleDao.deleteByCaptureId(captureId)
    }
}
