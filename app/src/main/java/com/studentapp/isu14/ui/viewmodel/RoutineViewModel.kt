package com.studentapp.isu14.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.studentapp.isu14.data.model.RoutineEntity
import com.studentapp.isu14.data.repository.IsuRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

data class LiveClassStatus(
    val currentRoutine: RoutineEntity? = null,
    val nextRoutine: RoutineEntity? = null,
    val stateText: String = "No classes scheduled right now",
    val progress: Float = 0f, // 0.0 to 1.0
    val currentTimeString: String = "",
    val currentDay: String = ""
)

class RoutineViewModel(
    private val repository: IsuRepository,
    studentId: String,
    department: String
) : ViewModel() {

    private val _selectedDay = MutableStateFlow("Today")
    val selectedDay: StateFlow<String> = _selectedDay.asStateFlow()

    private val _selectedType = MutableStateFlow("all")
    val selectedType: StateFlow<String> = _selectedType.asStateFlow()

    private val _detailRoutine = MutableStateFlow<RoutineEntity?>(null)
    val detailRoutine: StateFlow<RoutineEntity?> = _detailRoutine.asStateFlow()

    private val _liveClassStatus = MutableStateFlow(LiveClassStatus())
    val liveClassStatus: StateFlow<LiveClassStatus> = _liveClassStatus.asStateFlow()

    private val studentRoutines = repository.getRoutinesForStudent(studentId, department)

    val daysList = listOf("Today", "Tomorrow", "Saturday", "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "All")

    val filteredRoutines: StateFlow<List<RoutineEntity>> = combine(
        studentRoutines,
        _selectedDay,
        _selectedType
    ) { routines, dayFilter, typeFilter ->
        val currentDayOfWeek = getCurrentDayString()
        val tomorrowDayOfWeek = getTomorrowDayString()
        val targetDay = when (dayFilter) {
            "Today" -> currentDayOfWeek
            "Tomorrow" -> tomorrowDayOfWeek
            else -> dayFilter
        }

        routines.filter { routine ->
            val matchesDay = if (targetDay == "All") true else routine.day.equals(targetDay, ignoreCase = true)
            val matchesType = when (typeFilter) {
                "all" -> true
                "class" -> routine.type == "class"
                "ct" -> routine.type.startsWith("ct")
                "mid" -> routine.type == "mid"
                "final" -> routine.type == "final"
                "lab" -> routine.type == "lab"
                else -> true
            }
            matchesDay && matchesType
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val upcomingCt: StateFlow<RoutineEntity?> = studentRoutines.combine(_selectedDay) { routines, _ ->
        routines.firstOrNull { it.type.startsWith("ct") }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    init {
        // Real-time clock & live class ticker (updates every second)
        viewModelScope.launch {
            while (true) {
                updateLiveTracker()
                delay(1000)
            }
        }
    }

    fun setDay(day: String) {
        _selectedDay.value = day
    }

    fun setType(type: String) {
        _selectedType.value = type
    }

    fun showDetail(routine: RoutineEntity) {
        _detailRoutine.value = routine
    }

    fun closeDetail() {
        _detailRoutine.value = null
    }

    private suspend fun updateLiveTracker() {
        val now = LocalTime.now()
        val timeFormat = DateTimeFormatter.ofPattern("hh:mm:ss a")
        val timeString = now.format(timeFormat)
        val todayStr = getCurrentDayString()

        // Get routines for today
        val allRoutines = filteredRoutines.value
        val todayRoutines = allRoutines.filter { it.day.equals(todayStr, ignoreCase = true) }
            .sortedBy { it.startTime }

        var ongoing: RoutineEntity? = null
        var next: RoutineEntity? = null
        var progress = 0f
        var stateText = "No classes scheduled right now"

        for (r in todayRoutines) {
            try {
                val start = LocalTime.parse(r.startTime)
                val end = LocalTime.parse(r.endTime)
                if (now.isAfter(start) && now.isBefore(end)) {
                    ongoing = r
                    val totalSec = java.time.Duration.between(start, end).seconds
                    val elapsedSec = java.time.Duration.between(start, now).seconds
                    progress = (elapsedSec.toFloat() / totalSec.toFloat()).coerceIn(0f, 1f)
                    val remainingMins = java.time.Duration.between(now, end).toMinutes()
                    stateText = "Ongoing Class (${remainingMins}m remaining)"
                    break
                } else if (now.isBefore(start) && next == null) {
                    next = r
                    val minsUntil = java.time.Duration.between(now, start).toMinutes()
                    stateText = "Next class starts in ${minsUntil} mins"
                }
            } catch (_: Exception) {
                // Ignore parse errors gracefully
            }
        }

        if (ongoing == null && next == null) {
            stateText = if (todayRoutines.isEmpty()) "Free Day (No classes)" else "All classes finished for today"
        }

        _liveClassStatus.value = LiveClassStatus(
            currentRoutine = ongoing,
            nextRoutine = next,
            stateText = stateText,
            progress = progress,
            currentTimeString = timeString,
            currentDay = todayStr
        )
    }

    private fun getCurrentDayString(): String {
        return when (LocalDate.now().dayOfWeek) {
            DayOfWeek.SATURDAY -> "Saturday"
            DayOfWeek.SUNDAY -> "Sunday"
            DayOfWeek.MONDAY -> "Monday"
            DayOfWeek.TUESDAY -> "Tuesday"
            DayOfWeek.WEDNESDAY -> "Wednesday"
            DayOfWeek.THURSDAY -> "Thursday"
            DayOfWeek.FRIDAY -> "Friday"
            else -> "Saturday"
        }
    }

    private fun getTomorrowDayString(): String {
        return when (LocalDate.now().plusDays(1).dayOfWeek) {
            DayOfWeek.SATURDAY -> "Saturday"
            DayOfWeek.SUNDAY -> "Sunday"
            DayOfWeek.MONDAY -> "Monday"
            DayOfWeek.TUESDAY -> "Tuesday"
            DayOfWeek.WEDNESDAY -> "Wednesday"
            DayOfWeek.THURSDAY -> "Thursday"
            DayOfWeek.FRIDAY -> "Friday"
            else -> "Saturday"
        }
    }
}

class RoutineViewModelFactory(
    private val repository: IsuRepository,
    private val studentId: String,
    private val department: String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RoutineViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RoutineViewModel(repository, studentId, department) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
