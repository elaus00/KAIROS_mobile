package com.flit.app.presentation.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.ColorFilter
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.updateAll
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
import androidx.glance.unit.ColorProvider
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextDecoration
import androidx.glance.text.TextStyle
import com.flit.app.BuildConfig
import com.flit.app.MainActivity
import com.flit.app.R
import com.flit.app.data.local.database.dao.TodoDao
import com.flit.app.data.local.database.dao.TodoWithCaptureRow
import com.flit.app.data.local.database.entities.TodoEntity
import com.flit.app.domain.repository.UserPreferenceRepository
import com.flit.app.domain.usecase.todo.ToggleTodoCompletionUseCase
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.atomic.AtomicInteger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.system.measureTimeMillis

/**
 * 할 일 홈 화면 위젯 (Glance)
 * - 오늘 마감 할 일 표시 (완료 포함, 미완료 우선)
 * - 헤더에 오늘 날짜 표시
 * - 체크 토글: 완료 아이콘 + 취소선, 미완료 빈 원
 * - 앱 테마 설정(SYSTEM/LIGHT/DARK) 반영
 */
class TodoGlanceWidget : GlanceAppWidget() {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface TodoWidgetEntryPoint {
        fun todoDao(): TodoDao
        fun toggleTodoCompletionUseCase(): ToggleTodoCompletionUseCase
        fun userPreferenceRepository(): UserPreferenceRepository
    }

    companion object {
        val TodoIdKey = ActionParameters.Key<String>("todo_id")
    }

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val traceId = newTraceId("provide")
        val startedAt = SystemClock.elapsedRealtime()
        logDebug("[$traceId] provideGlance start glanceId=$id thread=${Thread.currentThread().name}")
        val entryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext,
            TodoWidgetEntryPoint::class.java
        )
        val isDark = resolveWidgetDarkTheme(context, entryPoint.userPreferenceRepository())
        val todayEnd = calculateTodayEndMillis()
        logDebug("[$traceId] provideGlance binding flow todayEnd=$todayEnd")
        val todayLabel = SimpleDateFormat("M월 d일 (E)", Locale.KOREAN)
            .format(Date())

        provideContent {
            val rowsFlow = remember(todayEnd) {
                entryPoint.todoDao().observeTodayTodosForWidget(todayEnd)
            }
            val items by rowsFlow.collectAsState(initial = emptyList())
            LaunchedEffect(items) {
                logDebug("[$traceId] flowEmission count=${items.size}, rows=${items.toWidgetSnapshot()}")
            }

            TodoContent(
                items = items,
                todayLabel = todayLabel,
                isDark = isDark
            )
        }
        logDebug("[$traceId] provideGlance end durationMs=${SystemClock.elapsedRealtime() - startedAt}")
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
        val traceId = newTraceId("toggle")
        val actionStartedAt = SystemClock.elapsedRealtime()
        val todoId = parameters[TodoGlanceWidget.TodoIdKey]
        if (todoId == null) {
            Log.e(TODO_WIDGET_TAG, "[$traceId] missing todo_id action parameter")
            return
        }
        val activeCount = ToggleTraceState.activeToggleCount.incrementAndGet()
        logDebug(
            "[$traceId] onAction start todoId=$todoId glanceId=$glanceId activeToggleCount=$activeCount " +
                "thread=${Thread.currentThread().name}"
        )

        val entryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext,
            TodoGlanceWidget.TodoWidgetEntryPoint::class.java
        )

        try {
            val todoDao = entryPoint.todoDao()
            val todayEnd = calculateTodayEndMillis()
            val beforeEntity = withContext(Dispatchers.IO) {
                todoDao.getById(todoId)
            }
            val beforeWidgetRows = withContext(Dispatchers.IO) {
                todoDao.getTodayTodosForWidget(todayEnd)
            }
            logDebug(
                "[$traceId] beforeToggle todo=${beforeEntity.toEntitySnapshot()}, " +
                    "todayRows=${beforeWidgetRows.toWidgetSnapshot()}"
            )

            runCatching {
                withContext(Dispatchers.IO) {
                    entryPoint.toggleTodoCompletionUseCase()(todoId, trackEvent = false)
                }
            }.onSuccess {
                val afterEntity = withContext(Dispatchers.IO) {
                    todoDao.getById(todoId)
                }
                val afterWidgetRows = withContext(Dispatchers.IO) {
                    todoDao.getTodayTodosForWidget(todayEnd)
                }
                logDebug(
                    "[$traceId] afterToggle todo=${afterEntity.toEntitySnapshot()}, " +
                        "todayRows=${afterWidgetRows.toWidgetSnapshot()}"
                )

                val glanceIds = runCatching {
                    GlanceAppWidgetManager(context).getGlanceIds(TodoGlanceWidget::class.java)
                }.getOrElse { e ->
                    Log.e(TODO_WIDGET_TAG, "[$traceId] failed to get glance ids", e)
                    emptyList()
                }
                val appWidgetIds = runCatching {
                    AppWidgetManager.getInstance(context).getAppWidgetIds(
                        ComponentName(context, TodoWidgetReceiver::class.java)
                    ).toList()
                }.getOrElse { e ->
                    Log.e(TODO_WIDGET_TAG, "[$traceId] failed to get appWidget ids", e)
                    emptyList()
                }
                logDebug(
                    "[$traceId] updateTargets glanceIds=${glanceIds.joinToString()}, " +
                        "appWidgetIds=${appWidgetIds.joinToString()}"
                )

                val elapsed = measureTimeMillis {
                    TodoGlanceWidget().updateAll(context)
                }
                logDebug("[$traceId] updateAll completed durationMs=$elapsed")
            }.onFailure { e ->
                Log.e(TODO_WIDGET_TAG, "[$traceId] toggle failed for todoId=$todoId", e)
            }
        } finally {
            val remained = ToggleTraceState.activeToggleCount.decrementAndGet()
            logDebug(
                "[$traceId] onAction end elapsedMs=${SystemClock.elapsedRealtime() - actionStartedAt} " +
                    "activeToggleCount=$remained"
            )
        }
    }
}

