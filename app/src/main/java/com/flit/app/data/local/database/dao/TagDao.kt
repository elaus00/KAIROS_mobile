package com.flit.app.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.flit.app.data.local.database.entities.TagEntity

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
     * ID로 태그 조회
     */
    @Query("SELECT * FROM tags WHERE id = :id")
    suspend fun getById(id: String): TagEntity?

    /**
     * 이름으로 태그 조회
     */
    @Query("SELECT * FROM tags WHERE name = :name LIMIT 1")
    suspend fun getByName(name: String): TagEntity?

    /** 동기화용 전체 태그 조회 */
    @Query("SELECT * FROM tags")
    suspend fun getAllForSync(): List<TagEntity>

}
