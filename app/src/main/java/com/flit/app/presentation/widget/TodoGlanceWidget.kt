package com.flit.app.presentation.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.ColorFilter
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextDecoration
import androidx.glance.text.TextStyle
import com.flit.app.MainActivity
import com.flit.app.R
import com.flit.app.data.local.database.dao.TodoDao
import com.flit.app.data.local.database.dao.TodoWithCaptureRow
import com.flit.app.domain.usecase.todo.ToggleTodoCompletionUseCase
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * 할 일 홈 화면 위젯 (Glance)
 * - 오늘 마감 할 일 표시 (완료 포함, 미완료 우선)
 * - 헤더에 오늘 날짜 + 바로가기 버튼
 * - 체크 토글: 완료 아이콘 + 취소선, 미완료 빈 원
 * - GlanceTheme 자동 다크/라이트 대응
 */
class TodoGlanceWidget : GlanceAppWidget() {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface TodoWidgetEntryPoint {
        fun todoDao(): TodoDao
        fun toggleTodoCompletionUseCase(): ToggleTodoCompletionUseCase
    }

    companion object {
        const val DISPLAY_LIMIT = 5
        val TodoIdKey = ActionParameters.Key<String>("todo_id")
    }

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val (items, totalCount) = loadData(context)
        val todayLabel = SimpleDateFormat("M월 d일 (E)", Locale.KOREAN)
            .format(Date())

        provideContent {
            GlanceTheme {
                TodoContent(items, totalCount, todayLabel)
            }
        }
    }

    private suspend fun loadData(context: Context): Pair<List<TodoWithCaptureRow>, Int> {
        return try {
            val entryPoint = EntryPointAccessors.fromApplication(
                context.applicationContext,
                TodoWidgetEntryPoint::class.java
            )
            val todoDao = entryPoint.todoDao()
            val todayEnd = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
                set(Calendar.MILLISECOND, 999)
            }.timeInMillis
            val items = todoDao.getTodayTodosForWidget(todayEnd)
            val total = todoDao.getTodayTodoCountForWidget(todayEnd)
            items to total
        } catch (e: Exception) {
            emptyList<TodoWithCaptureRow>() to 0
        }
    }
}

/**
 * 할 일 체크 토글 액션
 */
class ToggleTodoAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val todoId = parameters[TodoGlanceWidget.TodoIdKey] ?: return
        val entryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext,
            TodoGlanceWidget.TodoWidgetEntryPoint::class.java
        )
        entryPoint.toggleTodoCompletionUseCase()(todoId)
        TodoGlanceWidget().update(context, glanceId)
    }
}