@Composable
private fun TodoContent(
    items: List<TodoWithCaptureRow>,
    todayLabel: String,
    isDark: Boolean
) {
    val colors = widgetColors(isDark)

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(colors.background)
            .cornerRadius(16.dp)
    ) {
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // 헤더: 제목 + 날짜
            Row(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
                    .clickable(
                        onClick = actionStartActivity<MainActivity>(
                            parameters = actionParametersOf(
                                ActionParameters.Key<String>("navigate_to_tab") to "calendar"
                            )
                        )
                    ),
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = "오늘 할 일",
                    style = TextStyle(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.onBackground
                    )
                )
                Spacer(modifier = GlanceModifier.width(8.dp))
                Text(
                    text = todayLabel,
                    style = TextStyle(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal,
                        color = colors.onSurfaceVariant
                    )
                )
            }

            // 할 일 리스트 또는 빈 상태
            if (items.isEmpty()) {
                Box(
                    modifier = GlanceModifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "할 일을 모두 완료했어요!",
                        style = TextStyle(
                            fontSize = 16.5.sp,
                            color = colors.onSurfaceVariant
                        )
                    )
                }
            } else {
                val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                LazyColumn(modifier = GlanceModifier.fillMaxSize()) {
                    // itemId에 완료 상태 포함: isCompleted 변경 시 Glance가 새 아이템으로 인식하도록
                    items(items, itemId = { "${it.todoId}_${it.isCompleted}".hashCode().toLong() }) { item ->
                        TodoItemRow(
                            item = item,
                            timeFormat = timeFormat,
                            colors = colors
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TodoItemRow(
    item: TodoWithCaptureRow,
    timeFormat: SimpleDateFormat,
    colors: TodoWidgetColors
) {
    val title = item.aiTitle?.takeIf { it.isNotBlank() } ?: item.originalText
    val isOverdue = !item.isCompleted && item.deadline != null && item.deadline < System.currentTimeMillis()
    val textColor = if (item.isCompleted) colors.outline else colors.onSurface
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
                    if (item.isCompleted) colors.outline
                    else colors.onSurfaceVariant
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
            modifier = GlanceModifier.clickable(
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
                        item.isCompleted -> colors.outline
                        isOverdue -> colors.error
                        else -> colors.onSurfaceVariant
                    }
                )
            )
        }
    }
}

