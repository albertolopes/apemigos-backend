package org.apemigos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class ApemigosApplication {

	public static void main(String[] args) {
		SpringApplication.run(ApemigosApplication.class, args);
	}

}
