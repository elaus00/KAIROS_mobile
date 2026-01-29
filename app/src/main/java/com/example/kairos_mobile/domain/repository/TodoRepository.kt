package com.example.kairos_mobile.domain.repository

import com.example.kairos_mobile.domain.model.Classification
import com.example.kairos_mobile.domain.model.Result
import com.example.kairos_mobile.domain.model.Todo
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

/**
 * 투두 Repository 인터페이스
 */
interface TodoRepository {

    /**
     * 활성 투두 조회 (완료되지 않은 항목)
     */
    fun getActiveTodos(): Flow<List<Todo>>

    /**
     * 완료된 투두 조회
     */
    fun getCompletedTodos(): Flow<List<Todo>>

    /**
     * 마감일별로 그룹화된 투두 조회
     * @return 그룹명(오늘, 내일, 이번 주 등)과 투두 리스트 맵
     */
    fun getTodosGroupedByDueDate(): Flow<Map<String, List<Todo>>>

    /**
     * 새 투두 생성
     */
    suspend fun createTodo(todo: Todo): Result<Todo>

    /**
     * 캡처에서 투두 생성
     * @param captureId 원본 캡처 ID
     * @param classification AI 분류 결과
     */
    suspend fun createTodoFromCapture(
        captureId: String,
        classification: Classification
    ): Result<Todo>

    /**
     * 투두 업데이트
     */
    suspend fun updateTodo(todo: Todo): Result<Todo>

    /**
     * 완료 상태 토글
     */
    suspend fun toggleCompletion(id: String): Result<Unit>

    /**
     * 투두 삭제
     */
    suspend fun deleteTodo(id: String): Result<Unit>

    /**
     * ID로 투두 조회
     */
    suspend fun getTodoById(id: String): Result<Todo?>

    /**
     * 특정 날짜의 할 일 조회
     */
    fun getTodosByDate(date: LocalDate): Flow<List<Todo>>

    /**
     * 활성 투두 개수 조회
     */
    fun getActiveCount(): Flow<Int>
}
