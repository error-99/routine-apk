package com.studentapp.isu14.data.database

import com.studentapp.isu14.data.model.CourseEntity
import com.studentapp.isu14.data.model.DepartmentEntity
import com.studentapp.isu14.data.model.NotificationEntity
import com.studentapp.isu14.data.model.RoutineEntity
import com.studentapp.isu14.data.model.SemesterEntity
import com.studentapp.isu14.data.model.StudentCourseEntity
import com.studentapp.isu14.data.model.StudentEntity

object DatabaseSeedData {

    val departments = listOf(
        DepartmentEntity("AMM", "Apparel Manufacturing & Merchandising"),
        DepartmentEntity("CSE", "Computer Science & Engineering"),
        DepartmentEntity("EEE", "Electrical & Electronic Engineering"),
        DepartmentEntity("BBA", "Bachelor of Business Administration"),
        DepartmentEntity("English", "Department of English"),
        DepartmentEntity("Textile", "Textile Engineering")
    )

    val semesters = (1..8).map {
        SemesterEntity(
            id = it,
            name = when (it) {
                1 -> "1st Semester"
                2 -> "2nd Semester"
                3 -> "3rd Semester"
                else -> "${it}th Semester"
            },
            isActive = it == 1
        )
    }

    val courses = listOf(
        // AMM Semester 1
        CourseEntity(101, "AMM 101", "Introduction to Apparel & Textile Technology", "AMM", 1, 3.0, true, "Prof. Dr. M. Rahman"),
        CourseEntity(102, "AMM 102", "Textile Fibers, Polymers & Identification", "AMM", 1, 3.0, true, "Engr. Nazmul Hossain"),
        CourseEntity(103, "AMM 103", "Fashion Illustration & Design Basics", "AMM", 1, 2.0, true, "Ms. Sarah Tarannum"),
        CourseEntity(104, "MAT 101", "Applied Mathematics for Merchandising", "AMM", 1, 3.0, true, "Dr. Anisur Rahman"),
        CourseEntity(105, "ENG 101", "Professional English & RMG Terminology", "AMM", 1, 3.0, true, "Kazi Mahbubul Alam"),

        // AMM Semester 2
        CourseEntity(106, "AMM 201", "Yarn Manufacturing Technology & Structure", "AMM", 2, 3.0, true, "Engr. Nazmul Hossain"),
        CourseEntity(107, "AMM 202", "Apparel Merchandising Fundamentals & Costing", "AMM", 2, 3.0, true, "Prof. Dr. M. Rahman"),
        CourseEntity(108, "AMM 203", "Pattern Making & Grading Techniques - I", "AMM", 2, 3.0, true, "Ms. Sarah Tarannum"),
        CourseEntity(109, "PHY 102", "Applied Physics for Textile Machinery", "AMM", 2, 3.0, true, "Dr. Farhana Islam"),
        CourseEntity(110, "CHM 101", "Textile Chemistry & Wet Processing", "AMM", 2, 3.0, true, "Dr. Rafiqul Hassan"),

        // AMM Semester 3
        CourseEntity(111, "AMM 301", "Fabric Structure & Woven Design Analysis", "AMM", 3, 3.0, true, "Engr. Nazmul Hossain"),
        CourseEntity(112, "AMM 302", "Garment Manufacturing Process - I", "AMM", 3, 3.0, true, "Prof. Dr. M. Rahman"),
        CourseEntity(113, "AMM 303", "CAD for Pattern & Marker Planning", "AMM", 3, 3.0, true, "Ms. Sarah Tarannum"),
        CourseEntity(114, "ACC 201", "Cost & Management Accounting in RMG", "AMM", 3, 3.0, true, "Dr. Anisur Rahman"),

        // AMM Semester 4
        CourseEntity(115, "AMM 401", "Dyeing, Printing & Finishing Technology", "AMM", 4, 3.0, true, "Dr. Rafiqul Hassan"),
        CourseEntity(116, "AMM 402", "Apparel Quality Assurance & AQL Inspection", "AMM", 4, 3.0, true, "Engr. Nazmul Hossain"),
        CourseEntity(117, "AMM 403", "Apparel Sourcing & Global Supply Chain", "AMM", 4, 3.0, true, "Prof. Dr. M. Rahman"),
        CourseEntity(118, "STAT 202", "Statistical Quality Control in Apparel", "AMM", 4, 3.0, true, "Dr. Anisur Rahman"),

        // AMM Semester 5
        CourseEntity(119, "AMM 501", "Garment Manufacturing Process - II", "AMM", 5, 3.0, true, "Engr. Nazmul Hossain"),
        CourseEntity(120, "AMM 502", "Industrial Engineering (IE) & Work Study", "AMM", 5, 3.0, true, "Prof. Dr. M. Rahman"),
        CourseEntity(121, "AMM 503", "Knitwear & Sweater Manufacturing", "AMM", 5, 3.0, true, "Ms. Sarah Tarannum"),
        CourseEntity(122, "MKT 301", "Fashion Branding & Retail Merchandising", "AMM", 5, 3.0, true, "Kazi Mahbubul Alam"),

        // AMM Semester 6
        CourseEntity(123, "AMM 601", "Sustainable Apparel & Social Compliance", "AMM", 6, 3.0, true, "Prof. Dr. M. Rahman"),
        CourseEntity(124, "AMM 602", "Production Planning & Control (PPC)", "AMM", 6, 3.0, true, "Engr. Nazmul Hossain"),
        CourseEntity(125, "AMM 603", "Denim & Special Garment Processing", "AMM", 6, 3.0, true, "Ms. Sarah Tarannum"),

        // CSE Semester 1
        CourseEntity(201, "CSE 111", "Structured Programming Language (C)", "CSE", 1, 3.0, true, "Dr. Faisal Kabir"),
        CourseEntity(202, "CSE 112", "Structured Programming Lab", "CSE", 1, 1.5, true, "Engr. Tanvir Ahmed"),
        CourseEntity(203, "MATH 141", "Differential and Integral Calculus", "CSE", 1, 3.0, true, "Dr. Anisur Rahman"),
        CourseEntity(204, "ENG 101", "Basic English Communication", "CSE", 1, 3.0, true, "Kazi Mahbubul Alam"),
        CourseEntity(205, "PHY 101", "Physics (Mechanics & Waves)", "CSE", 1, 3.0, true, "Dr. Farhana Islam"),

        // CSE Semester 2
        CourseEntity(206, "CSE 121", "Object Oriented Programming (Java/C++)", "CSE", 2, 3.0, true, "Dr. Faisal Kabir"),
        CourseEntity(207, "CSE 122", "OOP Laboratory", "CSE", 2, 1.5, true, "Engr. Tanvir Ahmed"),
        CourseEntity(208, "CSE 123", "Discrete Mathematics", "CSE", 2, 3.0, true, "Dr. Anisur Rahman"),
        CourseEntity(209, "MATH 143", "Linear Algebra & Complex Analysis", "CSE", 2, 3.0, true, "Dr. Farhana Islam"),

        // CSE Semester 3
        CourseEntity(210, "CSE 211", "Data Structures & Algorithms", "CSE", 3, 3.0, true, "Dr. Faisal Kabir"),
        CourseEntity(211, "CSE 212", "Data Structures Lab", "CSE", 3, 1.5, true, "Engr. Tanvir Ahmed"),
        CourseEntity(212, "CSE 213", "Digital Logic Design", "CSE", 3, 3.0, true, "Dr. Farhana Islam"),
        CourseEntity(213, "CSE 214", "Database Management Systems (SQL)", "CSE", 3, 3.0, true, "Dr. Faisal Kabir"),

        // EEE Semester 1
        CourseEntity(301, "EEE 101", "Electrical Circuit Analysis I", "EEE", 1, 3.0, true, "Dr. Kamrul Hasan"),
        CourseEntity(302, "EEE 102", "Circuit Analysis Lab", "EEE", 1, 1.5, true, "Engr. Asif Iqbal"),
        CourseEntity(303, "MATH 101", "Engineering Mathematics", "EEE", 1, 3.0, true, "Dr. Anisur Rahman"),

        // BBA Semester 1
        CourseEntity(401, "BUS 101", "Principles of Management", "BBA", 1, 3.0, true, "Prof. S. R. Chowdhury"),
        CourseEntity(402, "ACT 101", "Financial Accounting I", "BBA", 1, 3.0, true, "Ms. Tahmina Akter"),
        CourseEntity(403, "MKT 101", "Principles of Marketing", "BBA", 1, 3.0, true, "Prof. S. R. Chowdhury"),

        // English Semester 1
        CourseEntity(501, "ENG 111", "Introduction to English Literature", "English", 1, 3.0, true, "Dr. R. K. Mukherjee"),
        CourseEntity(502, "ENG 112", "History of English Language", "English", 1, 3.0, true, "Dr. R. K. Mukherjee")
    )

