package com.interviewkb;

import com.interviewkb.common.utils.IpUtil;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.TransactionManager;
import org.springframework.transaction.annotation.TransactionManagementConfigurer;
import java.util.Arrays;

@Slf4j
@SpringBootApplication
@MapperScan("com.interviewkb.mapper")
@EnableAsync
@EnableScheduling
public class InterviewKbApplication implements TransactionManagementConfigurer, CommandLineRunner {

    @Autowired
    private Environment environment;

    public static void main(String[] args) {
        SpringApplication.run(InterviewKbApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        log.info("\n----------------------------------------------------------\n\t" +
                        "Application '{}' is running! Access URLs:\n\t" +
                        "Local: \t\thttp://127.0.0.1:{}\n\t" +
                        "External: \thttp://{}:{}\n\t" +
                        "Profile(s): \t{}\n----------------------------------------------------------",
                environment.getProperty("spring.application.name"),
                environment.getProperty("server.port"),
                IpUtil.getLocalIP(),
                environment.getProperty("server.port"),
                Arrays.toString(environment.getActiveProfiles()));
    }

    @Override
    public TransactionManager annotationDrivenTransactionManager() {
        return null;
    }
}
