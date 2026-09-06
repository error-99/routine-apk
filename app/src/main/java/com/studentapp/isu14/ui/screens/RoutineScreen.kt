package com.studentapp.isu14.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.studentapp.isu14.data.model.RoutineEntity
import com.studentapp.isu14.data.model.StudentEntity
import com.studentapp.isu14.ui.components.LiveClassCard
import com.studentapp.isu14.ui.components.RoutineDetailDialog
import com.studentapp.isu14.ui.theme.AmberAccent
import com.studentapp.isu14.ui.theme.CyanAccent
import com.studentapp.isu14.ui.theme.DarkBorder
import com.studentapp.isu14.ui.theme.EmeraldAccent
import com.studentapp.isu14.ui.theme.IndigoLight
import com.studentapp.isu14.ui.theme.PurpleAccent
import com.studentapp.isu14.ui.theme.RoseAccent
import com.studentapp.isu14.ui.viewmodel.RoutineViewModel

@Composable
fun RoutineScreen(
    viewModel: RoutineViewModel,
    student: StudentEntity,
    modifier: Modifier = Modifier
) {
    val liveStatus by viewModel.liveClassStatus.collectAsState()
    val routines by viewModel.filteredRoutines.collectAsState()
    val upcomingCt by viewModel.upcomingCt.collectAsState()
    val selectedDay by viewModel.selectedDay.collectAsState()
    val selectedType by viewModel.selectedType.collectAsState()
    val detailRoutine by viewModel.detailRoutine.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("routine_screen"),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Greeting Card
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Hello, ${student.name.split(" ").firstOrNull() ?: "Student"} 👋",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${student.department} • Semester ${student.semesterId} • Batch ${student.batchNo}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    val context = androidx.compose.ui.platform.LocalContext.current
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)),
                            modifier = Modifier
                                .testTag("pin_widget_button")
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    com.studentapp.isu14.widget.WidgetHelper.requestPinWidget(context)
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = "Add Widget",
                                    modifier = Modifier.size(14.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Add Widget",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = IndigoLight.copy(alpha = 0.15f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, IndigoLight.copy(alpha = 0.3f))
                        ) {
                            Text(
                                text = "ID: ${student.studentId}",
                                style = MaterialTheme.typography.labelSmall,
                                color = IndigoLight,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                            )
                        }
                    }
                }
            }
        }

        // Live Class Tracker Hero Widget
        item {
            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                LiveClassCard(status = liveStatus)
            }
        }

        // Upcoming CT Spotlight Banner
        if (upcomingCt != null) {
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .testTag("upcoming_ct_spotlight")
                        .clickable { viewModel.showDetail(upcomingCt!!) },
                    shape = RoundedCornerShape(14.dp),
                    color = AmberAccent.copy(alpha = 0.1f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AmberAccent.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(AmberAccent.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = AmberAccent,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "UPCOMING ${upcomingCt!!.assessmentTag.uppercase()}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = AmberAccent,
                                    fontWeight = FontWeight.Bold
                                )
                                if (!upcomingCt!!.date.isNullOrEmpty()) {
                                    Text(
                                        text = "• ${upcomingCt!!.date}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Text(
                                text = "${upcomingCt!!.courseCode}: ${upcomingCt!!.courseName}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1
                            )
                            Text(
                                text = "Room ${upcomingCt!!.room} • ${upcomingCt!!.startTime.take(5)} - ${upcomingCt!!.endTime.take(5)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Day Selector Bar (Horizontal Scroll)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Weekly Schedule",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(viewModel.daysList) { day ->
                        val isSelected = selectedDay == day
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (isSelected) IndigoLight else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .testTag("day_pill_$day")
                                .clip(RoundedCornerShape(20.dp))
                                .clickable { viewModel.setDay(day) },
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) IndigoLight else DarkBorder
                            )
                        ) {
                            Text(
                                text = day,
                                style = MaterialTheme.typography.labelMedium,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }
        }

        // Category Filter Chips
        item {
            val filterOptions = listOf(
                "all" to "All Classes",
                "class" to "Lectures",
                "ct" to "Class Tests",
                "mid" to "Midterm",
                "final" to "Finals",
                "lab" to "Labs"
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                filterOptions.forEach { (typeKey, label) ->
                    val isSelected = selectedType == typeKey
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.setType(typeKey) },
                        label = {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = IndigoLight.copy(alpha = 0.2f),
                            selectedLabelColor = IndigoLight
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = if (isSelected) IndigoLight else DarkBorder
                        )
                    )
                }
            }
        }

        // Routines List
        if (routines.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = "No classes scheduled for $selectedDay",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Enjoy your day off or review your coursework!",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        } else {
            items(routines, key = { it.id }) { routine ->
                RoutineCardItem(
                    routine = routine,
                    onClick = { viewModel.showDetail(routine) },
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }
    }

    // Detail Dialog
    detailRoutine?.let { routine ->
        RoutineDetailDialog(
            routine = routine,
            onDismiss = { viewModel.closeDetail() }
        )
    }
}

@Composable
private fun RoutineCardItem(
    routine: RoutineEntity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tagColor = when {
        routine.type.startsWith("ct") -> AmberAccent
        routine.type == "mid" -> PurpleAccent
        routine.type == "final" -> RoseAccent
        routine.type == "lab" -> CyanAccent
        else -> IndigoLight
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("routine_card_${routine.id}")
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .border(1.dp, DarkBorder, RoundedCornerShape(14.dp)),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            // Left color accent bar
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(90.dp)
                    .background(tagColor)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Top row: Type Tag & Time
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = tagColor.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = routine.assessmentTag.ifEmpty { routine.type.uppercase() },
                            style = MaterialTheme.typography.labelSmall,
                            color = tagColor,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            modifier = Modifier.size(13.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${routine.startTime.take(5)} - ${routine.endTime.take(5)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Course Code and Name
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = routine.courseCode,
                        style = MaterialTheme.typography.labelMedium,
                        color = IndigoLight,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = routine.courseName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1
                    )
                }

                // Room and Teacher
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MeetingRoom,
                            contentDescription = null,
                            modifier = Modifier.size(13.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = routine.room,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (routine.teacher.isNotEmpty()) {
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
                                text = routine.teacher.split(" ").takeLast(2).joinToString(" "),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
