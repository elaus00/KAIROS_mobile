package com.flit.app.presentation.calendar.components

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.flit.app.presentation.calendar.CalendarEvent
import com.flit.app.presentation.calendar.ScheduleDisplayItem
import com.flit.app.ui.theme.FlitTheme
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

/**
 * 일정 편집 BottomSheet
 * 제목, 종일, 시작/종료 시간, 장소 편집
 */
@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ScheduleEditSheet(
    schedule: ScheduleDisplayItem,
    onSave: (CalendarEvent.EditSchedule) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = FlitTheme.colors

    var title by remember { mutableStateOf(schedule.title) }
    var isAllDay by remember { mutableStateOf(schedule.isAllDay) }
    var startTimeMs by remember { mutableStateOf(schedule.startTime) }
    var endTimeMs by remember { mutableStateOf(schedule.endTime) }
    var location by remember { mutableStateOf(schedule.location ?: "") }

    var showStartDatePicker by remember { mutableStateOf(false) }
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }

    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = colors.accent,
        unfocusedBorderColor = colors.border,
        focusedTextColor = colors.text,
        unfocusedTextColor = colors.text,
        focusedLabelColor = colors.accent,
        unfocusedLabelColor = colors.textSecondary,
        cursorColor = colors.accent
    )

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colors.background,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 헤더
            Text(
                text = "일정 수정",
                color = colors.text,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )

            // 제목
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("제목") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = textFieldColors
            )

            // 종일 토글
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("종일", color = colors.text, fontSize = 15.sp)
                Switch(
                    checked = isAllDay,
                    onCheckedChange = { isAllDay = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = if (colors.isDark) colors.background else Color.White,
                        checkedTrackColor = colors.accent,
                        uncheckedThumbColor = colors.textMuted,
                        uncheckedTrackColor = colors.border
                    )
                )
            }

            // 시작/종료 시간 (종일이 아닐 때만)
            if (!isAllDay) {
                DateTimeRow(
                    label = "시작",
                    timeMs = startTimeMs,
                    onDateClick = { showStartDatePicker = true },
                    onTimeClick = { showStartTimePicker = true }
                )

                DateTimeRow(
                    label = "종료",
                    timeMs = endTimeMs,
                    onDateClick = { showEndDatePicker = true },
                    onTimeClick = { showEndTimePicker = true }
                )
            }

            // 장소
            OutlinedTextField(
                value = location,
                onValueChange = { location = it },
                label = { Text("장소") },
                singleLine = true,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.LocationOn,
                        contentDescription = null,
                        tint = colors.textSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                colors = textFieldColors
            )

            // 저장 버튼
            Button(
                onClick = {
                    onSave(
                        CalendarEvent.EditSchedule(
                            scheduleId = schedule.scheduleId,
                            title = title,
                            startTime = startTimeMs,
                            endTime = endTimeMs,
                            location = location.ifBlank { null },
                            isAllDay = isAllDay
                        )
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.accent,
                    contentColor = if (colors.isDark) colors.background else Color.White
                ),
                enabled = title.isNotBlank()
            ) {
                Text("저장", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
            }
        }
    }

    // 시작 날짜 선택
    if (showStartDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = startTimeMs
        )
        DatePickerDialog(
            onDismissRequest = { showStartDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { selectedMs ->
                        startTimeMs = mergeDateTime(selectedMs, startTimeMs)
                    }
                    showStartDatePicker = false
                }) { Text("확인", color = colors.accent) }
            },
            dismissButton = {
                TextButton(onClick = { showStartDatePicker = false }) {
                    Text("취소", color = colors.textSecondary)
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // 시작 시간 선택
    if (showStartTimePicker) {
        val zone = ZoneId.systemDefault()
        val initialHour = startTimeMs?.let {
            Instant.ofEpochMilli(it).atZone(zone).hour
        } ?: 9
        val initialMinute = startTimeMs?.let {
            Instant.ofEpochMilli(it).atZone(zone).minute
        } ?: 0
        val timePickerState = rememberTimePickerState(
            initialHour = initialHour,
            initialMinute = initialMinute,
            is24Hour = true
        )
        AlertDialog(
            onDismissRequest = { showStartTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    startTimeMs = mergeTime(startTimeMs, timePickerState.hour, timePickerState.minute)
                    showStartTimePicker = false
                }) { Text("확인", color = colors.accent) }
            },
            dismissButton = {
                TextButton(onClick = { showStartTimePicker = false }) {
                    Text("취소", color = colors.textSecondary)
                }
            },
            text = { TimePicker(state = timePickerState) }
        )
    }

    // 종료 날짜 선택
    if (showEndDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = endTimeMs ?: startTimeMs
        )
        DatePickerDialog(
            onDismissRequest = { showEndDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { selectedMs ->
                        endTimeMs = mergeDateTime(selectedMs, endTimeMs ?: startTimeMs)
                    }
                    showEndDatePicker = false
                }) { Text("확인", color = colors.accent) }
            },
            dismissButton = {
                TextButton(onClick = { showEndDatePicker = false }) {
                    Text("취소", color = colors.textSecondary)
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // 종료 시간 선택
    if (showEndTimePicker) {
        val zone = ZoneId.systemDefault()
        val initialHour = (endTimeMs ?: startTimeMs)?.let {
            Instant.ofEpochMilli(it).atZone(zone).hour
        } ?: 10
        val initialMinute = (endTimeMs ?: startTimeMs)?.let {
            Instant.ofEpochMilli(it).atZone(zone).minute
        } ?: 0
        val timePickerState = rememberTimePickerState(
            initialHour = initialHour,
            initialMinute = initialMinute,
            is24Hour = true
        )
        AlertDialog(
            onDismissRequest = { showEndTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    endTimeMs = mergeTime(endTimeMs ?: startTimeMs, timePickerState.hour, timePickerState.minute)
                    showEndTimePicker = false
                }) { Text("확인", color = colors.accent) }
            },
            dismissButton = {
                TextButton(onClick = { showEndTimePicker = false }) {
                    Text("취소", color = colors.textSecondary)
                }
            },
            text = { TimePicker(state = timePickerState) }
        )
    }
}

