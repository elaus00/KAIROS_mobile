package com.example.kairos_mobile.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.kairos_mobile.data.local.database.entities.FolderEntity
import kotlinx.coroutines.flow.Flow

/**
 * 폴더 DAO
 */
@Dao
interface FolderDao {

    /**
     * 폴더 삽입
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(folder: FolderEntity)

    /**
     * 모든 폴더 조회 (정렬 순서대로)
     */
    @Query("SELECT * FROM folders ORDER BY sort_order ASC, created_at ASC")
    fun getAllFolders(): Flow<List<FolderEntity>>

    /**
     * ID로 폴더 조회
     */
    @Query("SELECT * FROM folders WHERE id = :id")
    suspend fun getById(id: String): FolderEntity?

    /**
     * 이름으로 폴더 조회 (중복 체크용)
     */
    @Query("SELECT * FROM folders WHERE name = :name LIMIT 1")
    suspend fun getByName(name: String): FolderEntity?

    /**
     * 폴더 이름 변경
     */
    @Query("UPDATE folders SET name = :newName WHERE id = :id")
    suspend fun rename(id: String, newName: String)

    /**
     * 폴더 삭제 (사용자 폴더만)
     */
    @Query("DELETE FROM folders WHERE id = :id AND type = 'USER'")
    suspend fun deleteById(id: String)

}
