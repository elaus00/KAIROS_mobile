package com.example.kairos_mobile.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.kairos_mobile.data.local.database.entities.TodoEntity
import kotlinx.coroutines.flow.Flow

/**
 * Todo DAO
 * 투두 항목 데이터베이스 접근
 */
@Dao
interface TodoDao {

    /**
     * 새 투두 삽입
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(todo: TodoEntity)

    /**
     * 투두 업데이트
     */
    @Update
    suspend fun update(todo: TodoEntity)

    /**
     * 투두 삭제
     */
    @Query("DELETE FROM todos WHERE id = :id")
    suspend fun deleteById(id: String)

    /**
     * ID로 투두 조회
     */
    @Query("SELECT * FROM todos WHERE id = :id")
    suspend fun getById(id: String): TodoEntity?

    /**
     * 활성 투두 조회 (완료되지 않은 항목)
     * 마감일 → 우선순위 → 생성일 순 정렬
     */
    @Query("""
        SELECT * FROM todos
        WHERE is_completed = 0
        ORDER BY
            CASE WHEN due_date IS NULL THEN 1 ELSE 0 END,
            due_date ASC,
            priority DESC,
            created_at DESC
    """)
    fun getActiveTodos(): Flow<List<TodoEntity>>

    /**
     * 완료된 투두 조회
     * 완료 시간 역순 정렬
     */
    @Query("""
        SELECT * FROM todos
        WHERE is_completed = 1
        ORDER BY completed_at DESC
    """)
    fun getCompletedTodos(): Flow<List<TodoEntity>>

    /**
     * 모든 투두 조회 (활성 + 완료)
     */
    @Query("SELECT * FROM todos ORDER BY is_completed ASC, created_at DESC")
    fun getAllTodos(): Flow<List<TodoEntity>>

    /**
     * 특정 마감일의 투두 조회
     */
    @Query("""
        SELECT * FROM todos
        WHERE due_date >= :startOfDay AND due_date < :endOfDay
        AND is_completed = 0
        ORDER BY priority DESC, due_time ASC
    """)
    fun getTodosByDueDate(startOfDay: Long, endOfDay: Long): Flow<List<TodoEntity>>

    /**
     * 마감일이 있는 모든 활성 투두 조회 (그룹화용)
     */
    @Query("""
        SELECT * FROM todos
        WHERE is_completed = 0
        ORDER BY due_date ASC, priority DESC, created_at DESC
    """)
    fun getActiveTodosWithDueDate(): Flow<List<TodoEntity>>

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
     * 활성 투두 개수 조회
     */
    @Query("SELECT COUNT(*) FROM todos WHERE is_completed = 0")
    fun getActiveCount(): Flow<Int>

    /**
     * 완료된 투두 개수 조회
     */
    @Query("SELECT COUNT(*) FROM todos WHERE is_completed = 1")
    fun getCompletedCount(): Flow<Int>

    /**
     * 인사이트 ID로 연결된 투두 조회
     */
    @Query("SELECT * FROM todos WHERE source_insight_id = :insightId")
    suspend fun getTodoByInsightId(insightId: String): TodoEntity?
}
