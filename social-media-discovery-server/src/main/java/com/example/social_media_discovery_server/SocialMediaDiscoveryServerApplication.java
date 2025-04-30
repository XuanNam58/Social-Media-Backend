package com.example.social_media_discovery_server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@SpringBootApplication
@EnableEurekaServer
public class SocialMediaDiscoveryServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(SocialMediaDiscoveryServerApplication.class, args);
	}

}
