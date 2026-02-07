package com.example.kairos_mobile.domain.model

sealed class CalendarApiException(message: String) : RuntimeException(message) {
    class GoogleAuthRequired(message: String = "Google Calendar 연결이 필요합니다.") : CalendarApiException(message)
    class GoogleTokenExpired(message: String = "Google Calendar 토큰이 만료되었습니다.") : CalendarApiException(message)
    class GoogleApiError(message: String = "Google Calendar API 오류가 발생했습니다.") : CalendarApiException(message)
    class Unknown(message: String = "캘린더 요청에 실패했습니다.") : CalendarApiException(message)
}
