// RingApplication.java
package com.ring.Ring;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableAsync
@EnableScheduling
public class RingApplication {

	public static void main(String[] args) {
		SpringApplication.run(RingApplication.class, args);
	}
}