package com.flash.framework.job;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.concurrent.CountDownLatch;

/**
 * @author zhurg
 * @date 2019/8/19 - 下午5:08
 */
@SpringBootApplication
public class JobTaskApplication {


    public static void main(String[] args) {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        try {
            SpringApplication.run(JobTaskApplication.class, args);
            countDownLatch.await();
        } catch (Exception e) {
            e.printStackTrace();
            countDownLatch.countDown();
        }
    }
}