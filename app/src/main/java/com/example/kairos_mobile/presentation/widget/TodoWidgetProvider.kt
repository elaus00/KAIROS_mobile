package com.example.kairos_mobile.presentation.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.example.kairos_mobile.MainActivity
import com.example.kairos_mobile.R
import com.example.kairos_mobile.data.local.database.dao.TodoDao
import com.example.kairos_mobile.data.local.database.dao.TodoWithCaptureRow
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.runBlocking
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * 할 일 위젯 (4x2)
 * 오늘 마감 미완료 할 일 표시 + 체크박스 완료 토글
 */
class TodoWidgetProvider : AppWidgetProvider() {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface TodoWidgetEntryPoint {
        fun todoDao(): TodoDao
    }

    companion object {
        const val ACTION_TOGGLE_TODO = "com.example.kairos_mobile.ACTION_TOGGLE_TODO"
        const val EXTRA_TODO_ID = "extra_todo_id"
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_TOGGLE_TODO) {
            val todoId = intent.getStringExtra(EXTRA_TODO_ID) ?: return
            val entryPoint = EntryPointAccessors.fromApplication(
                context.applicationContext,
                TodoWidgetEntryPoint::class.java
            )
            val todoDao = entryPoint.todoDao()
            val now = System.currentTimeMillis()
            // 위젯에서 Room 접근 시 runBlocking 사용 (위젯 특성)
            runBlocking {
                todoDao.toggleCompletion(todoId, now, now)
            }
            // 위젯 갱신
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val widgetIds = appWidgetManager.getAppWidgetIds(
                intent.component ?: return
            )
            // 전체 위젯 갱신 알림
            appWidgetManager.notifyAppWidgetViewDataChanged(widgetIds, R.id.widget_todo_list)
            for (id in widgetIds) {
                updateWidget(context, appWidgetManager, id)
            }
        }
    }

    private fun updateWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        val views = RemoteViews(context.packageName, R.layout.widget_todo)

        // 리스트 어댑터 설정
        val serviceIntent = Intent(context, TodoWidgetService::class.java).apply {
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        }
        views.setRemoteAdapter(R.id.widget_todo_list, serviceIntent)
        views.setEmptyView(R.id.widget_todo_list, R.id.widget_todo_empty)

        // 리스트 아이템 클릭 → 완료 토글 PendingIntent 템플릿
        val toggleIntent = Intent(context, TodoWidgetProvider::class.java).apply {
            action = ACTION_TOGGLE_TODO
        }
        val togglePendingIntent = PendingIntent.getBroadcast(
            context, 0, toggleIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )
        views.setPendingIntentTemplate(R.id.widget_todo_list, togglePendingIntent)

        // 앱 열기 버튼
        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            context, appWidgetId, openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_todo_open_app, openAppPendingIntent)

        appWidgetManager.updateAppWidget(appWidgetId, views)
    }
}

/**
 * 할 일 위젯 RemoteViewsService
 */
class TodoWidgetService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        return TodoWidgetViewsFactory(applicationContext)
    }
}

/**
 * 할 일 위젯 RemoteViewsFactory
 * Room에서 오늘 마감 할 일 조회하여 리스트 뷰 구성
 */
private class TodoWidgetViewsFactory(
    private val context: Context
) : RemoteViewsService.RemoteViewsFactory {

    private var items: List<TodoWithCaptureRow> = emptyList()
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    override fun onCreate() {}

    override fun onDataSetChanged() {
        val entryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext,
            TodoWidgetProvider.TodoWidgetEntryPoint::class.java
        )
        val todoDao = entryPoint.todoDao()

        // 오늘 23:59:59 까지의 마감 할 일 조회
        val todayEnd = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.timeInMillis

        items = runBlocking {
            todoDao.getTodayIncompleteTodos(todayEnd)
        }
    }

    override fun onDestroy() {
        items = emptyList()
    }

    override fun getCount(): Int = items.size

    override fun getViewAt(position: Int): RemoteViews {
        val item = items[position]
        val views = RemoteViews(context.packageName, R.layout.widget_todo_item)

        // 제목 설정 (aiTitle 우선, 없으면 originalText)
        val title = item.aiTitle?.takeIf { it.isNotBlank() } ?: item.originalText
        views.setTextViewText(R.id.widget_todo_item_title, title)

        // 마감 시간 표시
        if (item.deadline != null) {
            views.setTextViewText(
                R.id.widget_todo_item_deadline,
                timeFormat.format(Date(item.deadline))
            )
            views.setViewVisibility(R.id.widget_todo_item_deadline, View.VISIBLE)
        } else {
            views.setViewVisibility(R.id.widget_todo_item_deadline, View.GONE)
        }

        // 체크박스 클릭 시 완료 토글 fill-in intent
        val fillInIntent = Intent().apply {
            putExtra(TodoWidgetProvider.EXTRA_TODO_ID, item.todoId)
        }
        views.setOnClickFillInIntent(R.id.widget_todo_checkbox, fillInIntent)

        return views
    }

    override fun getLoadingView(): RemoteViews? = null

    override fun getViewTypeCount(): Int = 1

    override fun getItemId(position: Int): Long = items[position].todoId.hashCode().toLong()

    override fun hasStableIds(): Boolean = true
}
