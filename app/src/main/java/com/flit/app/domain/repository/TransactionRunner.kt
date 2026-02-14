package com.flit.app.domain.repository

/**
 * 여러 Repository 호출을 하나의 DB 트랜잭션으로 실행한다.
 */
interface TransactionRunner {
    suspend fun runInTransaction(block: suspend () -> Unit)
}
