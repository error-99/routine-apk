package com.studentapp.isu14.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.RemoteViews
import com.studentapp.isu14.MainActivity
import com.studentapp.isu14.R
import com.studentapp.isu14.data.database.AppDatabase
import com.studentapp.isu14.data.model.RoutineEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

class RoutineWidgetProvider : AppWidgetProvider() {

    private val widgetScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    companion object {
        const val ACTION_SHOW_TODAY = "com.studentapp.isu14.widget.ACTION_SHOW_TODAY"
        const val ACTION_SHOW_TOMORROW = "com.studentapp.isu14.widget.ACTION_SHOW_TOMORROW"
        const val ACTION_REFRESH = "com.studentapp.isu14.widget.ACTION_REFRESH"
        private const val PREFS_NAME = "RoutineWidgetPrefs"
        private const val KEY_TAB_PREFIX = "tab_widget_"

        fun updateAllWidgets(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val componentName = ComponentName(context, RoutineWidgetProvider::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
            val intent = Intent(context, RoutineWidgetProvider::class.java).apply {
                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds)
            }
            context.sendBroadcast(intent)
        }
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
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val appWidgetId = intent.getIntExtra(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        )

        when (intent.action) {
            ACTION_SHOW_TODAY -> {
                saveSelectedTab(context, appWidgetId, "today")
                if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                    updateWidget(context, appWidgetManager, appWidgetId)
                } else {
                    updateAllWidgets(context)
                }
            }
            ACTION_SHOW_TOMORROW -> {
                saveSelectedTab(context, appWidgetId, "tomorrow")
                if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                    updateWidget(context, appWidgetManager, appWidgetId)
                } else {
                    updateAllWidgets(context)
                }
            }
            ACTION_REFRESH -> {
                if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                    updateWidget(context, appWidgetManager, appWidgetId)
                } else {
                    updateAllWidgets(context)
                }
            }
        }
    }

    private fun updateWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        widgetScope.launch {
            val selectedTab = getSelectedTab(context, appWidgetId)
            val isTomorrow = selectedTab == "tomorrow"

            val targetDate = if (isTomorrow) LocalDate.now().plusDays(1) else LocalDate.now()
            val dayOfWeek = targetDate.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.ENGLISH)
            val formattedDate = targetDate.format(DateTimeFormatter.ofPattern("EEE, MMM d"))

            val titlePrefix = if (isTomorrow) "Tomorrow" else "Today"
            val dayLabel = "$titlePrefix • $formattedDate"

            // Query routines from Room
            val db = AppDatabase.getDatabase(context, widgetScope)
            val dao = db.appDao()

            val student = dao.getFirstStudent()
            val routines: List<RoutineEntity> = if (student != null) {
                val studentRoutines = dao.getStudentRoutinesForDay(student.studentId, dayOfWeek)
                if (studentRoutines.isNotEmpty()) studentRoutines else dao.getAllRoutinesForDay(dayOfWeek)
            } else {
                dao.getAllRoutinesForDay(dayOfWeek)
            }

            val views = RemoteViews(context.packageName, R.layout.routine_widget_layout)

            // Header and Label
            views.setTextViewText(R.id.widget_day_label, dayLabel)

            // Tabs styling
            if (isTomorrow) {
                views.setInt(R.id.btn_tab_today, "setBackgroundResource", R.drawable.widget_tab_inactive)
                views.setTextColor(R.id.btn_tab_today, 0xFFCBD5E0.toInt())
                views.setInt(R.id.btn_tab_tomorrow, "setBackgroundResource", R.drawable.widget_tab_active)
                views.setTextColor(R.id.btn_tab_tomorrow, 0xFFFFFFFF.toInt())
            } else {
                views.setInt(R.id.btn_tab_today, "setBackgroundResource", R.drawable.widget_tab_active)
                views.setTextColor(R.id.btn_tab_today, 0xFFFFFFFF.toInt())
                views.setInt(R.id.btn_tab_tomorrow, "setBackgroundResource", R.drawable.widget_tab_inactive)
                views.setTextColor(R.id.btn_tab_tomorrow, 0xFFCBD5E0.toInt())
            }

            // Pending Intents for Today and Tomorrow buttons
            val todayIntent = Intent(context, RoutineWidgetProvider::class.java).apply {
                action = ACTION_SHOW_TODAY
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            }
            views.setOnClickPendingIntent(
                R.id.btn_tab_today,
                PendingIntent.getBroadcast(
                    context,
                    appWidgetId * 10 + 1,
                    todayIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            )

            val tomorrowIntent = Intent(context, RoutineWidgetProvider::class.java).apply {
                action = ACTION_SHOW_TOMORROW
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            }
            views.setOnClickPendingIntent(
                R.id.btn_tab_tomorrow,
                PendingIntent.getBroadcast(
                    context,
                    appWidgetId * 10 + 2,
                    tomorrowIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            )

            val refreshIntent = Intent(context, RoutineWidgetProvider::class.java).apply {
                action = ACTION_REFRESH
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            }
            views.setOnClickPendingIntent(
                R.id.btn_widget_refresh,
                PendingIntent.getBroadcast(
                    context,
                    appWidgetId * 10 + 3,
                    refreshIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            )

            // Open MainActivity on root click
            val openAppIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val openAppPendingIntent = PendingIntent.getActivity(
                context,
                appWidgetId * 10 + 4,
                openAppIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_root, openAppPendingIntent)
            views.setOnClickPendingIntent(R.id.tv_more_classes, openAppPendingIntent)
            views.setOnClickPendingIntent(R.id.tv_empty_schedule, openAppPendingIntent)

            // Populate class slots
            if (routines.isEmpty()) {
                views.setViewVisibility(R.id.slot_1, View.GONE)
                views.setViewVisibility(R.id.slot_2, View.GONE)
                views.setViewVisibility(R.id.slot_3, View.GONE)
                views.setViewVisibility(R.id.tv_empty_schedule, View.VISIBLE)
                views.setTextViewText(
                    R.id.tv_empty_schedule,
                    "🎉 No classes or exams scheduled for $titlePrefix"
                )
                views.setViewVisibility(R.id.tv_more_classes, View.GONE)
            } else {
                views.setViewVisibility(R.id.tv_empty_schedule, View.GONE)

                // Slot 1
                views.setViewVisibility(R.id.slot_1, View.VISIBLE)
                views.setTextViewText(R.id.slot_1_time, formatShortTime(routines[0].startTime))
                views.setTextViewText(R.id.slot_1_course, "${routines[0].courseCode} - ${routines[0].courseName}")
                views.setTextViewText(R.id.slot_1_room, "${routines[0].room} • ${routines[0].teacher.ifEmpty { "ISU Faculty" }}")

                // Slot 2
                if (routines.size >= 2) {
                    views.setViewVisibility(R.id.slot_2, View.VISIBLE)
                    views.setTextViewText(R.id.slot_2_time, formatShortTime(routines[1].startTime))
                    views.setTextViewText(R.id.slot_2_course, "${routines[1].courseCode} - ${routines[1].courseName}")
                    views.setTextViewText(R.id.slot_2_room, "${routines[1].room} • ${routines[1].teacher.ifEmpty { "ISU Faculty" }}")
                } else {
                    views.setViewVisibility(R.id.slot_2, View.GONE)
                }

                // Slot 3
                if (routines.size >= 3) {
                    views.setViewVisibility(R.id.slot_3, View.VISIBLE)
                    views.setTextViewText(R.id.slot_3_time, formatShortTime(routines[2].startTime))
                    views.setTextViewText(R.id.slot_3_course, "${routines[2].courseCode} - ${routines[2].courseName}")
                    views.setTextViewText(R.id.slot_3_room, "${routines[2].room} • ${routines[2].teacher.ifEmpty { "ISU Faculty" }}")
                } else {
                    views.setViewVisibility(R.id.slot_3, View.GONE)
                }

                // More classes hint
                if (routines.size > 3) {
                    views.setViewVisibility(R.id.tv_more_classes, View.VISIBLE)
                    views.setTextViewText(
                        R.id.tv_more_classes,
                        "+${routines.size - 3} more classes (tap to open app)"
                    )
                } else {
                    views.setViewVisibility(R.id.tv_more_classes, View.GONE)
                }
            }

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }

    private fun formatShortTime(timeStr: String): String {
        return try {
            val parts = timeStr.split(":")
            val h = parts[0].toInt()
            val m = parts[1].toInt()
            val amPm = if (h >= 12) "PM" else "AM"
            val displayH = if (h == 0) 12 else if (h > 12) h - 12 else h
            String.format(Locale.US, "%02d:%02d %s", displayH, m, amPm)
        } catch (e: Exception) {
            timeStr
        }
    }

    private fun saveSelectedTab(context: Context, appWidgetId: Int, tab: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_TAB_PREFIX + appWidgetId, tab).apply()
    }

    private fun getSelectedTab(context: Context, appWidgetId: Int): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_TAB_PREFIX + appWidgetId, "today") ?: "today"
    }
}
