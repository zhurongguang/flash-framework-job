package com.flash.framework.job.factory.quartz;

import com.flash.framework.job.factory.JobScheduleFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.quartz.*;
import org.quartz.impl.triggers.CronTriggerImpl;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

import java.util.Objects;

/**
 * @author zhurg
 * @date 2020/2/20 - 10:12 AM
 */
@Slf4j
public class QuartzJobScheduleFactory implements JobScheduleFactory<Scheduler, QuartzScheduleContext> {


    private static final String TRIGGER = "Trigger";

    private final SchedulerFactoryBean schedulerFactory;

    public QuartzJobScheduleFactory(SchedulerFactoryBean schedulerFactory) {
        this.schedulerFactory = schedulerFactory;
    }

    @Override
    public Scheduler createScheduler(QuartzScheduleContext context) throws Exception {
        if (exists(context)) {
            log.warn("Job Task] Job {} areadly exists", context.getJobName());
            return null;
        }
        Scheduler scheduler = schedulerFactory.getScheduler();
        JobBuilder jobBuilder = JobBuilder.newJob(context.getJobTask().getClass())
                .withIdentity(context.getJobName(), context.getJobGroupName());
        JobDetail jobDetail = jobBuilder.build();
        if (MapUtils.isNotEmpty(context.getJobParams())) {
            context.getJobParams().entrySet().forEach(entry -> jobDetail.getJobDataMap().put(entry.getKey(), entry.getValue()));
        }
        scheduler.scheduleJob(jobDetail, TriggerBuilder.newTrigger()
                .withIdentity(String.format("%s%s", context.getJobName(), TRIGGER), context.getTriggerGroupName())
                .withSchedule(CronScheduleBuilder.cronSchedule(context.getCron()))
                .build());
        return scheduler;
    }

    @Override
    public void addScheduler(QuartzScheduleContext context) throws Exception {
        Scheduler scheduler = createScheduler(context);
        if (Objects.nonNull(scheduler) && !scheduler.isShutdown()) {
            scheduler.start();
        }
    }

    @Override
    public void modifyScheduler(QuartzScheduleContext context) throws Exception {
        Scheduler scheduler = schedulerFactory.getScheduler();
        if (StringUtils.isNotBlank(context.getCron())) {
            TriggerKey triggerKey = TriggerKey.triggerKey(String.format("%s%s", context.getJobName(), TRIGGER), context.getTriggerGroupName());
            Trigger trigger = scheduler.getTrigger(triggerKey);
            if (Objects.nonNull(trigger)) {
                CronTriggerImpl ct = (CronTriggerImpl) trigger;
                ct.setCronExpression(context.getCron());
                scheduler.resumeTrigger(triggerKey);
            }
        }
        if (MapUtils.isNotEmpty(context.getJobParams())) {
            JobKey jobKey = JobKey.jobKey(context.getJobName(), context.getJobGroupName());
            JobDetail jobDetail = scheduler.getJobDetail(jobKey);
            if (Objects.isNull(jobDetail)) {
                context.getJobParams().entrySet().forEach(entry -> jobDetail.getJobDataMap().put(entry.getKey(), entry.getValue()));
            }
        }
    }

    @Override
    public void removeScheduler(QuartzScheduleContext context) throws Exception {
        Scheduler scheduler = schedulerFactory.getScheduler();
        TriggerKey triggerKey = TriggerKey.triggerKey(String.format("%s%s", context.getJobName(), TRIGGER), context.getTriggerGroupName());
        // 停止触发器
        scheduler.pauseTrigger(triggerKey);
        // 移除触发器
        scheduler.unscheduleJob(triggerKey);
        // 删除任务
        scheduler.deleteJob(JobKey.jobKey(context.getJobName(), context.getJobGroupName()));
    }

    /**
     * 判断任务是否存在
     *
     * @param context
     * @return
     */
    private boolean exists(QuartzScheduleContext context) throws Exception {
        Scheduler scheduler = schedulerFactory.getScheduler();
        JobDetail jobDetail = scheduler.getJobDetail(JobKey.jobKey(context.getJobName(), context.getJobGroupName()));
        return Objects.nonNull(jobDetail);
    }
}
