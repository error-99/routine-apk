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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.CorporateFare
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.studentapp.isu14.data.model.StudentEntity
import com.studentapp.isu14.ui.theme.DarkBorder
import com.studentapp.isu14.ui.theme.IndigoLight
import com.studentapp.isu14.ui.theme.RoseAccent
import com.studentapp.isu14.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    mainViewModel: MainViewModel,
    student: StudentEntity,
    modifier: Modifier = Modifier
) {
    val departments by mainViewModel.departments.collectAsState()
    val semesters by mainViewModel.semesters.collectAsState()

    var showEditNameDialog by remember { mutableStateOf(false) }
    var showEditIdDialog by remember { mutableStateOf(false) }
    var showChangeDeptDialog by remember { mutableStateOf(false) }
    var showChangeBatchDialog by remember { mutableStateOf(false) }
    var showChangeSemesterDialog by remember { mutableStateOf(false) }
    var showChangePasswordDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("profile_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Identity Banner Card
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .border(1.dp, DarkBorder, RoundedCornerShape(20.dp)),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(IndigoLight.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = student.name.take(2).uppercase(),
                            style = MaterialTheme.typography.headlineMedium,
                            color = IndigoLight,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = student.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Student ID: ${student.studentId}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Stat Pills Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        ProfileStatItem(label = "Department", value = student.department)
                        ProfileStatItem(label = "Semester", value = "${student.semesterId}th")
                        ProfileStatItem(label = "Batch", value = student.batchNo)
                        ProfileStatItem(label = "Credits", value = "${student.totalCredits}")
                    }
                }
            }
        }

        // Academic Settings Section
        item {
            Text(
                text = "Academic Settings",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
            ) {
                Column {
                    ProfileMenuRow(
                        icon = Icons.Default.CorporateFare,
                        title = "Change Department",
                        subtitle = "Currently: ${student.department}",
                        onClick = { showChangeDeptDialog = true },
                        testTag = "change_department_row"
                    )
                    ProfileDivider()
                    ProfileMenuRow(
                        icon = Icons.Default.School,
                        title = "Change Semester",
                        subtitle = "Currently: Semester ${student.semesterId}",
                        onClick = { showChangeSemesterDialog = true },
                        testTag = "change_semester_row"
                    )
                    ProfileDivider()
                    ProfileMenuRow(
                        icon = Icons.Default.Groups,
                        title = "Update Batch Number",
                        subtitle = "Currently: Batch ${student.batchNo}",
                        onClick = { showChangeBatchDialog = true },
                        testTag = "change_batch_row"
                    )
                }
            }
        }

        // Account Details Section
        item {
            Text(
                text = "Account Details",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
            ) {
                Column {
                    ProfileMenuRow(
                        icon = Icons.Default.Edit,
                        title = "Edit Full Name",
                        subtitle = student.name,
                        onClick = { showEditNameDialog = true },
                        testTag = "edit_name_row"
                    )
                    ProfileDivider()
                    ProfileMenuRow(
                        icon = Icons.Default.Badge,
                        title = "Change Student ID",
                        subtitle = student.studentId,
                        onClick = { showEditIdDialog = true },
                        testTag = "edit_id_row"
                    )
                    ProfileDivider()
                    ProfileMenuRow(
                        icon = Icons.Default.Lock,
                        title = "Change Password",
                        subtitle = "Security & portal credentials",
                        onClick = { showChangePasswordDialog = true },
                        testTag = "change_password_row"
                    )
                }
            }
        }

        // Widget & Cloud Sync Section
        item {
            Text(
                text = "Widget & Cloud Sync",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        item {
            val context = androidx.compose.ui.platform.LocalContext.current
            val isOnline by mainViewModel.isOnline.collectAsState()
            val syncState by mainViewModel.syncState.collectAsState()

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
            ) {
                Column {
                    ProfileMenuRow(
                        icon = Icons.Default.Widgets,
                        title = "Add Home Screen Widget",
                        subtitle = "View Today & Tomorrow classes directly on home screen",
                        onClick = {
                            com.studentapp.isu14.widget.WidgetHelper.requestPinWidget(context)
                        },
                        testTag = "pin_widget_profile_row"
                    )
                    ProfileDivider()
                    ProfileMenuRow(
                        icon = Icons.Default.CloudSync,
                        title = "Sync with Firebase Cloud",
                        subtitle = if (isOnline) {
                            if (syncState.lastSyncTime.isNotEmpty()) "Last synced: ${syncState.lastSyncTime}" else "Connected • Tap to sync latest"
                        } else {
                            "Offline • Showing saved local data"
                        },
                        onClick = {
                            mainViewModel.manualSync()
                        },
                        testTag = "manual_sync_row"
                    )
                }
            }
        }

        // Logout Button
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .clickable { showLogoutDialog = true }
                    .border(1.dp, RoseAccent.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                    .testTag("logout_button_row"),
                color = RoseAccent.copy(alpha = 0.08f),
                shape = RoundedCornerShape(14.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Logout,
                        contentDescription = null,
                        tint = RoseAccent,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Sign Out",
                        style = MaterialTheme.typography.labelLarge,
                        color = RoseAccent,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }

    // --- Dialogs ---

    // Edit Name Dialog
    if (showEditNameDialog) {
        var tempName by remember { mutableStateOf(student.name) }
        AlertDialog(
            onDismissRequest = { showEditNameDialog = false },
            title = { Text("Edit Name") },
            text = {
                OutlinedTextField(
                    value = tempName,
                    onValueChange = { tempName = it },
                    label = { Text("Full Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        mainViewModel.updateName(tempName)
                        showEditNameDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = IndigoLight)
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditNameDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Edit Student ID Dialog
    if (showEditIdDialog) {
        var tempId by remember { mutableStateOf(student.studentId) }
        AlertDialog(
            onDismissRequest = { showEditIdDialog = false },
            title = { Text("Change Student ID") },
            text = {
                OutlinedTextField(
                    value = tempId,
                    onValueChange = { tempId = it },
                    label = { Text("10-Digit Student ID") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        mainViewModel.updateStudentId(tempId)
                        showEditIdDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = IndigoLight)
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditIdDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Change Department Dialog
    if (showChangeDeptDialog) {
        var selectedDept by remember { mutableStateOf(student.department) }
        var expanded by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showChangeDeptDialog = false },
            title = { Text("Change Department") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Changing department will reset current enrolled courses and load new defaults.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded }
                    ) {
                        OutlinedTextField(
                            value = selectedDept,
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            modifier = Modifier
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            departments.forEach { dept ->
                                DropdownMenuItem(
                                    text = { Text("${dept.code} - ${dept.name}") },
                                    onClick = {
                                        selectedDept = dept.code
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        mainViewModel.changeDepartment(selectedDept)
                        showChangeDeptDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = IndigoLight)
                ) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(onClick = { showChangeDeptDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Change Semester Dialog
    if (showChangeSemesterDialog) {
        var selectedSem by remember { mutableStateOf(student.semesterId) }
        var expanded by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showChangeSemesterDialog = false },
            title = { Text("Change Semester") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Changing semester updates course eligibility and default semester courses.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded }
                    ) {
                        OutlinedTextField(
                            value = "Semester $selectedSem",
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            modifier = Modifier
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            semesters.forEach { sem ->
                                DropdownMenuItem(
                                    text = { Text(sem.name) },
                                    onClick = {
                                        selectedSem = sem.id
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        mainViewModel.changeSemester(selectedSem)
                        showChangeSemesterDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = IndigoLight)
                ) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(onClick = { showChangeSemesterDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Change Batch Dialog
    if (showChangeBatchDialog) {
        var tempBatch by remember { mutableStateOf(student.batchNo) }
        AlertDialog(
            onDismissRequest = { showChangeBatchDialog = false },
            title = { Text("Update Batch Number") },
            text = {
                OutlinedTextField(
                    value = tempBatch,
                    onValueChange = { tempBatch = it },
                    label = { Text("Batch Number (e.g. 1, 2, 3)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        mainViewModel.updateBatch(tempBatch)
                        showChangeBatchDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = IndigoLight)
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showChangeBatchDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Change Password Dialog
    if (showChangePasswordDialog) {
        var oldPassword by remember { mutableStateOf("") }
        var newPassword by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showChangePasswordDialog = false },
            title = { Text("Change Password") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = oldPassword,
                        onValueChange = { oldPassword = it },
                        label = { Text("Current Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        label = { Text("New Password (min 4 chars)") },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        mainViewModel.changePassword(oldPassword, newPassword)
                        showChangePasswordDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = IndigoLight)
                ) {
                    Text("Update")
                }
            },
            dismissButton = {
                TextButton(onClick = { showChangePasswordDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Logout Dialog
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Sign Out") },
            text = { Text("Are you sure you want to sign out of the ISU Student Routine app?") },
            confirmButton = {
                Button(
                    onClick = {
                        mainViewModel.logout()
                        showLogoutDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RoseAccent),
                    modifier = Modifier.testTag("confirm_logout_button")
                ) {
                    Text("Sign Out")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun ProfileStatItem(label: String, value: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = IndigoLight
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ProfileMenuRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(testTag)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(IndigoLight.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = IndigoLight,
                    modifier = Modifier.size(18.dp)
                )
            }

            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.size(14.dp)
        )
    }
}

@Composable
private fun ProfileDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(1.dp)
            .background(DarkBorder)
    )
}