@Composable
private fun TodoContent(
    items: List<TodoWithCaptureRow>,
    totalCount: Int,
    todayLabel: String
) {
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(GlanceTheme.colors.background)
            .cornerRadius(16.dp)
            .padding(16.dp)
    ) {
        // 헤더: 제목 + 날짜 + 바로가기
        Row(
            modifier = GlanceModifier.fillMaxWidth().padding(bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "오늘 할 일",
                style = TextStyle(
                    fontSize = 20.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = GlanceTheme.colors.onBackground
                )
            )
            Spacer(modifier = GlanceModifier.width(8.dp))
            Text(
                text = todayLabel,
                style = TextStyle(
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Normal,
                    color = GlanceTheme.colors.onSurfaceVariant
                )
            )
            Spacer(modifier = GlanceModifier.defaultWeight())
            // 바로가기 버튼 (우측 상단)
            Text(
                text = "바로가기",
                style = TextStyle(
                    fontSize = 12.5.sp,
                    color = GlanceTheme.colors.primary
                ),
                modifier = GlanceModifier
                    .padding(vertical = 4.dp)
                    .clickable(onClick = actionStartActivity<MainActivity>())
            )
        }

        // 할 일 리스트 또는 빈 상태
        if (items.isEmpty()) {
            Box(
                modifier = GlanceModifier.fillMaxWidth().defaultWeight(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "할 일을 모두 완료했어요!",
                    style = TextStyle(
                        fontSize = 16.5.sp,
                        color = GlanceTheme.colors.onSurfaceVariant
                    )
                )
            }
        } else {
            val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            LazyColumn(modifier = GlanceModifier.fillMaxWidth().defaultWeight()) {
                items(items, itemId = { it.todoId.hashCode().toLong() }) { item ->
                    TodoItemRow(item, timeFormat)
                }
                // 오버플로우 표시
                if (totalCount > TodoGlanceWidget.DISPLAY_LIMIT) {
                    val overflow = totalCount - TodoGlanceWidget.DISPLAY_LIMIT
                    item(itemId = -1L) {
                        Box(
                            modifier = GlanceModifier.fillMaxWidth().padding(vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "+${overflow}개 더 있음",
                                style = TextStyle(
                                    fontSize = 12.5.sp,
                                    color = GlanceTheme.colors.onSurfaceVariant
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TodoItemRow(item: TodoWithCaptureRow, timeFormat: SimpleDateFormat) {
    val title = item.aiTitle?.takeIf { it.isNotBlank() } ?: item.originalText
    val isOverdue = !item.isCompleted && item.deadline != null && item.deadline < System.currentTimeMillis()
    val textColor = if (item.isCompleted) GlanceTheme.colors.outline else GlanceTheme.colors.onSurface
    val textDecoration = if (item.isCompleted) TextDecoration.LineThrough else TextDecoration.None

    Row(
        modifier = GlanceModifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 체크 아이콘 (44dp 터치 타겟 확보)
        Box(
            modifier = GlanceModifier
                .size(44.dp)
                .clickable(
                    onClick = actionRunCallback<ToggleTodoAction>(
                        actionParametersOf(TodoGlanceWidget.TodoIdKey to item.todoId)
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Image(
                provider = ImageProvider(
                    if (item.isCompleted) R.drawable.ic_widget_check_circle
                    else R.drawable.ic_widget_circle_outline
                ),
                contentDescription = if (item.isCompleted) "완료됨" else "미완료",
                modifier = GlanceModifier.size(24.dp),
                colorFilter = ColorFilter.tint(
                    if (item.isCompleted) GlanceTheme.colors.outline
                    else GlanceTheme.colors.onSurfaceVariant
                )
            )
        }
        Spacer(modifier = GlanceModifier.width(4.dp))

        // 제목 (탭 → 앱에서 상세보기)
        Text(
            text = title,
            style = TextStyle(
                fontSize = 16.5.sp,
                color = textColor,
                textDecoration = textDecoration
            ),
            maxLines = 1,
            modifier = GlanceModifier.defaultWeight().clickable(
                onClick = actionStartActivity<MainActivity>(
                    parameters = actionParametersOf(
                        ActionParameters.Key<String>("navigate_to_capture_id") to item.captureId
                    )
                )
            )
        )

        // 마감 시간 + 지남 표시 (색상 + 텍스트로 이중 표시)
        if (item.deadline != null) {
            Spacer(modifier = GlanceModifier.width(8.dp))
            Text(
                text = if (isOverdue) {
                    "${timeFormat.format(Date(item.deadline))} 지남"
                } else {
                    timeFormat.format(Date(item.deadline))
                },
                style = TextStyle(
                    fontSize = 12.5.sp,
                    fontWeight = if (isOverdue) FontWeight.Bold else FontWeight.Normal,
                    color = when {
                        item.isCompleted -> GlanceTheme.colors.outline
                        isOverdue -> GlanceTheme.colors.error
                        else -> GlanceTheme.colors.onSurfaceVariant
                    }
                )
            )
        }
    }
}

/**
 * 할 일 위젯 리시버 (Manifest에 등록)
 */
class TodoWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = TodoGlanceWidget()
}