    val students = listOf(
        StudentEntity(
            id = 1,
            studentId = "2023100101",
            name = "Tamanna Jannat",
            passwordHash = "secretpassword",
            department = "AMM",
            batchNo = "1",
            semesterId = 1,
            totalCredits = 14.0,
            lastLogin = "2026-09-05T19:00:00"
        ),
        StudentEntity(
            id = 2,
            studentId = "2023100201",
            name = "MD Shabbir Ahmed",
            passwordHash = "secretpassword",
            department = "CSE",
            batchNo = "2",
            semesterId = 1,
            totalCredits = 13.5,
            lastLogin = "2026-09-05T19:00:00"
        )
    )

    val studentCourses = listOf(
        StudentCourseEntity(studentId = "2023100101", courseId = 101, semesterId = 1),
        StudentCourseEntity(studentId = "2023100101", courseId = 102, semesterId = 1),
        StudentCourseEntity(studentId = "2023100101", courseId = 103, semesterId = 1),
        StudentCourseEntity(studentId = "2023100101", courseId = 104, semesterId = 1),
        StudentCourseEntity(studentId = "2023100101", courseId = 105, semesterId = 1),

        StudentCourseEntity(studentId = "2023100201", courseId = 201, semesterId = 1),
        StudentCourseEntity(studentId = "2023100201", courseId = 202, semesterId = 1),
        StudentCourseEntity(studentId = "2023100201", courseId = 203, semesterId = 1),
        StudentCourseEntity(studentId = "2023100201", courseId = 204, semesterId = 1),
        StudentCourseEntity(studentId = "2023100201", courseId = 205, semesterId = 1)
    )

