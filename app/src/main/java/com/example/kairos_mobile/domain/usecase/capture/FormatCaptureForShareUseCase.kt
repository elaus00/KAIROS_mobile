package com.example.kairos_mobile.domain.usecase.capture

import com.example.kairos_mobile.domain.model.ClassifiedType
import com.example.kairos_mobile.domain.repository.CaptureRepository
import com.example.kairos_mobile.domain.repository.ScheduleRepository
import com.example.kairos_mobile.domain.repository.TodoRepository
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject

/**
 * 캡처를 공유용 텍스트로 포맷팅
 * 유형별로 다른 형식 적용
 */
class FormatCaptureForShareUseCase @Inject constructor(
    private val captureRepository: CaptureRepository,
    private val scheduleRepository: ScheduleRepository,
    private val todoRepository: TodoRepository
) {
    private val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

    suspend operator fun invoke(captureId: String): String {
        val capture = captureRepository.getCaptureById(captureId)
            ?: return ""

        val title = capture.aiTitle ?: capture.originalText.take(50)

        val body = when (capture.classifiedType) {
            ClassifiedType.TODO -> {
                val todo = todoRepository.getTodoByCaptureId(captureId)
                val deadlineText = todo?.deadline?.let { formatDateTime(it) } ?: "미정"
                buildString {
                    appendLine(title)
                    appendLine("마감: $deadlineText")
                    appendLine()
                    append(capture.originalText)
                }
            }
            ClassifiedType.SCHEDULE -> {
                val schedule = scheduleRepository.getScheduleByCaptureId(captureId)
                val startTimeText = schedule?.startTime?.let { formatDateTime(it) } ?: "미정"
                val locationText = schedule?.location ?: "-"
                buildString {
                    appendLine(title)
                    appendLine("일시: $startTimeText")
                    appendLine("장소: $locationText")
                    appendLine()
                    append(capture.originalText)
                }
            }
            ClassifiedType.NOTES -> {
                buildString {
                    appendLine(title)
                    appendLine()
                    append(capture.originalText)
                }
            }
            ClassifiedType.TEMP -> {
                buildString {
                    appendLine(title)
                    appendLine()
                    append(capture.originalText)
                }
            }
        }
        return body
    }

    private fun formatDateTime(epochMs: Long): String {
        return Instant.ofEpochMilli(epochMs)
            .atZone(ZoneId.systemDefault())
            .toLocalDateTime()
            .format(dateTimeFormatter)
    }
}
