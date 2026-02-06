package com.example.kairos_mobile.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.kairos_mobile.data.local.database.entities.TagEntity
import kotlinx.coroutines.flow.Flow

/**
 * 태그 DAO
 */
@Dao
interface TagDao {

    /**
     * 태그 삽입 (이미 존재하면 무시)
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(tag: TagEntity)

    /**
     * 여러 태그 삽입
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(tags: List<TagEntity>)

    /**
     * 모든 태그 조회
     */
    @Query("SELECT * FROM tags ORDER BY name ASC")
    fun getAllTags(): Flow<List<TagEntity>>

    /**
     * ID로 태그 조회
     */
    @Query("SELECT * FROM tags WHERE id = :id")
    suspend fun getById(id: String): TagEntity?

    /**
     * 이름으로 태그 조회
     */
    @Query("SELECT * FROM tags WHERE name = :name LIMIT 1")
    suspend fun getByName(name: String): TagEntity?

    /**
     * 태그 삭제
     */
    @Query("DELETE FROM tags WHERE id = :id")
    suspend fun deleteById(id: String)
}
