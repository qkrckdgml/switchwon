package com.ch.switchwon;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SwitchwonApplication {

	public static void main(String[] args) {
		SpringApplication.run(SwitchwonApplication.class, args);
	}
}
