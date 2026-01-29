package com.example.kairos_mobile.domain.util

import com.example.kairos_mobile.domain.model.CaptureType

/**
 * 키워드 매칭 유틸리티
 *
 * 입력된 텍스트에서 특정 키워드를 감지하여
 * 적합한 CaptureType을 추천
 */
object KeywordMatcher {

    /**
     * 각 타입별 키워드 맵
     * 한글과 영어 키워드 모두 지원
     */
    private val keywordMap = mapOf(
        CaptureType.IDEA to listOf(
            // 한글
            "아이디어", "생각", "떠올랐다", "떠올", "개선", "제안",
            "좋을 것 같", "어떨까", "생각해봤는데", "어떨까요",
            // 영어
            "idea", "suggestion", "improvement", "think", "thought"
        ),
        CaptureType.TODO to listOf(
            // 한글
            "해야", "해야 할", "해야지", "해야함", "해야한다",
            "작업", "과제", "완료", "처리", "하기", "할 일",
            "끝내", "마무리", "진행",
            // 일정 관련도 TODO로 분류
            "회의", "미팅", "일정", "약속", "만남", "예정", "모임",
            // 영어
            "todo", "task", "do", "finish", "complete", "work on",
            "meeting", "appointment", "schedule", "event"
        ),
        CaptureType.NOTE to listOf(
            // 한글
            "메모", "기록", "저장", "보관", "남기", "적어",
            "참고", "노트", "정리",
            // 영어
            "note", "save", "record", "remember", "memo"
        ),
        CaptureType.QUICK_NOTE to listOf(
            // 한글
            "빠른", "간단히", "잠깐",
            // 영어
            "quick", "brief"
        ),
        CaptureType.CLIP to listOf(
            // 한글
            "링크", "웹", "사이트", "클립",
            // 영어
            "link", "web", "url", "clip", "http"
        )
    )

    /**
     * 텍스트에서 매칭되는 CaptureType 찾기
     *
     * @param text 분석할 텍스트
     * @return 매칭된 CaptureType 리스트 (최대 3개, 우선순위 순)
     */
    fun matchTypes(text: String): List<CaptureType> {
        // 최소 2글자 이상이어야 매칭
        if (text.length < 2) return emptyList()

        val lowerText = text.lowercase()

        // 각 타입별 매칭된 키워드 개수 계산
        val matchCounts = keywordMap.entries
            .map { (type, keywords) ->
                val matchCount = keywords.count { keyword ->
                    lowerText.contains(keyword.lowercase())
                }
                type to matchCount
            }
            .filter { (_, count) -> count > 0 }  // 매칭된 것만
            .sortedByDescending { (_, count) -> count }  // 많이 매칭된 순
            .map { (type, _) -> type }
            .take(3)  // 최대 3개

        return matchCounts
    }

    /**
     * 특정 타입이 텍스트와 매칭되는지 확인
     *
     * @param text 분석할 텍스트
     * @param type 확인할 CaptureType
     * @return 매칭 여부
     */
    fun matchesType(text: String, type: CaptureType): Boolean {
        if (text.length < 2) return false

        val lowerText = text.lowercase()
        val keywords = keywordMap[type] ?: return false

        return keywords.any { keyword ->
            lowerText.contains(keyword.lowercase())
        }
    }

    /**
     * 텍스트에서 가장 강하게 매칭되는 타입 반환
     *
     * @param text 분석할 텍스트
     * @return 가장 강하게 매칭된 CaptureType (없으면 null)
     */
    fun getBestMatch(text: String): CaptureType? {
        return matchTypes(text).firstOrNull()
    }
}
