package com.studentapp.isu14.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.studentapp.isu14.data.firebase.FirebaseSyncManager
import com.studentapp.isu14.data.firebase.SyncState
import com.studentapp.isu14.data.model.DepartmentEntity
import com.studentapp.isu14.data.model.SemesterEntity
import com.studentapp.isu14.data.model.StudentEntity
import com.studentapp.isu14.data.repository.IsuRepository
import com.studentapp.isu14.util.NetworkMonitor
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AppTab {
    ROUTINE, COURSES, NOTIFICATIONS, PROFILE
}

data class ToastMessage(
    val message: String,
    val isError: Boolean = false
)

class MainViewModel(
    private val repository: IsuRepository,
    private val networkMonitor: NetworkMonitor,
    private val syncManager: FirebaseSyncManager
) : ViewModel() {

    private val _currentStudent = MutableStateFlow<StudentEntity?>(null)
    val currentStudent: StateFlow<StudentEntity?> = _currentStudent.asStateFlow()

    private val _currentTab = MutableStateFlow(AppTab.ROUTINE)
    val currentTab: StateFlow<AppTab> = _currentTab.asStateFlow()

    private val _toastFlow = MutableSharedFlow<ToastMessage>()
    val toastFlow: SharedFlow<ToastMessage> = _toastFlow.asSharedFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    val isOnline: StateFlow<Boolean> = networkMonitor.isOnline
        .stateIn(viewModelScope, SharingStarted.Eagerly, networkMonitor.isCurrentlyConnected())

    val syncState: StateFlow<SyncState> = syncManager.syncState

    private val _showOfflineDialog = MutableStateFlow(false)
    val showOfflineDialog: StateFlow<Boolean> = _showOfflineDialog.asStateFlow()

    private var offlineTimerJob: Job? = null

    val departments: StateFlow<List<DepartmentEntity>> = repository.allDepartments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val semesters: StateFlow<List<SemesterEntity>> = repository.allSemesters
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        // Auto-login seed demo student for instant access
        viewModelScope.launch {
            try {
                repository.ensureSeeded()
                val result = repository.login("2023100101", "secretpassword")
                result.onSuccess { student ->
                    _currentStudent.value = student
                    // Sync with Firebase once logged in
                    if (networkMonitor.isCurrentlyConnected()) {
                        syncManager.syncWithCloud(student, true)
                    }
                }.onFailure {
                    val fallback = repository.getFirstStudent()
                    if (fallback != null) {
                        _currentStudent.value = fallback
                    }
                }
            } catch (e: Exception) {
                val fallback = repository.getFirstStudent()
                if (fallback != null) {
                    _currentStudent.value = fallback
                }
            }
        }

        // Monitor network changes & handle 2-second offline dialog requirement
        viewModelScope.launch {
            networkMonitor.isOnline.collect { online ->
                if (online) {
                    offlineTimerJob?.cancel()
                    _showOfflineDialog.value = false
                    // Auto-update latest data from cloud when internet connects
                    syncManager.syncWithCloud(_currentStudent.value, true)
                } else {
                    // Start 2-second timer: if offline for 2 seconds, show popup
                    offlineTimerJob?.cancel()
                    offlineTimerJob = viewModelScope.launch {
                        delay(2000L) // 2 second requirement
                        if (!networkMonitor.isCurrentlyConnected()) {
                            _showOfflineDialog.value = true
                        }
                    }
                }
            }
        }
    }

    fun onAppResume() {
        // Auto-update when opening app if internet connected; otherwise sleep
        val connected = networkMonitor.isCurrentlyConnected()
        if (connected) {
            viewModelScope.launch {
                syncManager.syncWithCloud(_currentStudent.value, true)
            }
        } else {
            // Check if still offline after 2s
            offlineTimerJob?.cancel()
            offlineTimerJob = viewModelScope.launch {
                delay(2000L)
                if (!networkMonitor.isCurrentlyConnected()) {
                    _showOfflineDialog.value = true
                }
            }
        }
    }

    fun dismissOfflineDialog() {
        _showOfflineDialog.value = false
    }

    fun retryConnection() {
        val connected = networkMonitor.isCurrentlyConnected()
        if (connected) {
            _showOfflineDialog.value = false
            showToast("Internet connected! Fetching latest schedule...")
            viewModelScope.launch {
                syncManager.syncWithCloud(_currentStudent.value, true)
            }
        } else {
            showToast("Still disconnected. Showing previous saved data.", isError = true)
        }
    }

    fun manualSync() {
        viewModelScope.launch {
            val connected = networkMonitor.isCurrentlyConnected()
            val res = syncManager.syncWithCloud(_currentStudent.value, connected)
            showToast(res.message, isError = !connected)
        }
    }

    fun selectTab(tab: AppTab) {
        _currentTab.value = tab
    }

    fun showToast(message: String, isError: Boolean = false) {
        viewModelScope.launch {
            _toastFlow.emit(ToastMessage(message, isError))
        }
    }

    fun login(studentId: String, pass: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val res = repository.login(studentId, pass)
            _isLoading.value = false
            res.onSuccess { student ->
                _currentStudent.value = student
                showToast("Welcome back, ${student.name}!")
                if (networkMonitor.isCurrentlyConnected()) {
                    syncManager.syncWithCloud(student, true)
                }
            }.onFailure { err ->
                showToast(err.message ?: "Login failed", isError = true)
            }
        }
    }

    fun register(
        studentId: String,
        name: String,
        password: String,
        department: String,
        batchNo: String,
        semesterId: Int
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            val res = repository.register(studentId, name, password, department, batchNo, semesterId)
            _isLoading.value = false
            res.onSuccess { student ->
                _currentStudent.value = student
                showToast("Registration successful! Default courses enrolled.")
                if (networkMonitor.isCurrentlyConnected()) {
                    syncManager.syncWithCloud(student, true)
                }
            }.onFailure { err ->
                showToast(err.message ?: "Registration failed", isError = true)
            }
        }
    }

    fun logout() {
        _currentStudent.value = null
        _currentTab.value = AppTab.ROUTINE
        showToast("Signed out successfully")
    }

    fun updateName(newName: String) {
        val student = _currentStudent.value ?: return
        viewModelScope.launch {
            val res = repository.updateStudentName(student.studentId, newName)
            res.onSuccess {
                val updated = student.copy(name = newName)
                _currentStudent.value = updated
                showToast("Name updated")
                if (networkMonitor.isCurrentlyConnected()) {
                    syncManager.syncStudent(updated)
                }
            }.onFailure {
                showToast(it.message ?: "Failed to update name", isError = true)
            }
        }
    }

    fun updateStudentId(newId: String) {
        val student = _currentStudent.value ?: return
        viewModelScope.launch {
            val res = repository.updateStudentId(student.studentId, newId)
            res.onSuccess { updated ->
                _currentStudent.value = updated
                showToast("Student ID updated")
                if (networkMonitor.isCurrentlyConnected()) {
                    syncManager.syncStudent(updated)
                }
            }.onFailure {
                showToast(it.message ?: "Failed to update ID", isError = true)
            }
        }
    }

    fun updateBatch(newBatch: String) {
        val student = _currentStudent.value ?: return
        viewModelScope.launch {
            val res = repository.updateBatch(student.studentId, newBatch)
            res.onSuccess {
                val updated = student.copy(batchNo = newBatch)
                _currentStudent.value = updated
                showToast("Batch updated")
                if (networkMonitor.isCurrentlyConnected()) {
                    syncManager.syncStudent(updated)
                }
            }.onFailure {
                showToast(it.message ?: "Failed to update batch", isError = true)
            }
        }
    }

    fun changeDepartment(newDept: String) {
        val student = _currentStudent.value ?: return
        viewModelScope.launch {
            val res = repository.changeDepartment(student.studentId, newDept)
            res.onSuccess {
                val updated = student.copy(department = newDept)
                _currentStudent.value = updated
                showToast("Department changed to $newDept. Courses updated.")
                if (networkMonitor.isCurrentlyConnected()) {
                    syncManager.syncStudent(updated)
                }
            }.onFailure {
                showToast(it.message ?: "Failed to update department", isError = true)
            }
        }
    }

    fun changeSemester(newSem: Int) {
        val student = _currentStudent.value ?: return
        viewModelScope.launch {
            val res = repository.changeSemester(student.studentId, newSem)
            res.onSuccess {
                val updated = student.copy(semesterId = newSem)
                _currentStudent.value = updated
                showToast("Semester changed. Default courses loaded.")
                if (networkMonitor.isCurrentlyConnected()) {
                    syncManager.syncStudent(updated)
                }
            }.onFailure {
                showToast(it.message ?: "Failed to update semester", isError = true)
            }
        }
    }

    fun changePassword(oldPass: String, newPass: String) {
        val student = _currentStudent.value ?: return
        viewModelScope.launch {
            val res = repository.changePassword(student.studentId, oldPass, newPass)
            res.onSuccess {
                showToast("Password changed successfully")
            }.onFailure {
                showToast(it.message ?: "Password update failed", isError = true)
            }
        }
    }
}

class MainViewModelFactory(
    private val repository: IsuRepository,
    private val networkMonitor: NetworkMonitor,
    private val syncManager: FirebaseSyncManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(repository, networkMonitor, syncManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
