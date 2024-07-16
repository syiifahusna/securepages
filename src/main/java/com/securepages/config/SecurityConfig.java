package com.securepages.config;

import com.securepages.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.session.HttpSessionEventPublisher;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

@Configuration
@EnableWebSecurity
public class SecurityConfig{
    private final PasswordEncoder passwordEncoder;
    private final UserService userService;

    @Autowired
    public SecurityConfig(PasswordEncoder passwordEncoder, UserService userService) {
        this.passwordEncoder = passwordEncoder;
        this.userService = userService;
    }

//    @Bean
//    public AuthenticationSuccessHandler myAuthenticationSuccessHandler() {
//        return (request, response, authentication) -> {
//            if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
//                response.sendRedirect("/admin/home");
//            } else if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_GUEST"))) {
//                response.sendRedirect("/guest/home");
//            }
//        };
//    }

    @Bean
    public AuthenticationSuccessHandler myAuthenticationSuccessHandler() {
        return (request, response, authentication) -> {
            boolean isAdmin = false;
            boolean isGuest = false;
            for (GrantedAuthority authority : authentication.getAuthorities()) {
                if (authority.getAuthority().equals("ADMIN")) {
                    isAdmin = true;
                } else if (authority.getAuthority().equals("GUEST")) {
                    isGuest = true;
                }
            }
            if (isAdmin) {
                response.sendRedirect("/securepages/admin/home");
            } else if (isGuest) {
                response.sendRedirect("/securepages/guest/home");
            }
        };
    }

    @Bean
    public AuthenticationFailureHandler myAuthenticationFailureHandler() {
        return (request, response, exception) -> response.sendRedirect("/securepages/login?err=wrong credential");
    }

    @Bean
    public HttpSessionEventPublisher httpSessionEventPublisher() {
        return new HttpSessionEventPublisher();
    }

    @Configuration
    @Order(1)
    public class AdminSecurityConfig extends WebSecurityConfigurerAdapter {
        @Override
        protected void configure(HttpSecurity http) throws Exception {
            http
                    .antMatcher("/admin/**")
                        .authorizeRequests()
                        .anyRequest().hasAuthority("ADMIN")
                    .and()
                    .formLogin()
                        .loginPage("/login")
                        .successHandler(myAuthenticationSuccessHandler())
                        .failureHandler(myAuthenticationFailureHandler())
                    .and()
                    .logout()
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/securepages/login?msg=you have logout")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                    .and()
                    .sessionManagement()
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                        .sessionFixation().migrateSession()
                        .maximumSessions(1)
                        .expiredUrl("/securepages/login?err=session expired");
        }
    }

    @Configuration
    @Order(2)
    public class GuestSecurityConfig extends WebSecurityConfigurerAdapter {
        @Override
        protected void configure(HttpSecurity http) throws Exception {
            http
                    .antMatcher("/guest/**")
                        .authorizeRequests()
                        .anyRequest().hasAuthority("GUEST")
                    .and()
                        .formLogin()
                        .loginPage("/login")
                        .successHandler(myAuthenticationSuccessHandler())
                        .failureHandler(myAuthenticationFailureHandler())
                    .and()
                    .logout()
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/securepages/login?msg=you have logout")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                    .and()
                    .sessionManagement()
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                        .sessionFixation().migrateSession()
                        .maximumSessions(1)
                        .expiredUrl("/securepages/login?err=session expired");
        }
    }

    @Configuration
    @Order(3)
    public class PublicSecurityConfig extends WebSecurityConfigurerAdapter {

        @Override
        protected void configure(AuthenticationManagerBuilder auth) throws Exception {
            auth.userDetailsService(userService).passwordEncoder(passwordEncoder);
        }

        @Bean
        @Override
        public AuthenticationManager authenticationManagerBean() throws Exception {
            return super.authenticationManagerBean();
        }


        @Override
        protected void configure(HttpSecurity http) throws Exception {
            http
                    .authorizeRequests()
                        .antMatchers("/gallery").permitAll()
                        .antMatchers("/login",
                                "/register",
                                "/account_confirmation",
                                "/forgot_password",
                                "/reset_password",
                                "/index",
                                "/")
                            .not().fullyAuthenticated()
                    .and()
                    .formLogin()
                        .loginPage("/login")
                        .successHandler(myAuthenticationSuccessHandler())
                        .failureHandler(myAuthenticationFailureHandler())
                    .and()
                    .logout()
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/securepages/login?msg=you have logout")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                    .and()
                    .sessionManagement()
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                        .sessionFixation().migrateSession()
                        .maximumSessions(1)
                        .expiredUrl("/securepages/login?err=session expired")
                    .and()
                    .and()
                    .csrf()
                    .and()
                    .rememberMe()
                        .rememberMeParameter("rememberMe")
                        .tokenValiditySeconds(604800) // Set token validity (7 days)
                    .and()
                    .userDetailsService(userService);
        }
    }

}
