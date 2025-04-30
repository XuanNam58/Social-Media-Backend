package com.example.social_media_api_gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class SocialMediaApiGatewayApplication {

	public static void main(String[] args) {
		SpringApplication.run(SocialMediaApiGatewayApplication.class, args);
	}

}
