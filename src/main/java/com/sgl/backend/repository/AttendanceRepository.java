package com.sgl.backend.repository;

import com.sgl.backend.entity.Attendance;
import com.sgl.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    Optional<Attendance> findByUserAndTimestampBetween(User user, LocalDateTime start, LocalDateTime end);
    List<Attendance> findByUserCode(String userCode);
    List<Attendance> findByTimestampBetween(LocalDateTime start, LocalDateTime end);
}
