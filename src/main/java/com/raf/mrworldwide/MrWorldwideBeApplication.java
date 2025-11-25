package com.raf.mrworldwide;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class MrWorldwideBeApplication {

	public static void main(String[] args) {
		SpringApplication.run(MrWorldwideBeApplication.class, args);
	}

}
