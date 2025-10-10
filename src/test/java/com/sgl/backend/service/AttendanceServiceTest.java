package com.sgl.backend.service;

import com.sgl.backend.entity.Attendance;
import com.sgl.backend.entity.Role;
import com.sgl.backend.entity.User;
import com.sgl.backend.exception.SglException;
import com.sgl.backend.dto.OpacUserInfo;
import com.sgl.backend.repository.AttendanceRepository;
import com.sgl.backend.repository.RoleRepository;
import com.sgl.backend.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AttendanceServiceTest {

    @Mock
    private AttendanceRepository attendanceRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private OpacService opacService;

    @InjectMocks
    private AttendanceService attendanceService;

    @Test
    void registerAttendance_existingUser_success() {
        User user = User.builder().code("12345").role(Role.builder().name("ESTUDIANTE").build()).build();
        when(userRepository.findById("12345")).thenReturn(Optional.of(user));
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX);
        when(attendanceRepository.findByUserAndTimestampBetween(user, startOfDay, endOfDay)).thenReturn(Optional.empty());
        Attendance attendance = Attendance.builder().id(1L).user(user).timestamp(LocalDateTime.now()).build();
        when(attendanceRepository.save(any(Attendance.class))).thenReturn(attendance);

        Attendance result = attendanceService.registerAttendance("12345");

        assertThat(result.getUser().getCode()).isEqualTo("12345");
        verify(attendanceRepository).save(any(Attendance.class));
    }

    @Test
    void registerAttendance_newUserFromOpac_success() {
        OpacUserInfo opacInfo = OpacUserInfo.builder().code("12345").name("John Doe").program("Ingeniería").build();
        Role role = Role.builder().id(1L).name("ESTUDIANTE").build();
        User user = User.builder().code("12345").name("John Doe").role(role).build();
        when(userRepository.findById("12345")).thenReturn(Optional.empty());
        when(opacService.fetchUserInfo("12345")).thenReturn(opacInfo);
        when(roleRepository.findByName("ESTUDIANTE")).thenReturn(Optional.of(role));
        when(userRepository.save(any(User.class))).thenReturn(user);
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX);
        when(attendanceRepository.findByUserAndTimestampBetween(any(User.class), eq(startOfDay), eq(endOfDay)))
                .thenReturn(Optional.empty());
        Attendance attendance = Attendance.builder().id(1L).user(user).timestamp(LocalDateTime.now()).build();
        when(attendanceRepository.save(any(Attendance.class))).thenReturn(attendance);

        Attendance result = attendanceService.registerAttendance("12345");

        assertThat(result.getUser().getCode()).isEqualTo("12345");
        verify(userRepository).save(any(User.class));
        verify(attendanceRepository).save(any(Attendance.class));
    }

    @Test
    void registerAttendance_duplicate_throwsException() {
        User user = User.builder().code("12345").role(Role.builder().name("ESTUDIANTE").build()).build();
        when(userRepository.findById("12345")).thenReturn(Optional.of(user));
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX);
        when(attendanceRepository.findByUserAndTimestampBetween(user, startOfDay, endOfDay))
                .thenReturn(Optional.of(Attendance.builder().build()));

        assertThrows(SglException.class, () -> attendanceService.registerAttendance("12345"),
                "Attendance already registered for user 12345 today");
    }

    @Test
    void getAttendances_byUserCode_success() {
        User user = User.builder().code("12345").build();
        Attendance attendance = Attendance.builder().id(1L).user(user).timestamp(LocalDateTime.now()).build();
        when(attendanceRepository.findByUserCode("12345")).thenReturn(List.of(attendance));

        List<Attendance> result = attendanceService.getAttendances(null, null, "12345");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUser().getCode()).isEqualTo("12345");
    }

    @Test
    void getAttendances_byDateRange_success() {
        User user = User.builder().code("12345").build();
        Attendance attendance = Attendance.builder().id(1L).user(user).timestamp(LocalDateTime.now()).build();
        LocalDateTime start = LocalDate.now().atStartOfDay();
        LocalDateTime end = LocalDate.now().atTime(LocalTime.MAX);
        when(attendanceRepository.findByTimestampBetween(start, end)).thenReturn(List.of(attendance));

        List<Attendance> result = attendanceService.getAttendances(start, end, null);

        assertThat(result).hasSize(1);
    }
}