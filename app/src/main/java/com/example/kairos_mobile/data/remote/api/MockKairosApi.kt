package com.example.kairos_mobile.data.remote.api

import com.example.kairos_mobile.data.remote.dto.ai.ClassificationRequest
import com.example.kairos_mobile.data.remote.dto.ai.ClassificationResponse
import com.example.kairos_mobile.data.remote.dto.obsidian.ObsidianCreateRequest
import com.example.kairos_mobile.data.remote.dto.obsidian.ObsidianCreateResponse
import com.example.kairos_mobile.data.remote.dto.insight.OcrResponse
import com.example.kairos_mobile.data.remote.dto.insight.SttResponse
import com.example.kairos_mobile.data.remote.dto.ai.SuggestedTag
import com.example.kairos_mobile.data.remote.dto.ai.SummarizeRequest
import com.example.kairos_mobile.data.remote.dto.ai.SummarizeResponse
import com.example.kairos_mobile.data.remote.dto.ai.TagSuggestRequest
import com.example.kairos_mobile.data.remote.dto.ai.TagSuggestResponse
import com.example.kairos_mobile.data.remote.dto.insight.WebClipRequest
import com.example.kairos_mobile.data.remote.dto.insight.WebClipResponse
import kotlinx.coroutines.delay
import okhttp3.MultipartBody
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

/**
 * Mock KAIROS API 구현체
 * Phase 1에서 실제 백엔드 대신 사용
 */
@Singleton
class MockKairosApi @Inject constructor() : KairosApi {

