package com.flit.app.tracing

import androidx.tracing.Trace

/**
 * Tracing 호출이 테스트/JVM 환경에서 실패해도 앱 동작에는 영향이 없도록 보호한다.
 */
object AppTrace {
    inline fun <T> section(name: String, block: () -> T): T {
        runCatching { Trace.beginSection(name) }
        return try {
            block()
        } finally {
            runCatching { Trace.endSection() }
        }
    }

    suspend inline fun <T> suspendSection(name: String, crossinline block: suspend () -> T): T {
        runCatching { Trace.beginSection(name) }
        return try {
            block()
        } finally {
            runCatching { Trace.endSection() }
        }
    }
}
