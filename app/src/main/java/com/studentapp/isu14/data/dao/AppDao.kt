package com.studentapp.isu14.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.studentapp.isu14.data.model.CourseEntity
import com.studentapp.isu14.data.model.DepartmentEntity
import com.studentapp.isu14.data.model.NotificationEntity
import com.studentapp.isu14.data.model.RoutineEntity
import com.studentapp.isu14.data.model.SemesterEntity
import com.studentapp.isu14.data.model.StudentCourseEntity
import com.studentapp.isu14.data.model.StudentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {

    // --- Students ---
    @Query("SELECT * FROM students WHERE studentId = :studentId LIMIT 1")
    suspend fun getStudentByStudentId(studentId: String): StudentEntity?

    @Query("SELECT * FROM students WHERE studentId = :studentId LIMIT 1")
    fun observeStudent(studentId: String): Flow<StudentEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudent(student: StudentEntity): Long

    @Update
    suspend fun updateStudent(student: StudentEntity)

    // --- Departments & Semesters ---
    @Query("SELECT * FROM departments ORDER BY code ASC")
    fun getAllDepartments(): Flow<List<DepartmentEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertDepartments(departments: List<DepartmentEntity>)

    @Query("SELECT * FROM semesters ORDER BY id ASC")
    fun getAllSemesters(): Flow<List<SemesterEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertSemesters(semesters: List<SemesterEntity>)

    // --- Courses ---
    @Query("SELECT * FROM courses WHERE department = :department ORDER BY semesterId ASC, courseCode ASC")
    fun getCoursesByDepartment(department: String): Flow<List<CourseEntity>>

    @Query("SELECT * FROM courses WHERE department = :department AND semesterId = :semesterId AND isDefault = 1")
    suspend fun getDefaultCourses(department: String, semesterId: Int): List<CourseEntity>

    @Query("SELECT * FROM courses WHERE courseId = :courseId LIMIT 1")
    suspend fun getCourseById(courseId: Int): CourseEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCourses(courses: List<CourseEntity>)

    // --- Student Enrolled Courses ---
    @Query("""
        SELECT c.* FROM courses c
        INNER JOIN student_courses sc ON c.courseId = sc.courseId
        WHERE sc.studentId = :studentId
        ORDER BY sc.enrolledAt DESC
    """)
    fun getEnrolledCoursesForStudent(studentId: String): Flow<List<CourseEntity>>

    @Query("SELECT courseId FROM student_courses WHERE studentId = :studentId")
    suspend fun getEnrolledCourseIds(studentId: String): List<Int>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun enrollCourse(enrollment: StudentCourseEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun enrollCourses(enrollments: List<StudentCourseEntity>)

    @Query("DELETE FROM student_courses WHERE studentId = :studentId AND courseId = :courseId")
    suspend fun dropCourse(studentId: String, courseId: Int)

    @Query("DELETE FROM student_courses WHERE studentId = :studentId")
    suspend fun clearStudentCourses(studentId: String)

    // --- Routines ---
    @Query("""
        SELECT r.* FROM routines r
        INNER JOIN student_courses sc ON r.courseId = sc.courseId
        WHERE sc.studentId = :studentId AND r.department = :department
        ORDER BY r.startTime ASC
    """)
    fun getRoutinesForStudent(studentId: String, department: String): Flow<List<RoutineEntity>>

    @Query("SELECT * FROM routines WHERE department = :department ORDER BY startTime ASC")
    fun getAllRoutinesForDepartment(department: String): Flow<List<RoutineEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoutines(routines: List<RoutineEntity>)

    // --- Notifications ---
    @Query("SELECT * FROM notifications WHERE department = :department OR department = 'ALL' ORDER BY id DESC")
    fun getNotificationsForDepartment(department: String): Flow<List<NotificationEntity>>

    @Query("SELECT COUNT(*) FROM notifications WHERE (department = :department OR department = 'ALL') AND isRead = 0")
    fun getUnreadNotificationCount(department: String): Flow<Int>

    @Query("UPDATE notifications SET isRead = 1 WHERE id = :id")
    suspend fun markNotificationRead(id: Int)

    @Query("UPDATE notifications SET isRead = 1 WHERE department = :department OR department = 'ALL'")
    suspend fun markAllNotificationsRead(department: String)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertNotifications(notifications: List<NotificationEntity>)

    // --- Synchronous queries for Widget and Firebase Sync ---
    @Query("""
        SELECT r.* FROM routines r
        INNER JOIN student_courses sc ON r.courseId = sc.courseId
        WHERE sc.studentId = :studentId AND LOWER(r.day) = LOWER(:day)
        ORDER BY r.startTime ASC
    """)
    suspend fun getStudentRoutinesForDay(studentId: String, day: String): List<RoutineEntity>

    @Query("SELECT * FROM routines WHERE LOWER(day) = LOWER(:day) ORDER BY startTime ASC")
    suspend fun getAllRoutinesForDay(day: String): List<RoutineEntity>

    @Query("SELECT * FROM students LIMIT 1")
    suspend fun getFirstStudent(): StudentEntity?

    @Query("SELECT * FROM courses")
    suspend fun getAllCoursesSync(): List<CourseEntity>

    @Query("SELECT * FROM routines")
    suspend fun getAllRoutinesSync(): List<RoutineEntity>

    @Query("SELECT * FROM notifications")
    suspend fun getAllNotificationsSync(): List<NotificationEntity>
}
