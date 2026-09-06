package com.studentapp.isu14.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.studentapp.isu14.data.dao.AppDao
import com.studentapp.isu14.data.model.CourseEntity
import com.studentapp.isu14.data.model.DepartmentEntity
import com.studentapp.isu14.data.model.NotificationEntity
import com.studentapp.isu14.data.model.RoutineEntity
import com.studentapp.isu14.data.model.SemesterEntity
import com.studentapp.isu14.data.model.StudentCourseEntity
import com.studentapp.isu14.data.model.StudentEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        StudentEntity::class,
        DepartmentEntity::class,
        SemesterEntity::class,
        CourseEntity::class,
        StudentCourseEntity::class,
        RoutineEntity::class,
        NotificationEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun appDao(): AppDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "isu_routine.db"
                )
                    .addCallback(AppDatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class AppDatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateDatabase(database.appDao())
                    }
                }
            }

            suspend fun populateDatabase(dao: AppDao) {
                dao.insertDepartments(DatabaseSeedData.departments)
                dao.insertSemesters(DatabaseSeedData.semesters)
                dao.insertCourses(DatabaseSeedData.courses)
                for (student in DatabaseSeedData.students) {
                    dao.insertStudent(student)
                }
                dao.enrollCourses(DatabaseSeedData.studentCourses)
                dao.insertRoutines(DatabaseSeedData.routines)
                dao.insertNotifications(DatabaseSeedData.notifications)
            }
        }
    }
}
