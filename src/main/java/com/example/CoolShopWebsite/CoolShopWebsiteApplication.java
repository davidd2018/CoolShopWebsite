package com.example.CoolShopWebsite;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties
public class CoolShopWebsiteApplication {

	public static void main(String[] args) {
		SpringApplication.run(CoolShopWebsiteApplication.class, args);
	}

}
