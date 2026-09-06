package com.studentapp.isu14.data.firebase

import android.util.Log
import com.studentapp.isu14.data.dao.AppDao
import com.studentapp.isu14.data.model.CourseEntity
import com.studentapp.isu14.data.model.NotificationEntity
import com.studentapp.isu14.data.model.RoutineEntity
import com.studentapp.isu14.data.model.StudentCourseEntity
import com.studentapp.isu14.data.model.StudentEntity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.PersistentCacheSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull

enum class SyncStatus {
    IDLE,
    SYNCING,
    SUCCESS,
    OFFLINE_PREVIOUS_DATA,
    ERROR
}

data class SyncState(
    val status: SyncStatus = SyncStatus.IDLE,
    val lastSyncMillis: Long = 0L,
    val message: String = "Ready"
)

class FirebaseSyncManager(private val appDao: AppDao) {

    private val TAG = "FirebaseSyncManager"

    val firestore: FirebaseFirestore? by lazy {
        try {
            val instance = FirebaseFirestore.getInstance()
            try {
                val settings = FirebaseFirestoreSettings.Builder()
                    .setLocalCacheSettings(PersistentCacheSettings.newBuilder().build())
                    .build()
                instance.firestoreSettings = settings
            } catch (e: Exception) {
                Log.w(TAG, "Firestore settings already applied or error: ${e.message}")
            }
            instance
        } catch (t: Throwable) {
            Log.w(TAG, "Firestore not initialized or unavailable: ${t.message}")
            null
        }
    }

    private val _syncState = MutableStateFlow(SyncState())
    val syncState: StateFlow<SyncState> = _syncState.asStateFlow()

    suspend fun syncWithCloud(
        currentStudent: StudentEntity?,
        isNetworkAvailable: Boolean
    ): SyncState = withContext(Dispatchers.IO) {
        if (!isNetworkAvailable) {
            val state = SyncState(
                status = SyncStatus.OFFLINE_PREVIOUS_DATA,
                lastSyncMillis = _syncState.value.lastSyncMillis,
                message = "Offline: Showing previously cached routines"
            )
            _syncState.value = state
            return@withContext state
        }

        val fs = firestore
        if (fs == null) {
            val offlineState = SyncState(
                status = SyncStatus.OFFLINE_PREVIOUS_DATA,
                lastSyncMillis = _syncState.value.lastSyncMillis,
                message = "Offline Mode: Showing local routine data"
            )
            _syncState.value = offlineState
            return@withContext offlineState
        }

        _syncState.value = SyncState(
            status = SyncStatus.SYNCING,
            lastSyncMillis = _syncState.value.lastSyncMillis,
            message = "Syncing with cloud database..."
        )

        try {
            // 1. Sync Routines
            syncRoutines(fs)

            // 2. Sync Courses
            syncCourses(fs)

            // 3. Sync Notifications
            syncNotifications(fs)

            // 4. If student is logged in, sync student record and enrollments
            if (currentStudent != null) {
                syncStudent(currentStudent, fs)
            }

            val now = System.currentTimeMillis()
            val successState = SyncState(
                status = SyncStatus.SUCCESS,
                lastSyncMillis = now,
                message = "All routines and schedules up to date"
            )
            _syncState.value = successState
            successState
        } catch (e: Exception) {
            Log.e(TAG, "Sync error: ${e.message}", e)
            val fallbackState = SyncState(
                status = SyncStatus.OFFLINE_PREVIOUS_DATA,
                lastSyncMillis = _syncState.value.lastSyncMillis,
                message = "Showing offline routines (${e.localizedMessage ?: "Sync error"})"
            )
            _syncState.value = fallbackState
            fallbackState
        }
    }

    private suspend fun syncRoutines(fs: FirebaseFirestore) {
        val snapshot = withTimeoutOrNull(6000L) {
            fs.collection("routines").get().await()
        }

        if (snapshot != null && !snapshot.isEmpty) {
            val cloudRoutines = snapshot.documents.mapNotNull { doc ->
                try {
                    RoutineEntity(
                        id = (doc.getLong("id") ?: doc.id.toLongOrNull() ?: 0L).toInt(),
                        courseId = (doc.getLong("courseId") ?: 0L).toInt(),
                        courseCode = doc.getString("courseCode") ?: "",
                        courseName = doc.getString("courseName") ?: "",
                        department = doc.getString("department") ?: "CSE",
                        semesterId = (doc.getLong("semesterId") ?: 1L).toInt(),
                        batchNo = doc.getString("batchNo") ?: "ALL",
                        day = doc.getString("day") ?: "Sunday",
                        date = doc.getString("date"),
                        type = doc.getString("type") ?: "class",
                        assessmentTag = doc.getString("assessmentTag") ?: "Regular Class",
                        startTime = doc.getString("startTime") ?: "09:00:00",
                        endTime = doc.getString("endTime") ?: "10:30:00",
                        room = doc.getString("room") ?: "Room 401",
                        teacher = doc.getString("teacher") ?: "TBA",
                        title = doc.getString("title"),
                        syllabus = doc.getString("syllabus"),
                        note = doc.getString("note")
                    )
                } catch (e: Exception) {
                    null
                }
            }
            if (cloudRoutines.isNotEmpty()) {
                appDao.insertRoutines(cloudRoutines)
            }
        } else {
            // If cloud collection is empty, seed local routines to cloud
            val localRoutines = appDao.getAllRoutinesSync()
            if (localRoutines.isNotEmpty()) {
                val batch = fs.batch()
                localRoutines.take(20).forEach { r ->
                    val docRef = fs.collection("routines").document(r.id.toString())
                    batch.set(
                        docRef, mapOf(
                            "id" to r.id,
                            "courseId" to r.courseId,
                            "courseCode" to r.courseCode,
                            "courseName" to r.courseName,
                            "department" to r.department,
                            "semesterId" to r.semesterId,
                            "batchNo" to r.batchNo,
                            "day" to r.day,
                            "date" to r.date,
                            "type" to r.type,
                            "assessmentTag" to r.assessmentTag,
                            "startTime" to r.startTime,
                            "endTime" to r.endTime,
                            "room" to r.room,
                            "teacher" to r.teacher,
                            "title" to r.title,
                            "syllabus" to r.syllabus,
                            "note" to r.note
                        )
                    )
                }
                withTimeoutOrNull(4000L) { batch.commit().await() }
            }
        }
    }

