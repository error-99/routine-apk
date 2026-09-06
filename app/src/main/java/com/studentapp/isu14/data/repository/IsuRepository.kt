package com.studentapp.isu14.data.repository

import com.studentapp.isu14.data.dao.AppDao
import com.studentapp.isu14.data.model.CourseEntity
import com.studentapp.isu14.data.model.DepartmentEntity
import com.studentapp.isu14.data.model.NotificationEntity
import com.studentapp.isu14.data.model.RoutineEntity
import com.studentapp.isu14.data.model.SemesterEntity
import com.studentapp.isu14.data.model.StudentCourseEntity
import com.studentapp.isu14.data.model.StudentEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class IsuRepository(private val appDao: AppDao) {

    val allDepartments: Flow<List<DepartmentEntity>> = appDao.getAllDepartments()
    val allSemesters: Flow<List<SemesterEntity>> = appDao.getAllSemesters()

    suspend fun login(studentId: String, password: String): Result<StudentEntity> {
        val cleanedId = studentId.trim()
        if (cleanedId.isEmpty() || password.isEmpty()) {
            return Result.failure(IllegalArgumentException("Please provide both Student ID and Password."))
        }
        val student = appDao.getStudentByStudentId(cleanedId)
            ?: return Result.failure(IllegalArgumentException("Student ID not found. Please check your credentials."))

        if (student.passwordHash != password) {
            return Result.failure(IllegalArgumentException("Incorrect password. Please try again."))
        }

        val updated = student.copy(lastLogin = java.time.LocalDateTime.now().toString())
        appDao.updateStudent(updated)
        return Result.success(updated)
    }

    suspend fun register(
        studentId: String,
        name: String,
        password: String,
        department: String,
        batchNo: String,
        semesterId: Int
    ): Result<StudentEntity> {
        val cleanedId = studentId.trim()
        if (!cleanedId.matches(Regex("^\\d{10,}$"))) {
            return Result.failure(IllegalArgumentException("Student ID must contain numbers only and be at least 10 digits."))
        }
        if (name.trim().length < 2) {
            return Result.failure(IllegalArgumentException("Full Name must be at least 2 characters."))
        }
        if (password.length < 4) {
            return Result.failure(IllegalArgumentException("Password must be at least 4 characters long."))
        }
        if (department.isEmpty()) {
            return Result.failure(IllegalArgumentException("Please select a department."))
        }

        val existing = appDao.getStudentByStudentId(cleanedId)
        if (existing != null) {
            return Result.failure(IllegalArgumentException("A student with this ID already exists. Please sign in."))
        }

        val defaultCourses = appDao.getDefaultCourses(department, semesterId)
        val initialCredits = defaultCourses.sumOf { it.credit }

        val newStudent = StudentEntity(
            studentId = cleanedId,
            name = name.trim(),
            passwordHash = password,
            department = department,
            batchNo = batchNo.ifEmpty { "1" },
            semesterId = semesterId,
            totalCredits = initialCredits,
            lastLogin = java.time.LocalDateTime.now().toString()
        )

        appDao.insertStudent(newStudent)

        // Auto-enroll default courses
        val enrollments = defaultCourses.map {
            StudentCourseEntity(
                studentId = cleanedId,
                courseId = it.courseId,
                semesterId = semesterId,
                enrolledAt = System.currentTimeMillis()
            )
        }
        appDao.enrollCourses(enrollments)

        return Result.success(newStudent)
    }

    fun observeStudent(studentId: String): Flow<StudentEntity?> =
        appDao.observeStudent(studentId)

    suspend fun updateStudentName(studentId: String, newName: String): Result<Unit> {
        if (newName.trim().length < 2) {
            return Result.failure(IllegalArgumentException("Name must be at least 2 characters."))
        }
        val student = appDao.getStudentByStudentId(studentId)
            ?: return Result.failure(IllegalArgumentException("Student not found"))
        appDao.updateStudent(student.copy(name = newName.trim()))
        return Result.success(Unit)
    }

    suspend fun updateStudentId(oldId: String, newId: String): Result<StudentEntity> {
        val cleaned = newId.trim()
        if (!cleaned.matches(Regex("^\\d{10,}$"))) {
            return Result.failure(IllegalArgumentException("Student ID must be at least 10 numbers."))
        }
        if (cleaned != oldId) {
            val existing = appDao.getStudentByStudentId(cleaned)
            if (existing != null) {
                return Result.failure(IllegalArgumentException("Student ID already in use."))
            }
        }
        val student = appDao.getStudentByStudentId(oldId)
            ?: return Result.failure(IllegalArgumentException("Student not found"))
        val updated = student.copy(studentId = cleaned)
        appDao.updateStudent(updated)
        return Result.success(updated)
    }

    suspend fun updateBatch(studentId: String, newBatch: String): Result<Unit> {
        val student = appDao.getStudentByStudentId(studentId)
            ?: return Result.failure(IllegalArgumentException("Student not found"))
        appDao.updateStudent(student.copy(batchNo = newBatch.trim()))
        return Result.success(Unit)
    }

    suspend fun changeDepartment(studentId: String, newDept: String): Result<Unit> {
        val student = appDao.getStudentByStudentId(studentId)
            ?: return Result.failure(IllegalArgumentException("Student not found"))

        appDao.clearStudentCourses(studentId)
        val defaultCourses = appDao.getDefaultCourses(newDept, student.semesterId)
        val enrollments = defaultCourses.map {
            StudentCourseEntity(
                studentId = studentId,
                courseId = it.courseId,
                semesterId = student.semesterId,
                enrolledAt = System.currentTimeMillis()
            )
        }
        appDao.enrollCourses(enrollments)

        val totalCredits = defaultCourses.sumOf { it.credit }
        appDao.updateStudent(student.copy(department = newDept, totalCredits = totalCredits))
        return Result.success(Unit)
    }

    suspend fun changeSemester(studentId: String, newSemesterId: Int): Result<Unit> {
        val student = appDao.getStudentByStudentId(studentId)
            ?: return Result.failure(IllegalArgumentException("Student not found"))

        appDao.clearStudentCourses(studentId)
        val defaultCourses = appDao.getDefaultCourses(student.department, newSemesterId)
        val enrollments = defaultCourses.map {
            StudentCourseEntity(
                studentId = studentId,
                courseId = it.courseId,
                semesterId = newSemesterId,
                enrolledAt = System.currentTimeMillis()
            )
        }
        appDao.enrollCourses(enrollments)

        val totalCredits = defaultCourses.sumOf { it.credit }
        appDao.updateStudent(student.copy(semesterId = newSemesterId, totalCredits = totalCredits))
        return Result.success(Unit)
    }

    suspend fun changePassword(studentId: String, oldPass: String, newPass: String): Result<Unit> {
        val student = appDao.getStudentByStudentId(studentId)
            ?: return Result.failure(IllegalArgumentException("Student not found"))
        if (student.passwordHash != oldPass) {
            return Result.failure(IllegalArgumentException("Current password incorrect"))
        }
        if (newPass.length < 4) {
            return Result.failure(IllegalArgumentException("New password must be at least 4 characters"))
        }
        appDao.updateStudent(student.copy(passwordHash = newPass))
        return Result.success(Unit)
    }

    fun getEnrolledCourses(studentId: String): Flow<List<CourseEntity>> =
        appDao.getEnrolledCoursesForStudent(studentId)

    fun getDepartmentCourses(department: String): Flow<List<CourseEntity>> =
        appDao.getCoursesByDepartment(department)

    suspend fun enrollCourse(studentId: String, courseId: Int, studentSemesterId: Int): Result<CourseEntity> {
        val course = appDao.getCourseById(courseId)
            ?: return Result.failure(IllegalArgumentException("Course not found in catalog"))

        // Algorithm rule: Cannot take higher semester courses
        if (course.semesterId > studentSemesterId) {
            return Result.failure(
                IllegalArgumentException(
                    "Cannot enroll in ${course.courseCode}. You are in Semester $studentSemesterId; courses for Semester ${course.semesterId} are locked."
                )
            )
        }

        appDao.enrollCourse(
            StudentCourseEntity(
                studentId = studentId,
                courseId = courseId,
                semesterId = course.semesterId,
                enrolledAt = System.currentTimeMillis()
            )
        )

        // Recalculate credits
        val currentEnrolled = appDao.getEnrolledCoursesForStudent(studentId).first()
        val total = currentEnrolled.sumOf { it.credit }
        val student = appDao.getStudentByStudentId(studentId)
        if (student != null) {
            appDao.updateStudent(student.copy(totalCredits = total))
        }

        return Result.success(course)
    }

    suspend fun dropCourse(studentId: String, courseId: Int): Result<Unit> {
        appDao.dropCourse(studentId, courseId)
        val currentEnrolled = appDao.getEnrolledCoursesForStudent(studentId).first()
        val total = currentEnrolled.sumOf { it.credit }
        val student = appDao.getStudentByStudentId(studentId)
        if (student != null) {
            appDao.updateStudent(student.copy(totalCredits = total))
        }
        return Result.success(Unit)
    }

    fun getRoutinesForStudent(studentId: String, department: String): Flow<List<RoutineEntity>> =
        appDao.getRoutinesForStudent(studentId, department)

    fun getNotifications(department: String): Flow<List<NotificationEntity>> =
        appDao.getNotificationsForDepartment(department)

    fun getUnreadCount(department: String): Flow<Int> =
        appDao.getUnreadNotificationCount(department)

    suspend fun markNotificationRead(id: Int) =
        appDao.markNotificationRead(id)

    suspend fun markAllNotificationsRead(department: String) =
        appDao.markAllNotificationsRead(department)

    suspend fun ensureSeeded() {
        val firstStudent = appDao.getFirstStudent()
        if (firstStudent == null) {
            appDao.insertDepartments(com.studentapp.isu14.data.database.DatabaseSeedData.departments)
            appDao.insertSemesters(com.studentapp.isu14.data.database.DatabaseSeedData.semesters)
            appDao.insertCourses(com.studentapp.isu14.data.database.DatabaseSeedData.courses)
            for (student in com.studentapp.isu14.data.database.DatabaseSeedData.students) {
                appDao.insertStudent(student)
            }
            appDao.enrollCourses(com.studentapp.isu14.data.database.DatabaseSeedData.studentCourses)
            appDao.insertRoutines(com.studentapp.isu14.data.database.DatabaseSeedData.routines)
            appDao.insertNotifications(com.studentapp.isu14.data.database.DatabaseSeedData.notifications)
        }
    }

    suspend fun getFirstStudent(): StudentEntity? = appDao.getFirstStudent()
}