private data class TodoWidgetColors(
    val background: ColorProvider,
    val primary: ColorProvider,
    val onBackground: ColorProvider,
    val onSurface: ColorProvider,
    val onSurfaceVariant: ColorProvider,
    val outline: ColorProvider,
    val error: ColorProvider
)

private fun widgetColors(isDark: Boolean): TodoWidgetColors {
    return if (isDark) {
        TodoWidgetColors(
            background = fixedColor(Color(0xFF111111)),
            primary = fixedColor(Color(0xFFF0F0F0)),
            onBackground = fixedColor(Color(0xFFECECEC)),
            onSurface = fixedColor(Color(0xFFF5F5F5)),
            onSurfaceVariant = fixedColor(Color(0xFFBDBDBD)),
            outline = fixedColor(Color(0xFF8A8A8A)),
            error = fixedColor(Color(0xFFFF8A80))
        )
    } else {
        TodoWidgetColors(
            background = fixedColor(Color(0xFFFFFFFF)),
            primary = fixedColor(Color(0xFF1A1A1A)),
            onBackground = fixedColor(Color(0xFF111111)),
            onSurface = fixedColor(Color(0xFF212121)),
            onSurfaceVariant = fixedColor(Color(0xFF616161)),
            outline = fixedColor(Color(0xFF9E9E9E)),
            error = fixedColor(Color(0xFFC62828))
        )
    }
}

private fun fixedColor(color: Color): ColorProvider =
    ColorProvider(color)

/**
 * 할 일 위젯 리시버 (Manifest에 등록)
 */
class TodoWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = TodoGlanceWidget()

    override fun onReceive(context: Context, intent: Intent) {
        val appWidgetIds = intent.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS)?.toList()
            ?: emptyList()
        val extraKeys = intent.extras?.keySet()?.joinToString().orEmpty()
        logDebug(
            "[receiver] onReceive action=${intent.action}, appWidgetIds=$appWidgetIds, " +
                "extraKeys=$extraKeys"
        )
        super.onReceive(context, intent)
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        logDebug("[receiver] onUpdate appWidgetIds=${appWidgetIds.toList()}")
        super.onUpdate(context, appWidgetManager, appWidgetIds)
    }

    override fun onEnabled(context: Context) {
        logDebug("[receiver] onEnabled")
        super.onEnabled(context)
    }

    override fun onDisabled(context: Context) {
        logDebug("[receiver] onDisabled")
        super.onDisabled(context)
    }
}

private const val TODO_WIDGET_TAG = "TodoWidgetTrace"
private const val MAX_WIDGET_LOG_ITEMS = 12

private object ToggleTraceState {
    val activeToggleCount = AtomicInteger(0)
}

private fun newTraceId(prefix: String): String =
    "$prefix-${SystemClock.elapsedRealtimeNanos().toString(16)}"

private fun calculateTodayEndMillis(): Long {
    return Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 23)
        set(Calendar.MINUTE, 59)
        set(Calendar.SECOND, 59)
        set(Calendar.MILLISECOND, 999)
    }.timeInMillis
}

private fun List<TodoWithCaptureRow>.toWidgetSnapshot(limit: Int = MAX_WIDGET_LOG_ITEMS): String {
    if (isEmpty()) return "[]"
    val shown = take(limit).joinToString(" | ") { row ->
        val itemId = "${row.todoId}_${row.isCompleted}".hashCode().toLong()
        "todoId=${row.todoId},done=${row.isCompleted},deadline=${row.deadline},itemId=$itemId"
    }
    return if (size > limit) {
        "[$shown | ... +${size - limit}개]"
    } else {
        "[$shown]"
    }
}

private fun TodoEntity?.toEntitySnapshot(): String {
    if (this == null) return "null"
    return "id=$id,done=$isCompleted,deadline=$deadline,completedAt=$completedAt,updatedAt=$updatedAt"
}

private fun logDebug(message: String) {
    if (BuildConfig.DEBUG) {
        Log.d(TODO_WIDGET_TAG, message)
    }
}
