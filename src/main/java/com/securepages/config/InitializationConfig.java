package com.securepages.config;

import com.securepages.model.Role;
import com.securepages.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.Transactional;

@Configuration
public class InitializationConfig {

    @Autowired
    private RoleRepository roleRepository;

    @Bean
    @Transactional
    public void initData() {

        Role roleGuest = new Role("GUEST");
        Role roleAdmin = new Role("ADMIN");

        roleRepository.save(roleGuest);
        roleRepository.save(roleAdmin);
    }

}
