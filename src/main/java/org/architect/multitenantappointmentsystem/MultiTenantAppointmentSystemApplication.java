package org.architect.multitenantappointmentsystem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@SpringBootApplication
public class MultiTenantAppointmentSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(MultiTenantAppointmentSystemApplication.class, args);
    }

    @Bean
    public WebMvcConfigurer viewControllerConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addViewControllers(ViewControllerRegistry registry) {
                registry.addRedirectViewController("/", "/dashboard/tenants");
                registry.addViewController("/dashboard/tenants").setViewName("tenants");
                registry.addViewController("/dashboard/tenant/**").setViewName("tenant-detail");
                registry.addViewController("/dashboard/tenant/*/book/*").setViewName("booking");
                registry.addViewController("/dashboard/login").setViewName("login");
                registry.addViewController("/dashboard/register").setViewName("register");
                registry.addViewController("/dashboard/profile").setViewName("profile");
            }
        };
    }
}