    val routines = listOf(
        RoutineEntity(
            id = 1001,
            courseId = 101,
            courseCode = "AMM 101",
            courseName = "Introduction to Apparel & Textile Technology",
            department = "AMM",
            semesterId = 1,
            day = "Saturday",
            type = "class",
            assessmentTag = "Regular Class",
            startTime = "09:00:00",
            endTime = "10:30:00",
            room = "Room 501 (Textile Wing)",
            teacher = "Prof. Dr. M. Rahman",
            note = "Bring textile lab safety guide"
        ),
        RoutineEntity(
            id = 1002,
            courseId = 102,
            courseCode = "AMM 102",
            courseName = "Textile Fibers, Polymers & Identification",
            department = "AMM",
            semesterId = 1,
            day = "Saturday",
            type = "class",
            assessmentTag = "Regular Class",
            startTime = "11:00:00",
            endTime = "12:30:00",
            room = "Lab Room 302",
            teacher = "Engr. Nazmul Hossain",
            note = "Fiber identification microscope lab"
        ),
        RoutineEntity(
            id = 1003,
            courseId = 101,
            courseCode = "AMM 101",
            courseName = "Introduction to Apparel & Textile Technology",
            department = "AMM",
            semesterId = 1,
            day = "Sunday",
            date = "2026-08-30",
            type = "ct1",
            assessmentTag = "CT 1",
            startTime = "09:30:00",
            endTime = "10:15:00",
            room = "Room 501",
            teacher = "Prof. Dr. M. Rahman",
            title = "Class Test 1: Natural & Synthetic Fibers",
            syllabus = "Chapters 1 to 3: Cotton, Wool, Polyester & Polymer chain structures",
            note = "Total 20 Marks. Scientific calculator allowed."
        ),
        RoutineEntity(
            id = 1004,
            courseId = 103,
            courseCode = "AMM 103",
            courseName = "Fashion Illustration & Design Basics",
            department = "AMM",
            semesterId = 1,
            day = "Monday",
            type = "lab",
            assessmentTag = "Lab Session",
            startTime = "10:00:00",
            endTime = "12:00:00",
            room = "Design Studio 401",
            teacher = "Ms. Sarah Tarannum",
            note = "Carry 8-head croquis sketching sheets"
        ),
        RoutineEntity(
            id = 1005,
            courseId = 102,
            courseCode = "AMM 102",
            courseName = "Textile Fibers, Polymers & Identification",
            department = "AMM",
            semesterId = 1,
            day = "Tuesday",
            date = "2026-09-08",
            type = "ct2",
            assessmentTag = "CT 2",
            startTime = "11:00:00",
            endTime = "11:45:00",
            room = "Lab Room 302",
            teacher = "Engr. Nazmul Hossain",
            title = "Class Test 2: Chemical Solubility & Burning Test",
            syllabus = "Solubility chart, burning test behavior, tensile strength calculation",
            note = "Total 20 Marks."
        ),
        RoutineEntity(
            id = 1006,
            courseId = 104,
            courseCode = "MAT 101",
            courseName = "Applied Mathematics for Merchandising",
            department = "AMM",
            semesterId = 1,
            day = "Wednesday",
            type = "class",
            assessmentTag = "Regular Class",
            startTime = "09:00:00",
            endTime = "10:30:00",
            room = "Room 502",
            teacher = "Dr. Anisur Rahman",
            note = "Consumption formulas & fabric calculation"
        ),
        RoutineEntity(
            id = 1007,
            courseId = 101,
            courseCode = "AMM 101",
            courseName = "Introduction to Apparel & Textile Technology",
            department = "AMM",
            semesterId = 1,
            day = "Thursday",
            date = "2026-09-22",
            type = "mid",
            assessmentTag = "Midterm Exam",
            startTime = "10:00:00",
            endTime = "12:00:00",
            room = "Central Exam Hall-A",
            teacher = "Prof. Dr. M. Rahman",
            title = "Midterm Examination: Apparel & Textile Foundation",
            syllabus = "Full syllabus of Weeks 1-7 including Spinning, Weaving & Garment Construction",
            note = "Admit card and student ID mandatory. Total 40 marks."
        ),
        RoutineEntity(
            id = 1008,
            courseId = 102,
            courseCode = "AMM 102",
            courseName = "Textile Fibers, Polymers & Identification",
            department = "AMM",
            semesterId = 1,
            day = "Saturday",
            date = "2026-10-18",
            type = "final",
            assessmentTag = "Final Exam",
            startTime = "10:00:00",
            endTime = "13:00:00",
            room = "Central Exam Hall-B",
            teacher = "Engr. Nazmul Hossain",
            title = "Semester Final Examination: AMM 102",
            syllabus = "Comprehensive final examination over full semester curriculum",
            note = "Calculators and drawing kits permitted."
        ),
        // CSE Routine
        RoutineEntity(
            id = 2001,
            courseId = 201,
            courseCode = "CSE 111",
            courseName = "Structured Programming Language (C)",
            department = "CSE",
            semesterId = 1,
            day = "Saturday",
            type = "class",
            assessmentTag = "Regular Class",
            startTime = "10:00:00",
            endTime = "11:30:00",
            room = "Room 604",
            teacher = "Dr. Faisal Kabir",
            note = "Pointers & Dynamic Memory Allocation"
        ),
        RoutineEntity(
            id = 2002,
            courseId = 201,
            courseCode = "CSE 111",
            courseName = "Structured Programming Language (C)",
            department = "CSE",
            semesterId = 1,
            day = "Monday",
            date = "2026-09-01",
            type = "ct1",
            assessmentTag = "CT 1",
            startTime = "10:00:00",
            endTime = "10:45:00",
            room = "Room 604",
            teacher = "Dr. Faisal Kabir",
            title = "Class Test 1: Loops, Arrays & Pointers",
            syllabus = "Basic C syntax, nested loops, multi-dimensional arrays",
            note = "Closed book. 20 marks."
        )
    )

