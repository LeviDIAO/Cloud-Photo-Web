package com.cloud.photo.trans;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * @Author：kkoneone11
 * @name：CloudPhotoTransApplication
 * @Date：2023/7/13 14:57
 */

@MapperScan(basePackages = {"com.cloud.photo.trans.mapper"})
@EnableDiscoveryClient
@SpringBootApplication
//@EnableFeignClients(basePackages = {"com.cloud.photo"})
public class CloudPhotoTransApplication {
    public static void main(String[] args) {
        SpringApplication.run(CloudPhotoTransApplication.class,args);
    }
}
