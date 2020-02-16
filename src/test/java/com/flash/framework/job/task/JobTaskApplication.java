package com.flash.framework.job.task;

import com.flash.framework.job.task.execution.JobExecutionService;
import com.flash.framework.job.task.execution.jdbc.JdbcJobExecutionService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

/**
 * @author zhurg
 * @date 2019/8/19 - 下午5:08
 */
@SpringBootApplication
public class JobTaskApplication {

    @Bean
    @Primary
    public JobExecutionService jobExecutionService() {
        return new JdbcJobExecutionService();
    }

    public static void main(String[] args) {
        SpringApplication.run(JobTaskApplication.class, args);
        try {
            Thread.sleep(100000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}