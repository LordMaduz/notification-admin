package com.admin.notification;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.reactive.config.EnableWebFlux;


@SpringBootApplication
@EnableWebFlux
@OpenAPIDefinition(info = @Info(title = "Notification Admin Service", version = "1.0", description = "Notification Admin Service"))
public class NotificationAdminApplication {

	public static void main(String[] args) {
		SpringApplication.run(NotificationAdminApplication.class, args);
	}

}

