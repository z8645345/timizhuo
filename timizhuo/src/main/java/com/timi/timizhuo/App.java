package com.timi.timizhuo;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource;

@SpringBootApplication
@PropertySource("classpath:config/mybatis.properties")
@PropertySource("classpath:config/datasource.properties")
@PropertySource("classpath:config/redis.properties")
@MapperScan("com.timi.timizhuo.mapper")
public class App {
	public static void main(String[] args) {
		SpringApplication.run(App.class, args);
	}
}