    /**
     * Mock AI 분류
     * 간단한 키워드 기반 분류 로직
     */
    override suspend fun classifyCapture(
        request: ClassificationRequest
    ): Response<ClassificationResponse> {
        // 네트워크 지연 시뮬레이션 (500ms)
        delay(500)

        val content = request.content.lowercase()

        // 키워드 기반 분류
        val (type, path) = when {
            // 일정 관련 키워드
            content.contains("회의") ||
                    content.contains("미팅") ||
                    content.contains("약속") ||
                    content.matches(Regex(".*\\d+시.*")) ->
                Pair("SCHEDULE", "Calendar/2024/01")

            // 할 일 관련 키워드
            content.contains("해야") ||
                    content.contains("하기") ||
                    content.contains("todo") ||
                    content.contains("작업") ->
                Pair("TODO", "Tasks/Inbox")

            // 아이디어 관련 키워드
            content.contains("아이디어") ||
                    content.contains("생각") ||
                    content.contains("idea") ||
                    content.startsWith("만약") ->
                Pair("IDEA", "Ideas/Inbox")

            // 노트 (긴 텍스트)
            content.length > 100 ->
                Pair("NOTE", "Notes/Inbox")

            // 기본: 빠른 메모
            else ->
                Pair("QUICK_NOTE", "QuickNotes")
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

        val response = ClassificationResponse(
            type = type,
            destinationPath = path,
            title = title,
            tags = tags,
            confidence = confidence,
            metadata = mapOf(
                "source" to "mobile_app",
                "timestamp" to System.currentTimeMillis().toString()
            )
        )

        return Response.success(response)
    }

    /**
     * Mock Obsidian 노트 생성
     */
    override suspend fun createObsidianNote(
        request: ObsidianCreateRequest
    ): Response<ObsidianCreateResponse> {
        // 네트워크 지연 시뮬레이션 (300ms)
        delay(300)

        // 90% 성공률 시뮬레이션
        val success = Random.nextFloat() > 0.1f

        val response = if (success) {
            ObsidianCreateResponse(
                success = true,
                filePath = "${request.path}/${request.title}.md",
                message = "Note created successfully"
            )
        } else {
            ObsidianCreateResponse(
                success = false,
                filePath = null,
                message = "Failed to create note: Connection timeout"
            )
        }

        return if (success) {
            Response.success(response)
        } else {
            Response.error(
                500,
                "Server error".toResponseBody(null)
            )
        }
    }

    /**
     * Mock OCR - 이미지에서 텍스트 추출
     * 서버에서 실제 AI Vision으로 처리될 예정
     */
    override suspend fun extractTextFromImage(
        image: MultipartBody.Part
    ): Response<OcrResponse> {
        // 네트워크 지연 시뮬레이션 (2-5초)
        delay(Random.nextLong(2000, 5000))

        // Mock: 항상 샘플 텍스트 반환
        val response = OcrResponse(
            success = true,
            text = "이미지에서 추출된 샘플 텍스트입니다.\n회의 내용: 프로젝트 일정 논의\n참석자: 김철수, 이영희",
            error = null
        )

        return Response.success(response)
    }

    /**
     * Mock STT - 오디오에서 텍스트 추출
     * 서버에서 실제 AI 음성인식(Whisper/Claude)으로 처리될 예정
     */
    override suspend fun extractTextFromAudio(
        audioFile: MultipartBody.Part
    ): Response<SttResponse> {
        // 네트워크 지연 시뮬레이션 (2-5초)
        delay(Random.nextLong(2000, 5000))

        // Mock: 항상 샘플 텍스트 반환
        val response = SttResponse(
            success = true,
            text = "음성에서 추출된 샘플 텍스트입니다. 내일 오후 3시에 팀 미팅이 있습니다.",
            confidence = Random.nextFloat() * 0.15f + 0.85f,  // 0.85 ~ 1.0
            language = "ko",
            error = null
        )

        return Response.success(response)
    }

    /**
     * Mock 웹 클립 - URL에서 메타데이터 추출 및 AI 요약
     * 서버에서 크롤링 + AI 요약 처리될 예정
     */
    override suspend fun extractWebClip(
        request: WebClipRequest
    ): Response<WebClipResponse> {
        // 네트워크 지연 시뮬레이션 (5-10초)
        delay(Random.nextLong(5000, 10000))

        // Mock: URL 기반 샘플 메타데이터 반환
        val response = WebClipResponse(
            success = true,
            title = "웹 페이지 제목 - ${request.url.substringAfter("://").take(30)}",
            description = "이 페이지는 ${request.url}에서 가져온 콘텐츠입니다.",
            imageUrl = null,
            content = "AI가 요약한 웹 페이지 내용입니다. 주요 내용을 간략하게 정리했습니다.",
            error = null
        )

        return Response.success(response)
    }

    /**
     * Mock 서버 상태 확인
     */
    override suspend fun healthCheck(): Response<Unit> {
        delay(100)
        return Response.success(Unit)
    }

    // ========== Phase 3: 스마트 처리 기능 ==========

    /**
     * Mock AI 요약 생성
     * 긴 콘텐츠를 자동으로 요약
     */
    override suspend fun generateSummary(
        request: SummarizeRequest
    ): Response<SummarizeResponse> {
        // 네트워크 지연 시뮬레이션 (1-3초)
        delay(Random.nextLong(1000, 3000))

        val originalLength = request.content.length
        val maxLength = request.maxLength.coerceAtMost(200)

        // Mock: 간단한 요약 생성 (첫 문장 + 줄임)
        val summary = if (originalLength > maxLength) {
            val firstSentence = request.content.split('.', '。', '!', '?')
                .firstOrNull()?.trim() ?: request.content.take(maxLength)
            if (firstSentence.length > maxLength) {
                firstSentence.take(maxLength - 3) + "..."
            } else {
                firstSentence + " [AI 요약]"
            }
        } else {
            request.content
        }

        val response = SummarizeResponse(
            success = true,
            summary = summary,
            originalLength = originalLength,
            summaryLength = summary.length,
            error = null
        )

        return Response.success(response)
    }

    /**
     * Mock 스마트 태그 제안
     * 과거 패턴 기반 태그 자동 제안
     */
    override suspend fun suggestTags(
        request: TagSuggestRequest
    ): Response<TagSuggestResponse> {
        // 네트워크 지연 시뮬레이션 (500ms-1.5초)
        delay(Random.nextLong(500, 1500))

        val content = request.content.lowercase()
        val suggestedTags = mutableListOf<SuggestedTag>()

        // 분류 타입 기반 태그 제안
        when (request.classification) {
            "SCHEDULE" -> {
                suggestedTags.add(SuggestedTag("meeting", 0.9f, "일정 타입의 캡처"))
                suggestedTags.add(SuggestedTag("calendar", 0.85f, "과거 유사 패턴"))
            }
            "TODO" -> {
                suggestedTags.add(SuggestedTag("task", 0.9f, "할 일 타입의 캡처"))
                suggestedTags.add(SuggestedTag("action", 0.8f, "과거 유사 패턴"))
            }
            "IDEA" -> {
                suggestedTags.add(SuggestedTag("idea", 0.9f, "아이디어 타입의 캡처"))
                suggestedTags.add(SuggestedTag("brainstorm", 0.75f, "과거 유사 패턴"))
            }
        }

        // 키워드 기반 추가 태그 제안
        if (content.contains("프로젝트") || content.contains("project")) {
            suggestedTags.add(SuggestedTag("project", 0.88f, "프로젝트 관련 키워드 감지"))
        }
        if (content.contains("팀") || content.contains("team")) {
            suggestedTags.add(SuggestedTag("team", 0.85f, "팀 관련 키워드 감지"))
        }
        if (content.contains("업무") || content.contains("work")) {
            suggestedTags.add(SuggestedTag("work", 0.82f, "업무 관련 키워드 감지"))
        }
        if (content.contains("개인") || content.contains("personal")) {
            suggestedTags.add(SuggestedTag("personal", 0.8f, "개인 관련 키워드 감지"))
        }

        // 중복 제거 및 상위 N개 선택
        val distinctTags = suggestedTags
            .distinctBy { it.name }
            .sortedByDescending { it.confidence }
            .take(request.limit)

        val response = TagSuggestResponse(
            success = true,
            tags = distinctTags,
            error = null
        )

        return Response.success(response)
    }

    /**
     * 간단한 태그 추출 로직
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

        return tags.distinct().take(5) // 최대 5개, 중복 제거
    }
}
