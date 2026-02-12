package com.example.kairos_mobile.domain.model

sealed class CalendarException(message: String) : RuntimeException(message) {
    class PermissionDenied(message: String = "캘린더 권한이 필요합니다.") : CalendarException(message)
    class NoCalendarSelected(message: String = "대상 캘린더를 선택해주세요.") : CalendarException(message)
    class InsertFailed(message: String = "캘린더 일정 추가에 실패했습니다.") : CalendarException(message)
    class Unknown(message: String = "캘린더 요청에 실패했습니다.") : CalendarException(message)
}
