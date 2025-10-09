package com.sgl.backend.repository;

import com.sgl.backend.entity.Role;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
@DataJpaTest
class RoleRepositoryTest {
    @Autowired
    private RoleRepository roleRepository;

    @Test
    void findByName_existingRole_returnsRole() {
        Role role = Role.builder().name("TEST_ROLE").build();
        roleRepository.save(role);

        Optional<Role> found = roleRepository.findByName("TEST_ROLE");
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("TEST_ROLE");
    }

    @Test 
    void findByName_nonExistingRole_returnsEmpty() {
        Optional<Role> found = roleRepository.findByName("NON_EXISTENT_ROLE");
        assertThat(found).isNotPresent();
    }
}
