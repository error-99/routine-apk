package com.studentapp.isu14.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.studentapp.isu14.data.model.CourseEntity
import com.studentapp.isu14.data.model.StudentEntity
import com.studentapp.isu14.ui.theme.AmberAccent
import com.studentapp.isu14.ui.theme.DarkBorder
import com.studentapp.isu14.ui.theme.EmeraldAccent
import com.studentapp.isu14.ui.theme.IndigoLight
import com.studentapp.isu14.ui.theme.RoseAccent
import com.studentapp.isu14.ui.viewmodel.CourseGatingStatus
import com.studentapp.isu14.ui.viewmodel.CourseUiItem
import com.studentapp.isu14.ui.viewmodel.CoursesViewModel

@Composable
fun CoursesScreen(
    viewModel: CoursesViewModel,
    student: StudentEntity,
    onShowToast: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val enrolledCourses by viewModel.enrolledCourses.collectAsState()
    val catalogCourses by viewModel.departmentCourses.collectAsState()
    val totalCredits by viewModel.totalCredits.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var courseToDrop by remember { mutableStateOf<CourseEntity?>(null) }

    LaunchedEffect(Unit) {
        viewModel.toastEvent.collect {
            onShowToast(it)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("courses_screen")
    ) {
        // Top Header: Credits & Academic Status
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Course Registration",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${student.department} • Semester ${student.semesterId}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = "$totalCredits",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = IndigoLight
                    )
                    Text(
                        text = "Total Credits",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Tab Row: My Courses vs Catalog
        TabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = IndigoLight
        ) {
            Tab(
                selected = selectedTabIndex == 0,
                onClick = { selectedTabIndex = 0 },
                text = {
                    Text(
                        "Enrolled (${enrolledCourses.size})",
                        fontWeight = if (selectedTabIndex == 0) FontWeight.Bold else FontWeight.Normal
                    )
                }
            )
            Tab(
                selected = selectedTabIndex == 1,
                onClick = { selectedTabIndex = 1 },
                text = {
                    Text(
                        "Department Catalog",
                        fontWeight = if (selectedTabIndex == 1) FontWeight.Bold else FontWeight.Normal
                    )
                }
            )
        }

        // Search Bar (Available in Catalog view)
        if (selectedTabIndex == 1) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                placeholder = { Text("Search course code or name...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = IndigoLight) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("course_search_input"),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = IndigoLight,
                    unfocusedBorderColor = DarkBorder
                )
            )
        }

        // List Content
        if (selectedTabIndex == 0) {
            // Enrolled Courses List (LIFO order: newest enrolled shown at top!)
            if (enrolledCourses.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MenuBook,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = "No enrolled courses yet",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Button(
                            onClick = { selectedTabIndex = 1 },
                            colors = ButtonDefaults.buttonColors(containerColor = IndigoLight)
                        ) {
                            Text("Browse Catalog")
                        }
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(enrolledCourses, key = { it.courseId }) { course ->
                        EnrolledCourseCard(
                            course = course,
                            onDrop = { courseToDrop = course }
                        )
                    }
                }
            }
        } else {
            // Catalog List with Semester Gating
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(catalogCourses, key = { it.course.courseId }) { item ->
                    CatalogCourseCard(
                        item = item,
                        onEnroll = { viewModel.enroll(item.course) },
                        onDrop = { courseToDrop = item.course }
                    )
                }
            }
        }
    }

    // Drop confirmation dialog
    courseToDrop?.let { course ->
        AlertDialog(
            onDismissRequest = { courseToDrop = null },
            title = { Text("Drop Course") },
            text = { Text("Are you sure you want to drop ${course.courseCode} - ${course.courseName}? You will lose ${course.credit} credits.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.drop(course)
                        courseToDrop = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RoseAccent),
                    modifier = Modifier.testTag("confirm_drop_course_button")
                ) {
                    Text("Drop Course")
                }
            },
            dismissButton = {
                TextButton(onClick = { courseToDrop = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun EnrolledCourseCard(
    course: CourseEntity,
    onDrop: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, DarkBorder, RoundedCornerShape(12.dp)),
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = course.courseCode,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = IndigoLight
                    )
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = "${course.credit} Credits",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = "Sem ${course.semesterId}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Text(
                    text = course.courseName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (course.teacher.isNotEmpty()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.size(13.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = course.teacher,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            IconButton(
                onClick = onDrop,
                modifier = Modifier.testTag("drop_course_${course.courseId}")
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "Drop course",
                    tint = RoseAccent
                )
            }
        }
    }
}

@Composable
private fun CatalogCourseCard(
    item: CourseUiItem,
    onEnroll: () -> Unit,
    onDrop: () -> Unit
) {
    val course = item.course
    val status = item.gatingStatus

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, DarkBorder, RoundedCornerShape(12.dp)),
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = course.courseCode,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = IndigoLight
                    )
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = "${course.credit} Cr",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    // Gating Badge
                    when (status) {
                        is CourseGatingStatus.Enrolled -> {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = EmeraldAccent.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "ENROLLED",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = EmeraldAccent,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        is CourseGatingStatus.Allowed -> {
                            if (status.isRetake) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = AmberAccent.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = "RETAKE / DOWN COURSE",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = AmberAccent,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            } else {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = IndigoLight.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = "CURRENT SEMESTER",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = IndigoLight,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                        is CourseGatingStatus.Locked -> {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = RoseAccent.copy(alpha = 0.15f)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Icon(Icons.Default.Lock, null, tint = RoseAccent, modifier = Modifier.size(10.dp))
                                    Spacer(modifier = Modifier.size(3.dp))
                                    Text(
                                        text = "LOCKED (Sem ${status.requiredSemester})",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = RoseAccent,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                Text(
                    text = course.courseName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (course.teacher.isNotEmpty()) {
                    Text(
                        text = course.teacher,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Action Button
            when (status) {
                is CourseGatingStatus.Enrolled -> {
                    OutlinedButton(
                        onClick = onDrop,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = RoseAccent),
                        border = androidx.compose.foundation.BorderStroke(1.dp, RoseAccent.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Drop", style = MaterialTheme.typography.labelMedium)
                    }
                }
                is CourseGatingStatus.Allowed -> {
                    Button(
                        onClick = onEnroll,
                        colors = ButtonDefaults.buttonColors(containerColor = IndigoLight),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("enroll_course_${course.courseId}")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.size(4.dp))
                        Text("Take", style = MaterialTheme.typography.labelMedium)
                    }
                }
                is CourseGatingStatus.Locked -> {
                    // Disabled
                    OutlinedButton(
                        onClick = {},
                        enabled = false,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Locked", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }
    }
}
