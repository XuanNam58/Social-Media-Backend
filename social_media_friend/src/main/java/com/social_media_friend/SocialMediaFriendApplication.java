package com.social_media_friend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class SocialMediaFriendApplication {

	public static void main(String[] args) {
		SpringApplication.run(SocialMediaFriendApplication.class, args);
	}

}
