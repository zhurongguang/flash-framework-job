package com.flash.framework.job.factory;

import lombok.Data;

import java.util.Map;

/**
 * @author zhurg
 * @date 2020/2/20 - 11:08 AM
 */
@Data
public abstract class ScheduleContext<J> {

    public ScheduleContext(String jobName, J jobTask, String cron, Map<String, Object> jobParams) {
        this.jobName = jobName;
        this.jobTask = jobTask;
        this.cron = cron;
        this.jobParams = jobParams;
    }

    private final String jobName;

    private final J jobTask;

    private final String cron;

    private final Map<String, Object> jobParams;
}