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

    override suspend fun deleteByCaptureId(captureId: String) {
        scheduleDao.deleteByCaptureId(captureId)
    }

    override suspend fun getSyncedSchedules(): List<Schedule> {
        return scheduleDao.getByCalendarSyncStatus("SYNCED")
            .map { scheduleMapper.toDomain(it) }
    }

    override suspend fun updateFromRemote(
        scheduleId: String,
        title: String,
        startTime: Long,
        endTime: Long?,
        location: String?
    ) {
        // ScheduleEntity에는 title이 없음 (title은 CaptureEntity에 저장)
        // 시간/장소 등 일정 고유 필드만 업데이트
        val entity = scheduleDao.getById(scheduleId) ?: return
        scheduleDao.update(
            entity.copy(
                startTime = startTime,
                endTime = endTime,
                location = location,
                updatedAt = System.currentTimeMillis()
            )
        )
    }
}
