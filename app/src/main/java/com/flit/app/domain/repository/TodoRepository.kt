package com.flit.app.domain.repository

import com.flit.app.domain.model.Todo
import kotlinx.coroutines.flow.Flow

/**
 * 할 일 Repository 인터페이스
 */
interface TodoRepository {

    /** 할 일 생성 */
    suspend fun createTodo(todo: Todo)

    /** 할 일 조회 (ID 기준) */
    suspend fun getTodoById(todoId: String): Todo?

    /** 할 일 조회 (capture_id 기준) */
    suspend fun getTodoByCaptureId(captureId: String): Todo?

    /** 활성 할 일 목록 (is_completed=false, sort_order 순) */
    fun getActiveTodos(): Flow<List<Todo>>

    /** 전체 할 일 목록 (완료 여부 무관) */
    fun getAllTodos(): Flow<List<Todo>>

    /** 완료된 할 일 목록 */
    fun getCompletedTodos(): Flow<List<Todo>>

    /** 완료 토글 */
    suspend fun toggleCompletion(todoId: String)

    /** 정렬 순서 업데이트 */
    suspend fun updateSortOrder(todoId: String, sortOrder: Int, sortSource: String)

    /** 마감일 업데이트 */
    suspend fun updateDeadline(todoId: String, deadlineMs: Long)

    /** capture_id로 삭제 */
    suspend fun deleteByCaptureId(captureId: String)
}
