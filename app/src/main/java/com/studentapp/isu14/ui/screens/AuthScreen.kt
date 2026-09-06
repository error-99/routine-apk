package com.studentapp.isu14.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.studentapp.isu14.ui.theme.DarkBorder
import com.studentapp.isu14.ui.theme.IndigoLight
import com.studentapp.isu14.ui.theme.IndigoPrimary
import com.studentapp.isu14.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    mainViewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val departments by mainViewModel.departments.collectAsState()
    val semesters by mainViewModel.semesters.collectAsState()
    val isLoading by mainViewModel.isLoading.collectAsState()

    var isRegisterMode by remember { mutableStateOf(false) }

    var studentId by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var selectedDepartment by remember { mutableStateOf("AMM") }
    var selectedSemester by remember { mutableIntStateOf(1) }
    var batchNo by remember { mutableStateOf("1") }

    var deptExpanded by remember { mutableStateOf(false) }
    var semExpanded by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        IndigoPrimary.copy(alpha = 0.15f),
                        MaterialTheme.colorScheme.background
                    )
                )
            )
            .testTag("auth_screen"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // App Branding Header
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(IndigoLight.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.School,
                    contentDescription = null,
                    tint = IndigoLight,
                    modifier = Modifier.size(36.dp)
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "ISU Routine Portal",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Academic Routine & Student Dashboard",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Auth Card
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .border(1.dp, DarkBorder, RoundedCornerShape(20.dp)),
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(20.dp),
                tonalElevation = 4.dp
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Segmented Toggle: Sign In vs Register
                    TabRow(
                        selectedTabIndex = if (isRegisterMode) 1 else 0,
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        contentColor = IndigoLight,
                        modifier = Modifier.clip(RoundedCornerShape(10.dp))
                    ) {
                        Tab(
                            selected = !isRegisterMode,
                            onClick = { isRegisterMode = false },
                            text = { Text("Sign In", fontWeight = FontWeight.SemiBold) },
                            modifier = Modifier.testTag("tab_sign_in")
                        )
                        Tab(
                            selected = isRegisterMode,
                            onClick = { isRegisterMode = true },
                            text = { Text("Register", fontWeight = FontWeight.SemiBold) },
                            modifier = Modifier.testTag("tab_register")
                        )
                    }

                    // Registration extra fields
                    if (isRegisterMode) {
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Full Name") },
                            leadingIcon = { Icon(Icons.Default.Person, null, tint = IndigoLight) },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("register_name_input"),
                            shape = RoundedCornerShape(12.dp)
                        )

                        // Department Dropdown
                        ExposedDropdownMenuBox(
                            expanded = deptExpanded,
                            onExpandedChange = { deptExpanded = !deptExpanded }
                        ) {
                            OutlinedTextField(
                                value = departments.find { it.code == selectedDepartment }?.name
                                    ?: selectedDepartment,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Department") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = deptExpanded) },
                                modifier = Modifier
                                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                    .fillMaxWidth()
                                    .testTag("register_dept_dropdown"),
                                shape = RoundedCornerShape(12.dp)
                            )
                            ExposedDropdownMenu(
                                expanded = deptExpanded,
                                onDismissRequest = { deptExpanded = false }
                            ) {
                                departments.forEach { dept ->
                                    DropdownMenuItem(
                                        text = { Text("${dept.code} - ${dept.name}") },
                                        onClick = {
                                            selectedDepartment = dept.code
                                            deptExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        // Semester Dropdown
                        ExposedDropdownMenuBox(
                            expanded = semExpanded,
                            onExpandedChange = { semExpanded = !semExpanded }
                        ) {
                            OutlinedTextField(
                                value = "Semester $selectedSemester",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Semester") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = semExpanded) },
                                modifier = Modifier
                                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                    .fillMaxWidth()
                                    .testTag("register_semester_dropdown"),
                                shape = RoundedCornerShape(12.dp)
                            )
                            ExposedDropdownMenu(
                                expanded = semExpanded,
                                onDismissRequest = { semExpanded = false }
                            ) {
                                semesters.forEach { sem ->
                                    DropdownMenuItem(
                                        text = { Text(sem.name) },
                                        onClick = {
                                            selectedSemester = sem.id
                                            semExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        OutlinedTextField(
                            value = batchNo,
                            onValueChange = { batchNo = it },
                            label = { Text("Batch Number (e.g. 1)") },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("register_batch_input"),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    // Student ID Field
                    OutlinedTextField(
                        value = studentId,
                        onValueChange = { if (it.all { ch -> ch.isDigit() }) studentId = it },
                        label = { Text("Student ID (Numbers only, min 10 digits)") },
                        leadingIcon = { Icon(Icons.Default.Badge, null, tint = IndigoLight) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("auth_student_id_input"),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = IndigoLight,
                            unfocusedBorderColor = DarkBorder
                        )
                    )

                    // Password Field
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password (min 4 characters)") },
                        leadingIcon = { Icon(Icons.Default.Lock, null, tint = IndigoLight) },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("auth_password_input"),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = IndigoLight,
                            unfocusedBorderColor = DarkBorder
                        )
                    )

                    // Submit Button
                    Button(
                        onClick = {
                            if (isRegisterMode) {
                                mainViewModel.register(
                                    studentId = studentId,
                                    name = name,
                                    password = password,
                                    department = selectedDepartment,
                                    batchNo = batchNo,
                                    semesterId = selectedSemester
                                )
                            } else {
                                mainViewModel.login(studentId, password)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("auth_submit_button"),
                        enabled = !isLoading && studentId.length >= 10 && password.length >= 4,
                        colors = ButtonDefaults.buttonColors(containerColor = IndigoLight),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = if (isRegisterMode) "Create Account" else "Sign In",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Quick Demo Accounts Fill
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Quick Demo Accounts (Instant Fill):",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    studentId = "2023100101"
                                    password = "secretpassword"
                                    isRegisterMode = false
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("quick_fill_amm_demo"),
                                shape = RoundedCornerShape(8.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, IndigoLight.copy(alpha = 0.4f))
                            ) {
                                Text("AMM: 2023100101", style = MaterialTheme.typography.labelSmall, color = IndigoLight)
                            }

                            OutlinedButton(
                                onClick = {
                                    studentId = "2023100201"
                                    password = "secretpassword"
                                    isRegisterMode = false
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("quick_fill_cse_demo"),
                                shape = RoundedCornerShape(8.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, IndigoLight.copy(alpha = 0.4f))
                            ) {
                                Text("CSE: 2023100201", style = MaterialTheme.typography.labelSmall, color = IndigoLight)
                            }
                        }
                    }
                }
            }
        }
    }
}
