package com.example.kairos_mobile.data.repository

import com.example.kairos_mobile.data.local.database.dao.ScheduleDao
import com.example.kairos_mobile.data.mapper.ScheduleMapper
import com.example.kairos_mobile.domain.model.Result
import com.example.kairos_mobile.domain.model.Schedule
import com.example.kairos_mobile.domain.model.ScheduleCategory
import com.example.kairos_mobile.domain.repository.ScheduleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Schedule Repository 구현체 (PRD v4.0)
 */
@Singleton
class ScheduleRepositoryImpl @Inject constructor(
    private val scheduleDao: ScheduleDao
) : ScheduleRepository {

    override fun getSchedulesByDate(date: LocalDate): Flow<List<Schedule>> {
        return scheduleDao.getSchedulesByDate(date.toEpochDay()).map { entities ->
            ScheduleMapper.toDomainList(entities)
        }
    }

    override fun getSchedulesBetweenDates(
        startDate: LocalDate,
        endDate: LocalDate
    ): Flow<List<Schedule>> {
        return scheduleDao.getSchedulesBetweenDates(
            startDate.toEpochDay(),
            endDate.toEpochDay()
        ).map { entities ->
            ScheduleMapper.toDomainList(entities)
        }
    }

    override fun getUpcomingSchedules(): Flow<List<Schedule>> {
        return scheduleDao.getUpcomingSchedules(LocalDate.now().toEpochDay()).map { entities ->
            ScheduleMapper.toDomainList(entities)
        }
    }

    override fun getAllSchedules(): Flow<List<Schedule>> {
        return scheduleDao.getAllSchedules().map { entities ->
            ScheduleMapper.toDomainList(entities)
        }
    }

    override fun getSchedulesByCategory(category: ScheduleCategory): Flow<List<Schedule>> {
        return scheduleDao.getSchedulesByCategory(category.name).map { entities ->
            ScheduleMapper.toDomainList(entities)
        }
    }

    override fun getDatesWithSchedules(
        startDate: LocalDate,
        endDate: LocalDate
    ): Flow<List<LocalDate>> {
        return scheduleDao.getDatesWithSchedules(
            startDate.toEpochDay(),
            endDate.toEpochDay()
        ).map { epochDays ->
            epochDays.map { LocalDate.ofEpochDay(it) }
        }
    }

    override suspend fun createSchedule(schedule: Schedule): Result<Schedule> {
        return try {
            val entity = ScheduleMapper.toEntity(schedule)
            scheduleDao.insert(entity)
            Result.Success(schedule)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun createScheduleFromCapture(
        captureId: String,
        title: String,
        date: LocalDate,
        time: LocalTime,
        location: String?,
        category: ScheduleCategory
    ): Result<Schedule> {
        return try {
            // 이미 해당 캡처로 생성된 일정이 있는지 확인
            val existing = scheduleDao.getScheduleByCaptureId(captureId)
            if (existing != null) {
                return Result.Success(ScheduleMapper.toDomain(existing))
            }

            val now = Instant.now()
            val schedule = Schedule(
                id = UUID.randomUUID().toString(),
                title = title,
                time = time,
                date = date,
                location = location,
                category = category,
                googleCalendarId = null,
                sourceCaptureId = captureId,
                createdAt = now,
                updatedAt = now
            )

            val entity = ScheduleMapper.toEntity(schedule)
            scheduleDao.insert(entity)
            Result.Success(schedule)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun updateSchedule(schedule: Schedule): Result<Schedule> {
        return try {
            val updated = schedule.copy(updatedAt = Instant.now())
            val entity = ScheduleMapper.toEntity(updated)
            scheduleDao.update(entity)
            Result.Success(updated)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun deleteSchedule(id: String): Result<Unit> {
        return try {
            scheduleDao.deleteById(id)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun getScheduleById(id: String): Result<Schedule?> {
        return try {
            val entity = scheduleDao.getById(id)
            Result.Success(entity?.let { ScheduleMapper.toDomain(it) })
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun getScheduleByGoogleCalendarId(googleCalendarId: String): Result<Schedule?> {
        return try {
            val entity = scheduleDao.getByGoogleCalendarId(googleCalendarId)
            Result.Success(entity?.let { ScheduleMapper.toDomain(it) })
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun getScheduleCountByDate(date: LocalDate): Int {
        return scheduleDao.getScheduleCountByDate(date.toEpochDay())
    }
}