    private suspend fun syncCourses(fs: FirebaseFirestore) {
        val snapshot = withTimeoutOrNull(6000L) {
            fs.collection("courses").get().await()
        }

        if (snapshot != null && !snapshot.isEmpty) {
            val cloudCourses = snapshot.documents.mapNotNull { doc ->
                try {
                    CourseEntity(
                        courseId = (doc.getLong("courseId") ?: doc.id.toLongOrNull() ?: 0L).toInt(),
                        courseCode = doc.getString("courseCode") ?: "",
                        courseName = doc.getString("courseName") ?: "",
                        department = doc.getString("department") ?: "CSE",
                        semesterId = (doc.getLong("semesterId") ?: 1L).toInt(),
                        credit = doc.getDouble("credit") ?: 3.0,
                        isDefault = doc.getBoolean("isDefault") ?: true,
                        teacher = doc.getString("teacher") ?: ""
                    )
                } catch (e: Exception) {
                    null
                }
            }
            if (cloudCourses.isNotEmpty()) {
                appDao.insertCourses(cloudCourses)
            }
        } else {
            val localCourses = appDao.getAllCoursesSync()
            if (localCourses.isNotEmpty()) {
                val batch = fs.batch()
                localCourses.take(20).forEach { c ->
                    val docRef = fs.collection("courses").document(c.courseId.toString())
                    batch.set(
                        docRef, mapOf(
                            "courseId" to c.courseId,
                            "courseCode" to c.courseCode,
                            "courseName" to c.courseName,
                            "department" to c.department,
                            "semesterId" to c.semesterId,
                            "credit" to c.credit,
                            "isDefault" to c.isDefault,
                            "teacher" to c.teacher
                        )
                    )
                }
                withTimeoutOrNull(4000L) { batch.commit().await() }
            }
        }
    }

    private suspend fun syncNotifications(fs: FirebaseFirestore) {
        val snapshot = withTimeoutOrNull(6000L) {
            fs.collection("notifications").get().await()
        }

        if (snapshot != null && !snapshot.isEmpty) {
            val cloudNotices = snapshot.documents.mapNotNull { doc ->
                try {
                    NotificationEntity(
                        id = (doc.getLong("id") ?: doc.id.toLongOrNull() ?: 0L).toInt(),
                        studentId = doc.getString("studentId"),
                        department = doc.getString("department") ?: "ALL",
                        semesterId = doc.getLong("semesterId")?.toInt(),
                        courseId = doc.getLong("courseId")?.toInt(),
                        title = doc.getString("title") ?: "",
                        message = doc.getString("message") ?: "",
                        type = doc.getString("type") ?: "general",
                        link = doc.getString("link"),
                        isRead = doc.getBoolean("isRead") ?: false,
                        createdAt = doc.getString("createdAt") ?: ""
                    )
                } catch (e: Exception) {
                    null
                }
            }
            if (cloudNotices.isNotEmpty()) {
                appDao.insertNotifications(cloudNotices)
            }
        } else {
            val localNotices = appDao.getAllNotificationsSync()
            if (localNotices.isNotEmpty()) {
                val batch = fs.batch()
                localNotices.take(10).forEach { n ->
                    val docRef = fs.collection("notifications").document(n.id.toString())
                    batch.set(
                        docRef, mapOf(
                            "id" to n.id,
                            "studentId" to n.studentId,
                            "department" to n.department,
                            "semesterId" to n.semesterId,
                            "courseId" to n.courseId,
                            "title" to n.title,
                            "message" to n.message,
                            "type" to n.type,
                            "link" to n.link,
                            "isRead" to n.isRead,
                            "createdAt" to n.createdAt
                        )
                    )
                }
                withTimeoutOrNull(4000L) { batch.commit().await() }
            }
        }
    }

    suspend fun syncStudent(student: StudentEntity, fs: FirebaseFirestore) {
        val docRef = fs.collection("students").document(student.studentId)
        val studentData = mapOf(
            "studentId" to student.studentId,
            "name" to student.name,
            "department" to student.department,
            "batchNo" to student.batchNo,
            "semesterId" to student.semesterId,
            "totalCredits" to student.totalCredits,
            "lastLogin" to student.lastLogin
        )
        withTimeoutOrNull(4000L) {
            docRef.set(studentData).await()
        }

        // Sync enrollments
        val enrolledIds = appDao.getEnrolledCourseIds(student.studentId)
        val enrollData = mapOf(
            "studentId" to student.studentId,
            "enrolledCourseIds" to enrolledIds,
            "updatedAt" to System.currentTimeMillis()
        )
        withTimeoutOrNull(4000L) {
            fs.collection("student_enrollments")
                .document(student.studentId)
                .set(enrollData)
                .await()
        }
    }
}
