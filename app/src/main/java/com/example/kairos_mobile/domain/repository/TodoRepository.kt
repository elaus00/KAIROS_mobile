package com.example.kairos_mobile.domain.repository

import com.example.kairos_mobile.domain.model.Todo
import kotlinx.coroutines.flow.Flow

/**
 * 할 일 Repository 인터페이스
 */
interface TodoRepository {

    /** 할 일 생성 */
    suspend fun createTodo(todo: Todo)

    /** 할 일 조회 (capture_id 기준) */
    suspend fun getTodoByCaptureId(captureId: String): Todo?

    /** 활성 할 일 목록 (is_completed=false, sort_order 순) */
    fun getActiveTodos(): Flow<List<Todo>>

    /** 완료 토글 */
    suspend fun toggleCompletion(todoId: String)

    /** capture_id로 삭제 */
    suspend fun deleteByCaptureId(captureId: String)
}
