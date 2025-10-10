package com.sgl.backend.controller;

import com.sgl.backend.entity.Attendance;
import com.sgl.backend.service.AttendanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/attendances")
@RequiredArgsConstructor
@Tag(name = "Attendance Management", description = "Endpoints for registering and viewing attendances")
public class AttendanceController {

    private final AttendanceService attendanceService;

    @PostMapping("/{code}")
    @Operation(summary = "Register attendance", description = "Registers attendance for a user by scanning their barcode. Restricted to ADMIN or MONITOR.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Attendance registered successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid user code or attendance already registered today"),
            @ApiResponse(responseCode = "403", description = "Forbidden: Requires ADMIN or MONITOR role"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Attendance> registerAttendance(@PathVariable String code) {
        Attendance attendance = attendanceService.registerAttendance(code);
        return ResponseEntity.ok(attendance);
    }

    @GetMapping
    @Operation(summary = "List attendances", description = "Retrieves attendance records filtered by date range or user. Restricted to ADMIN or MONITOR.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of attendances retrieved"),
            @ApiResponse(responseCode = "403", description = "Forbidden: Requires ADMIN or MONITOR role"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<List<Attendance>> getAttendances(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
            @RequestParam(required = false) String userCode) {
        List<Attendance> attendances = attendanceService.getAttendances(start, end, userCode);
        return ResponseEntity.ok(attendances);
    }
}