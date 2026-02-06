package com.example.kairos_mobile.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.kairos_mobile.data.local.database.entities.TodoEntity
import kotlinx.coroutines.flow.Flow

/**
 * 할 일 DAO
 */
@Dao
interface TodoDao {

    /**
     * 할 일 삽입
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(todo: TodoEntity)

    /**
     * 할 일 업데이트
     */
    @Update
    suspend fun update(todo: TodoEntity)

    /**
     * ID로 할 일 조회
     */
    @Query("SELECT * FROM todos WHERE id = :id")
    suspend fun getById(id: String): TodoEntity?

    /**
     * 캡처 ID로 할 일 조회
     */
    @Query("SELECT * FROM todos WHERE capture_id = :captureId")
    suspend fun getByCaptureId(captureId: String): TodoEntity?

    /**
     * 활성 할 일 조회 (정렬: deadline 있는 것 우선 → deadline 오름차순, 없는 것은 created_at 역순)
     */
    @Query("""
        SELECT t.* FROM todos t
        INNER JOIN captures c ON c.id = t.capture_id
        WHERE t.is_completed = 0
        AND c.is_deleted = 0
        ORDER BY
            CASE WHEN t.deadline IS NULL THEN 1 ELSE 0 END,
            t.deadline ASC,
            t.created_at DESC
    """)
    fun getActiveTodos(): Flow<List<TodoEntity>>

    /**
     * 완료된 할 일 조회 (완료 시각 역순)
     */
    @Query("""
        SELECT t.* FROM todos t
        INNER JOIN captures c ON c.id = t.capture_id
        WHERE t.is_completed = 1
        AND c.is_deleted = 0
        ORDER BY t.completed_at DESC
    """)
    fun getCompletedTodos(): Flow<List<TodoEntity>>

    /**
     * 완료 상태 토글
     */
    @Query("""
        UPDATE todos
        SET is_completed = NOT is_completed,
            completed_at = CASE WHEN is_completed = 0 THEN :completedAt ELSE NULL END,
            updated_at = :updatedAt
        WHERE id = :id
    """)
    suspend fun toggleCompletion(id: String, completedAt: Long, updatedAt: Long)

    /**
     * 할 일 삭제
     */
    @Query("DELETE FROM todos WHERE id = :id")
    suspend fun deleteById(id: String)

    /**
     * 캡처 ID로 할 일 삭제
     */
    @Query("DELETE FROM todos WHERE capture_id = :captureId")
    suspend fun deleteByCaptureId(captureId: String)

    /**
     * 활성 할 일 수 조회
     */
    @Query("""
        SELECT COUNT(*) FROM todos t
        INNER JOIN captures c ON c.id = t.capture_id
        WHERE t.is_completed = 0
        AND c.is_deleted = 0
    """)
    fun getActiveCount(): Flow<Int>
}