/**
 * 날짜+시간 행 (DatePicker/TimePicker 연동)
 */
@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun DateTimeRow(
    label: String,
    timeMs: Long?,
    onDateClick: () -> Unit,
    onTimeClick: () -> Unit
) {
    val colors = FlitTheme.colors
    val zone = ZoneId.systemDefault()

    val dateText = timeMs?.let {
        Instant.ofEpochMilli(it).atZone(zone).toLocalDate()
            .format(DateTimeFormatter.ofPattern("yyyy.MM.dd"))
    } ?: "날짜 선택"

    val timeText = timeMs?.let {
        Instant.ofEpochMilli(it).atZone(zone).toLocalTime()
            .format(DateTimeFormatter.ofPattern("HH:mm"))
    } ?: "시간 선택"

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = colors.textSecondary,
            fontSize = 14.sp,
            modifier = Modifier.width(40.dp)
        )

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(colors.chipBg)
                .clickable { onDateClick() }
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Text(dateText, color = colors.text, fontSize = 14.sp)
        }

        Spacer(modifier = Modifier.width(8.dp))

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(colors.chipBg)
                .clickable { onTimeClick() }
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Text(timeText, color = colors.text, fontSize = 14.sp)
        }
    }
}

/**
 * 선택된 날짜(DatePicker)와 기존 시간을 합쳐서 epoch ms 반환
 */
@RequiresApi(Build.VERSION_CODES.O)
private fun mergeDateTime(selectedDateMs: Long, existingTimeMs: Long?): Long {
    val zone = ZoneId.systemDefault()
    val newDate = Instant.ofEpochMilli(selectedDateMs).atZone(zone).toLocalDate()
    val existingTime = existingTimeMs?.let {
        Instant.ofEpochMilli(it).atZone(zone).toLocalTime()
    } ?: LocalTime.of(9, 0)
    return ZonedDateTime.of(newDate, existingTime, zone).toInstant().toEpochMilli()
}

/**
 * 기존 날짜에 선택된 시간(TimePicker)을 합쳐서 epoch ms 반환
 */
@RequiresApi(Build.VERSION_CODES.O)
private fun mergeTime(existingMs: Long?, hour: Int, minute: Int): Long {
    val zone = ZoneId.systemDefault()
    val existingDate = existingMs?.let {
        Instant.ofEpochMilli(it).atZone(zone).toLocalDate()
    } ?: LocalDate.now()
    val newTime = LocalTime.of(hour, minute)
    return ZonedDateTime.of(existingDate, newTime, zone).toInstant().toEpochMilli()
}