    val notifications = listOf(
        NotificationEntity(
            id = 1,
            department = "AMM",
            semesterId = 1,
            courseId = 101,
            title = "📢 AMM Class Test 1 (CT-1) Schedule Announced",
            message = "Class Test 1 for AMM 101 is scheduled for Sunday, August 30 at 9:30 AM in Room 501. Topics: Natural & Synthetic Fibers.",
            type = "ct",
            link = "",
            isRead = false,
            createdAt = "2026-08-25 08:30:00"
        ),
        NotificationEntity(
            id = 2,
            department = "AMM",
            semesterId = 1,
            courseId = 101,
            title = "📅 AMM Midterm Examination Notice",
            message = "The Midterm Examination for Apparel Manufacturing & Merchandising (AMM) 1st Semester will commence on September 22, 2026.",
            type = "mid",
            link = "https://isu.edu.bd/academic-calendar",
            isRead = false,
            createdAt = "2026-08-24 14:15:00"
        ),
        NotificationEntity(
            id = 3,
            department = "ALL",
            semesterId = 1,
            courseId = null,
            title = "🏛️ University Routine App Offline Support",
            message = "The ISU Student Routine app provides instant offline routine viewing, real-time live class tracking, and course management.",
            type = "general",
            link = "",
            isRead = false,
            createdAt = "2026-08-23 10:00:00"
        )
    )
}
