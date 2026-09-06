package com.studentapp.isu14.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "students")
data class StudentEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val studentId: String,
    val name: String,
    val passwordHash: String,
    val department: String,
    val batchNo: String,
    val semesterId: Int = 1,
    val totalCredits: Double = 0.0,
    val lastLogin: String = ""
)

@Entity(tableName = "departments")
data class DepartmentEntity(
    @PrimaryKey val code: String,
    val name: String
)

@Entity(tableName = "semesters")
data class SemesterEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val isActive: Boolean = true
)

@Entity(tableName = "courses")
data class CourseEntity(
    @PrimaryKey val courseId: Int,
    val courseCode: String,
    val courseName: String,
    val department: String,
    val semesterId: Int,
    val credit: Double = 3.0,
    val isDefault: Boolean = true,
    val teacher: String = ""
)

@Entity(tableName = "student_courses")
data class StudentCourseEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val studentId: String,
    val courseId: Int,
    val semesterId: Int,
    val enrolledAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "routines")
data class RoutineEntity(
    @PrimaryKey val id: Int,
    val courseId: Int,
    val courseCode: String,
    val courseName: String,
    val department: String,
    val semesterId: Int,
    val batchNo: String = "ALL",
    val day: String,
    val date: String? = null,
    val type: String = "class", // class, ct1, ct2, ct3, ct, mid, final, lab
    val assessmentTag: String = "Regular Class",
    val startTime: String, // "09:00:00"
    val endTime: String,   // "10:30:00"
    val room: String,
    val teacher: String,
    val title: String? = null,
    val syllabus: String? = null,
    val note: String? = null
)

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey val id: Int,
    val studentId: String? = null,
    val department: String,
    val semesterId: Int? = null,
    val courseId: Int? = null,
    val title: String,
    val message: String,
    val type: String = "general", // ct, mid, final, class, general
    val link: String? = null,
    val isRead: Boolean = false,
    val createdAt: String
)
