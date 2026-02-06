package com.example.kairos_mobile.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.kairos_mobile.data.local.database.entities.UserPreferenceEntity
import kotlinx.coroutines.flow.Flow

/**
 * 사용자 설정 DAO
 */
@Dao
interface UserPreferenceDao {

    /**
     * 설정 저장 (upsert)
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun set(preference: UserPreferenceEntity)

    /**
     * 설정 값 조회
     */
    @Query("SELECT value FROM user_preferences WHERE `key` = :key")
    suspend fun get(key: String): String?

    /**
     * 설정 값 Flow 조회
     */
    @Query("SELECT value FROM user_preferences WHERE `key` = :key")
    fun observe(key: String): Flow<String?>

    /**
     * 설정 삭제
     */
    @Query("DELETE FROM user_preferences WHERE `key` = :key")
    suspend fun delete(key: String)

    /**
     * 모든 설정 조회
     */
    @Query("SELECT * FROM user_preferences")
    fun getAll(): Flow<List<UserPreferenceEntity>>
}
