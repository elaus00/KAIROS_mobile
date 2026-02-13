package com.flit.app.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.flit.app.data.local.database.entities.TodoEntity
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
     * capture_id로 할 일 조회
     */
    @Query("SELECT * FROM todos WHERE capture_id = :captureId LIMIT 1")
    suspend fun getByCaptureId(captureId: String): TodoEntity?

    /**
     * 활성 할 일 조회 (정렬: deadline 있는 것 우선 → deadline 오름차순, 없는 것은 created_at 역순)
     */
    @Query("""
        SELECT t.* FROM todos t
        INNER JOIN captures c ON c.id = t.capture_id
        WHERE t.is_completed = 0
        AND c.is_deleted = 0
        AND c.is_trashed = 0
        ORDER BY
            CASE WHEN t.deadline IS NULL THEN 1 ELSE 0 END,
            t.deadline ASC,
            t.created_at DESC
    """)
    fun getActiveTodos(): Flow<List<TodoEntity>>

    /**
     * 전체 할 일 조회 (완료 여부 무관, deadline 있는 것 우선 → deadline 오름차순, 없는 것은 created_at 역순)
     */
    @Query("""
        SELECT t.* FROM todos t
        INNER JOIN captures c ON c.id = t.capture_id
        WHERE c.is_deleted = 0
        AND c.is_trashed = 0
        ORDER BY
            CASE WHEN t.deadline IS NULL THEN 1 ELSE 0 END,
            t.deadline ASC,
            t.created_at DESC
    """)
    fun getAllTodos(): Flow<List<TodoEntity>>

    /**
     * 완료된 할 일 조회 (완료 시각 역순)
     */
    @Query("""
        SELECT t.* FROM todos t
        INNER JOIN captures c ON c.id = t.capture_id
        WHERE t.is_completed = 1
        AND c.is_deleted = 0
        AND c.is_trashed = 0
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
     * 캡처 ID로 할 일 삭제
     */
    @Query("DELETE FROM todos WHERE capture_id = :captureId")
    suspend fun deleteByCaptureId(captureId: String)

    @Query("DELETE FROM todos WHERE id = :id")
    suspend fun deleteById(id: String)

    /**
     * 정렬 순서 업데이트
     */
    @Query("""
        UPDATE todos
        SET sort_order = :sortOrder, sort_source = :sortSource, updated_at = :updatedAt
        WHERE id = :id
    """)
    suspend fun updateSortOrder(id: String, sortOrder: Int, sortSource: String, updatedAt: Long)

    /**
     * 활성 할 일 수 조회
     */
    @Query("""
        SELECT COUNT(*) FROM todos t
        INNER JOIN captures c ON c.id = t.capture_id
        WHERE t.is_completed = 0
        AND c.is_deleted = 0
        AND c.is_trashed = 0
    """)
    fun getActiveCount(): Flow<Int>

    /**
     * 오늘 마감 미완료 할 일 수 조회 (위젯 오버플로우 표시용)
     */
    @Query("""
        SELECT COUNT(*)
        FROM todos t
        INNER JOIN captures c ON t.capture_id = c.id
        WHERE t.is_completed = 0 AND c.is_deleted = 0 AND c.is_trashed = 0
        AND (:todayEndMs IS NULL OR t.deadline <= :todayEndMs)
    """)
    suspend fun getTodayIncompleteTodoCount(todayEndMs: Long?): Int

    /**
     * 오늘 마감 미완료 할 일 조회 (위젯용, 최대 5개)
     */
    @Query("""
        SELECT t.id AS todoId, t.capture_id AS captureId,
               c.ai_title AS aiTitle, c.original_text AS originalText,
               t.deadline, t.is_completed AS isCompleted
        FROM todos t
        INNER JOIN captures c ON t.capture_id = c.id
        WHERE t.is_completed = 0 AND c.is_deleted = 0 AND c.is_trashed = 0
        AND (:todayEndMs IS NULL OR t.deadline <= :todayEndMs)
        ORDER BY t.deadline ASC, t.sort_order ASC
        LIMIT 5
    """)
    suspend fun getTodayIncompleteTodos(todayEndMs: Long?): List<TodoWithCaptureRow>

    /**
     * 오늘 마감 할 일 조회 — 완료 포함 (위젯용, 미완료 우선 정렬, 최대 5개)
     */
    @Query("""
        SELECT t.id AS todoId, t.capture_id AS captureId,
               c.ai_title AS aiTitle, c.original_text AS originalText,
               t.deadline, t.is_completed AS isCompleted
        FROM todos t
        INNER JOIN captures c ON t.capture_id = c.id
        WHERE c.is_deleted = 0 AND c.is_trashed = 0
        AND (:todayEndMs IS NULL OR t.deadline <= :todayEndMs)
        ORDER BY t.is_completed ASC, t.deadline ASC, t.sort_order ASC
        LIMIT 5
    """)
    suspend fun getTodayTodosForWidget(todayEndMs: Long?): List<TodoWithCaptureRow>

    /**
     * 오늘 마감 할 일 전체 수 — 완료 포함 (위젯 오버플로우 표시용)
     */
    @Query("""
        SELECT COUNT(*)
        FROM todos t
        INNER JOIN captures c ON t.capture_id = c.id
        WHERE c.is_deleted = 0 AND c.is_trashed = 0
        AND (:todayEndMs IS NULL OR t.deadline <= :todayEndMs)
    """)
    suspend fun getTodayTodoCountForWidget(todayEndMs: Long?): Int

    /**
     * 마감일 업데이트
     */
    @Query("UPDATE todos SET deadline = :deadlineMs, updated_at = :updatedAt WHERE id = :todoId")
    suspend fun updateDeadline(todoId: String, deadlineMs: Long, updatedAt: Long)

    /** 동기화용 전체 할 일 조회 */
    @Query("SELECT * FROM todos")
    suspend fun getAllForSync(): List<TodoEntity>
}

/**
 * 위젯용 할 일 + 캡처 결합 데이터 클래스
 */
data class TodoWithCaptureRow(
    val todoId: String,
    val captureId: String,
    val aiTitle: String?,
    val originalText: String,
    val deadline: Long?,
    val isCompleted: Boolean
)
