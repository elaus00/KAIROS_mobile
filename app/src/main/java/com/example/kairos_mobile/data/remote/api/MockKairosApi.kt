package com.example.kairos_mobile.data.remote.api

import com.example.kairos_mobile.data.remote.dto.v2.ClassifyRequest
import com.example.kairos_mobile.data.remote.dto.v2.ClassifyResponse
import com.example.kairos_mobile.data.remote.dto.v2.ClipMetadataDto
import com.example.kairos_mobile.data.remote.dto.v2.ClipRequest
import com.example.kairos_mobile.data.remote.dto.v2.ClipResponse
import com.example.kairos_mobile.data.remote.dto.v2.HealthResponse
import com.example.kairos_mobile.data.remote.dto.v2.NoteCreateRequest
import com.example.kairos_mobile.data.remote.dto.v2.NoteCreateResponse
import com.example.kairos_mobile.data.remote.dto.v2.OcrRequest
import com.example.kairos_mobile.data.remote.dto.v2.OcrResponse
import com.example.kairos_mobile.data.remote.dto.v2.SttRequest
import com.example.kairos_mobile.data.remote.dto.v2.SttResponse
import com.example.kairos_mobile.data.remote.dto.v2.TodoMetadataDto
import kotlinx.coroutines.delay
import retrofit2.Response
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

/**
 * Mock KAIROS API 구현체 (API v2.1)
 * 실제 백엔드 대신 사용하는 로컬 Mock
 */
@Singleton
class MockKairosApi @Inject constructor() : KairosApi {

    /**
     * Mock AI 분류
     * 키워드 기반 분류 로직
     */
    override suspend fun classify(
        request: ClassifyRequest
    ): Response<ClassifyResponse> {
        // 네트워크 지연 시뮬레이션 (500ms)
        delay(500)

        val content = request.content.lowercase()

        // 키워드 기반 분류
        val (type, destination, todoMetadata) = when {
            // 할 일 관련 키워드 → todo destination
            content.contains("해야") ||
                    content.contains("하기") ||
                    content.contains("todo") ||
                    content.contains("작업") ||
                    content.contains("마감") -> {
                val priority = when {
                    content.contains("긴급") || content.contains("urgent") -> "high"
                    content.contains("중요") || content.contains("important") -> "medium"
                    else -> "none"
                }
                val dueDate = extractDueDate(content)
                Triple(
                    "todo",
                    "todo",
                    TodoMetadataDto(
                        dueDate = dueDate,
                        dueTime = null,
                        priority = priority,
                        labels = extractLabels(content)
                    )
                )
            }

            // 아이디어 관련 키워드 → obsidian
            content.contains("아이디어") ||
                    content.contains("생각") ||
                    content.contains("idea") ||
                    content.startsWith("만약") -> {
                Triple("idea", "obsidian", null)
            }

            // 웹 URL → clip
            content.contains("http://") ||
                    content.contains("https://") -> {
                Triple("clip", "obsidian", null)
            }

            // 노트 (긴 텍스트) → obsidian
            content.length > 100 -> {
                Triple("note", "obsidian", null)
            }

            // 기본: 빠른 메모 → obsidian
            else -> {
                Triple("quick_note", "obsidian", null)
            }
        }

        // 제목 추출 (첫 줄 또는 첫 50자)
        val title = request.content
            .lines()
            .firstOrNull()
            ?.take(50)
            ?: "Untitled"

        // 태그 추출
        val tags = extractTags(request.content)

        // 신뢰도 계산 (랜덤 0.85 ~ 0.98)
        val confidence = Random.nextFloat() * 0.13f + 0.85f

        // 추론 근거 생성
        val reasoning = when (type) {
            "todo" -> "할 일 관련 키워드가 감지되어 투두로 분류했습니다."
            "idea" -> "아이디어/생각 관련 키워드가 감지되어 아이디어로 분류했습니다."
            "clip" -> "URL이 포함되어 웹 클립으로 분류했습니다."
            "note" -> "긴 텍스트로 판단되어 노트로 분류했습니다."
            else -> "짧은 메모로 판단되어 빠른 메모로 분류했습니다."
        }

        val response = ClassifyResponse(
            success = true,
            type = type,
            destination = destination,
            confidence = confidence,
            reasoning = reasoning,
            title = title,
            tags = tags,
            suggestedFilename = "${title.replace(" ", "_")}.md",
            suggestedPath = when (type) {
                "idea" -> "Ideas/Inbox"
                "note" -> "Notes/Inbox"
                "quick_note" -> "QuickNotes"
                "clip" -> "Clips"
                else -> null
            },
            todoMetadata = todoMetadata
        )

        return Response.success(response)
    }

    /**
     * Mock Obsidian 노트 생성
     */
    override suspend fun createNote(
        request: NoteCreateRequest
    ): Response<NoteCreateResponse> {
        // 네트워크 지연 시뮬레이션 (300ms)
        delay(300)

        // 90% 성공률 시뮬레이션
        val success = Random.nextFloat() > 0.1f

        val response = if (success) {
            NoteCreateResponse(
                success = true,
                noteId = UUID.randomUUID().toString(),
                path = "${request.folder ?: "Inbox"}/${request.title}.md",
                title = request.title,
                createdAt = java.time.Instant.now().toString(),
                error = null
            )
        } else {
            NoteCreateResponse(
                success = false,
                noteId = null,
                path = null,
                title = null,
                createdAt = null,
                error = "Failed to create note: Connection timeout"
            )
        }

        return Response.success(response)
    }

