package com.sgl.backend.service;

import com.sgl.backend.entity.Attendance;
import com.sgl.backend.entity.Role;
import com.sgl.backend.entity.User;
import com.sgl.backend.exception.SglException;
import com.sgl.backend.dto.OpacUserInfo;
import com.sgl.backend.repository.AttendanceRepository;
import com.sgl.backend.repository.RoleRepository;
import com.sgl.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final OpacService opacService;

    public Attendance registerAttendance(String code) {
        User user = userRepository.findById(code).orElse(null);
        if (user == null) {
            OpacUserInfo opacInfo = opacService.fetchUserInfo(code);
            Role role = roleRepository.findByName("ESTUDIANTE")
                    .orElseThrow(() -> new SglException("Default role ESTUDIANTE not found"));
            user = User.builder()
                    .code(opacInfo.getCode())
                    .name(opacInfo.getName())
                    .email(opacInfo.getEmail())
                    .document(opacInfo.getDocument())
                    .role(role)
                    .build();
            user = userRepository.save(user);
        }

        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX);
        if (attendanceRepository.findByUserAndTimestampBetween(user, startOfDay, endOfDay).isPresent()) {
            throw new SglException("Attendance already registered for user " + code + " today");
        }

        Attendance attendance = Attendance.builder()
                .user(user)
                .timestamp(LocalDateTime.now())
                .build();
        return attendanceRepository.save(attendance);
    }

    public List<Attendance> getAttendances(LocalDateTime start, LocalDateTime end, String userCode) {
        if (userCode != null && !userCode.isEmpty()) {
            return attendanceRepository.findByUserCode(userCode);
        }
        if (start != null && end != null) {
            return attendanceRepository.findByTimestampBetween(start, end);
        }
        return attendanceRepository.findAll();
    }
}