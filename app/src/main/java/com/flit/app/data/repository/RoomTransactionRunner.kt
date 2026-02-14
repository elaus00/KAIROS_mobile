package com.flit.app.data.repository

import androidx.room.withTransaction
import com.flit.app.data.local.database.FlitDatabase
import com.flit.app.domain.repository.TransactionRunner
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Room DB 기반 트랜잭션 실행기.
 */
@Singleton
class RoomTransactionRunner @Inject constructor(
    private val database: FlitDatabase
) : TransactionRunner {
    override suspend fun runInTransaction(block: suspend () -> Unit) {
        database.withTransaction {
            block()
        }
    }
}