    /**
     * Mock OCR - 이미지에서 텍스트 추출
     */
    override suspend fun ocr(
        request: OcrRequest
    ): Response<OcrResponse> {
        // 네트워크 지연 시뮬레이션 (2-5초)
        delay(Random.nextLong(2000, 5000))

        val response = OcrResponse(
            success = true,
            text = "이미지에서 추출된 샘플 텍스트입니다.\n회의 내용: 프로젝트 일정 논의\n참석자: 김철수, 이영희",
            confidence = Random.nextFloat() * 0.15f + 0.85f,
            language = request.languageHint ?: "ko",
            wordCount = 15,
            error = null
        )

        return Response.success(response)
    }

    /**
     * Mock STT - 오디오에서 텍스트 추출
     */
    override suspend fun stt(
        request: SttRequest
    ): Response<SttResponse> {
        // 네트워크 지연 시뮬레이션 (2-5초)
        delay(Random.nextLong(2000, 5000))

        val response = SttResponse(
            success = true,
            text = "음성에서 추출된 샘플 텍스트입니다. 내일 오후 3시에 팀 미팅이 있습니다.",
            confidence = Random.nextFloat() * 0.15f + 0.85f,
            durationSeconds = Random.nextFloat() * 10f + 5f,
            error = null
        )

        return Response.success(response)
    }

    /**
     * Mock 웹 클립 - URL에서 메타데이터 추출 및 AI 요약
     */
    override suspend fun clip(
        request: ClipRequest
    ): Response<ClipResponse> {
        // 네트워크 지연 시뮬레이션 (3-6초)
        delay(Random.nextLong(3000, 6000))

        val response = ClipResponse(
            success = true,
            url = request.url,
            title = "웹 페이지 제목 - ${request.url.substringAfter("://").take(30)}",
            content = "이 페이지의 주요 내용입니다. 여러 문단으로 이루어진 글이 여기에 포함됩니다.",
            summary = if (request.summarize) {
                "AI가 요약한 웹 페이지 내용입니다. 주요 내용을 간략하게 정리했습니다."
            } else null,
            metadata = ClipMetadataDto(
                author = "Author Name",
                publishedDate = "2026-01-25",
                siteName = request.url.substringAfter("://").substringBefore("/"),
                imageUrl = null,
                wordCount = 150
            ),
            tags = listOf("web", "article"),
            error = null
        )

        return Response.success(response)
    }

    /**
     * Mock 서버 상태 확인
     */
    override suspend fun health(): Response<HealthResponse> {
        delay(100)

        val response = HealthResponse(
            success = true,
            status = "healthy",
            version = "2.1.0",
            environment = "mock",
            timestamp = java.time.Instant.now().toString()
        )

        return Response.success(response)
    }

    // ========== Helper Methods ==========

    /**
     * 텍스트에서 해시태그 및 키워드 추출
     */
    private fun extractTags(content: String): List<String> {
        val tags = mutableListOf<String>()

        // # 태그 추출
        val hashTagRegex = "#([가-힣a-zA-Z0-9_]+)".toRegex()
        hashTagRegex.findAll(content).forEach {
            tags.add(it.groupValues[1])
        }

        // 키워드 기반 자동 태그
        val lowerContent = content.lowercase()
        if (lowerContent.contains("프로젝트")) tags.add("project")
        if (lowerContent.contains("개인")) tags.add("personal")
        if (lowerContent.contains("업무")) tags.add("work")
        if (lowerContent.contains("학습") || lowerContent.contains("공부")) tags.add("study")
        if (lowerContent.contains("팀")) tags.add("team")

        return tags.distinct().take(5)
    }

    /**
     * 텍스트에서 라벨 추출 (Todo용)
     */
    private fun extractLabels(content: String): List<String> {
        val labels = mutableListOf<String>()
        val lowerContent = content.lowercase()

        if (lowerContent.contains("회의") || lowerContent.contains("미팅")) labels.add("meeting")
        if (lowerContent.contains("코드") || lowerContent.contains("개발")) labels.add("dev")
        if (lowerContent.contains("문서")) labels.add("docs")
        if (lowerContent.contains("리뷰")) labels.add("review")

        return labels.distinct()
    }

    /**
     * 텍스트에서 마감일 추출
     */
    private fun extractDueDate(content: String): String? {
        val lowerContent = content.lowercase()
        val today = LocalDate.now()
        val formatter = DateTimeFormatter.ISO_LOCAL_DATE

        return when {
            lowerContent.contains("오늘") -> today.format(formatter)
            lowerContent.contains("내일") -> today.plusDays(1).format(formatter)
            lowerContent.contains("모레") -> today.plusDays(2).format(formatter)
            lowerContent.contains("이번 주") -> today.plusDays((7 - today.dayOfWeek.value).toLong()).format(formatter)
            lowerContent.contains("다음 주") -> today.plusWeeks(1).format(formatter)
            else -> null
        }
    }
}
