package com.studentapp.isu14.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.studentapp.isu14.data.model.NotificationEntity
import com.studentapp.isu14.data.repository.IsuRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class NotificationsViewModel(
    private val repository: IsuRepository,
    private val department: String
) : ViewModel() {

    private val _filterCategory = MutableStateFlow("all")
    val filterCategory: StateFlow<String> = _filterCategory.asStateFlow()

    val unreadCount: StateFlow<Int> = repository.getUnreadCount(department)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val notifications: StateFlow<List<NotificationEntity>> = combine(
        repository.getNotifications(department),
        _filterCategory
    ) { list, cat ->
        if (cat == "all") {
            list
        } else {
            list.filter { it.type.equals(cat, ignoreCase = true) }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setFilter(category: String) {
        _filterCategory.value = category
    }

    fun markRead(id: Int) {
        viewModelScope.launch {
            repository.markNotificationRead(id)
        }
    }

    fun markAllRead() {
        viewModelScope.launch {
            repository.markAllNotificationsRead(department)
        }
    }
}

class NotificationsViewModelFactory(
    private val repository: IsuRepository,
    private val department: String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NotificationsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return NotificationsViewModel(repository, department) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
