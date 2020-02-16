package com.flash.framework.job.task.execution.compensate.job;

import com.flash.framework.job.task.execution.compensate.CompensateService;
import com.flash.framework.job.task.task.Job;
import com.flash.framework.job.task.task.JobTask;
import com.flash.framework.job.task.task.JobTaskContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

/**
 * @author zhurg
 * @date 2019/5/8 - 下午2:04
 */
@Slf4j
@ConditionalOnProperty(prefix = "tasks", name = "compensate", havingValue = "true")
@Job(name = "compensateJob", desc = "统一补偿任务", cron = "${tasks.compensate.cron:0 0/1 * * * ?}")
public class CompensateJob extends JobTask {

    @Autowired
    private CompensateService compensateService;

    @Override
    public void execute(JobTaskContext jobTaskContext) {
        log.info("[Job Task] CompensateJob start");
        compensateService.handler();
        log.info("[Job Task] CompensateJob end");
    }
}