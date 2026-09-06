package com.studentapp.isu14.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.studentapp.isu14.data.model.CourseEntity
import com.studentapp.isu14.data.repository.IsuRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class CourseGatingStatus {
    data object Enrolled : CourseGatingStatus()
    data class Allowed(val isRetake: Boolean) : CourseGatingStatus()
    data class Locked(val requiredSemester: Int) : CourseGatingStatus()
}

data class CourseUiItem(
    val course: CourseEntity,
    val gatingStatus: CourseGatingStatus
)

class CoursesViewModel(
    private val repository: IsuRepository,
    private val studentId: String,
    private val department: String,
    private val studentSemester: Int
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _toastEvent = MutableSharedFlow<String>()
    val toastEvent: SharedFlow<String> = _toastEvent.asSharedFlow()

    val enrolledCourses: StateFlow<List<CourseEntity>> = repository.getEnrolledCourses(studentId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalCredits: StateFlow<Double> = enrolledCourses.combine(_searchQuery) { list, _ ->
        list.sumOf { it.credit }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val departmentCourses: StateFlow<List<CourseUiItem>> = combine(
        repository.getDepartmentCourses(department),
        enrolledCourses,
        _searchQuery
    ) { catalog, enrolled, query ->
        val enrolledIds = enrolled.map { it.courseId }.toSet()
        val filtered = if (query.isBlank()) {
            catalog
        } else {
            catalog.filter {
                it.courseCode.contains(query, ignoreCase = true) ||
                        it.courseName.contains(query, ignoreCase = true)
            }
        }

        filtered.map { course ->
            val status = when {
                enrolledIds.contains(course.courseId) -> CourseGatingStatus.Enrolled
                course.semesterId > studentSemester -> CourseGatingStatus.Locked(course.semesterId)
                course.semesterId < studentSemester -> CourseGatingStatus.Allowed(isRetake = true)
                else -> CourseGatingStatus.Allowed(isRetake = false)
            }
            CourseUiItem(course, status)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun enroll(course: CourseEntity) {
        viewModelScope.launch {
            val res = repository.enrollCourse(studentId, course.courseId, studentSemester)
            res.onSuccess {
                _toastEvent.emit("Successfully enrolled in ${course.courseCode}!")
            }.onFailure {
                _toastEvent.emit(it.message ?: "Failed to enroll")
            }
        }
    }

    fun drop(course: CourseEntity) {
        viewModelScope.launch {
            val res = repository.dropCourse(studentId, course.courseId)
            res.onSuccess {
                _toastEvent.emit("Dropped ${course.courseCode}")
            }.onFailure {
                _toastEvent.emit("Failed to drop course")
            }
        }
    }
}

class CoursesViewModelFactory(
    private val repository: IsuRepository,
    private val studentId: String,
    private val department: String,
    private val studentSemester: Int
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CoursesViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CoursesViewModel(repository, studentId, department, studentSemester) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
