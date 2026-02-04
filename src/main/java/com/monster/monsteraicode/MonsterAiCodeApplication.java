package com.monster.monsteraicode;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MonsterAiCodeApplication {

/**
 * Spring Boot应用程序的主入口方法
 * 通过调用SpringApplication的静态run方法来启动应用程序
 *
 * @param args 命令行参数，可以在启动应用程序时传入
 */
    public static void main(String[] args) {
        // 使用SpringApplication类的静态run方法启动应用程序
        // 第一个参数是应用程序的主类(带有@SpringBootApplication注解的类)
        // 第二个参数是命令行参数数组
        SpringApplication.run(MonsterAiCodeApplication.class, args);
    }

}
