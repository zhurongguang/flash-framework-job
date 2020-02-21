package com.flash.framework.job.schedule;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * @author zhurg
 * @date 2020/2/21 - 10:22 PM
 */
@Component
public class SpringScheduleJob {

    /**
     * 使用@Scheduled，也会记录日志到job_task_execution_log
     */
    @Scheduled(cron = "0/30 * * * * ?")
    public void springSchedule() {
        System.out.println("springSchedule execute");
    }
}