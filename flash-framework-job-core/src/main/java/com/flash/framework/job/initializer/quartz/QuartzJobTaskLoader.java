package com.flash.framework.job.initializer.quartz;

import com.alibaba.fastjson.JSON;
import com.flash.framework.job.autoconfigure.JobTaskConfigure;
import com.flash.framework.job.exception.JobTaskException;
import com.flash.framework.job.execution.compensate.CompensateJobHelper;
import com.flash.framework.job.execution.compensate.job.CompensateJobTask;
import com.flash.framework.job.factory.JobScheduleFactory;
import com.flash.framework.job.factory.quartz.QuartzScheduleContext;
import com.flash.framework.job.initializer.JobTaskLoader;
import com.flash.framework.job.quartz.QuartzProperties;
import com.flash.framework.job.task.Job;
import com.flash.framework.job.task.JobTask;
import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.quartz.Scheduler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.env.Environment;

import java.util.Map;
import java.util.Objects;

/**
 * @author zhurg
 * @date 2019/8/19 - 上午11:27
 */
@Slf4j
public class QuartzJobTaskLoader implements JobTaskLoader {

    @Autowired
    private JobScheduleFactory<Scheduler, QuartzScheduleContext> jobScheduleFactory;

    @Autowired
    private JobTaskConfigure jobTaskConfigure;

    @Autowired(required = false)
    private CompensateJobHelper compensateJobHelper;

    @Override
    public void loadJobs(ApplicationContext applicationContext, DefaultListableBeanFactory beanFactory, Map<String, Object> jobTasks) {
        Environment environment = applicationContext.getEnvironment();
        if (Objects.isNull(jobTaskConfigure.getQuartz())) {
            jobTaskConfigure.setQuartz(new QuartzProperties());
        }
        jobTasks.forEach((beanName, jobBean) -> {
            if (!JobTask.class.isAssignableFrom(jobBean.getClass())) {
                throw new JobTaskException("[Job Task] Job " + jobBean.getClass().getCanonicalName() + " must assignable from JobTask!");
            }
            JobTask jobTask = (JobTask) jobBean;
            Job jobAnnotation = AnnotationUtils.findAnnotation(jobBean.getClass(), Job.class);
            try {
                String jobName = StringUtils.isNotBlank(jobAnnotation.name()) ? environment.resolvePlaceholders(jobAnnotation.name()) : beanName;
                String cron = environment.resolvePlaceholders(jobAnnotation.cron());
                String jobParameter = jobAnnotation.jobParameter();
                Map<String, Object> params = StringUtils.isNotBlank(jobParameter) ? JSON.parseObject(environment.resolvePlaceholders(jobParameter)) : null;
                String jobGroupName = StringUtils.isBlank(jobAnnotation.jobGroupName()) ? jobTaskConfigure.getQuartz().getJobGroupName() : jobAnnotation.jobGroupName();
                String traggerGroupName = StringUtils.isBlank(jobAnnotation.triggerGroupName()) ? jobTaskConfigure.getQuartz().getTriggerGroupName() : jobAnnotation.triggerGroupName();
                jobScheduleFactory.addScheduler(QuartzScheduleContext.builder(jobName, jobTask, cron, params)
                        .jobGroupName(jobGroupName)
                        .triggerGroupName(traggerGroupName)
                        .desc(jobAnnotation.desc())
                        .build());
                //创建对应的补偿任务
                if (jobAnnotation.compensate() && jobTaskConfigure.isCompensate()) {
                    String compensateCron = jobAnnotation.compensateCron();
                    if (StringUtils.isBlank(compensateCron)) {
                        throw new JobTaskException("[Job Task] Job " + jobName + " need compensateCron properties");
                    }
                    CompensateJobTask compensateJobTask = compensateJobHelper.createCompensateJob(jobName, beanFactory);
                    jobScheduleFactory.addScheduler(QuartzScheduleContext.builder(compensateJobHelper.buildCompensateJobName(jobName), compensateJobTask, environment.resolvePlaceholders(compensateCron), ImmutableMap.of("jobName", jobName))
                            .jobGroupName(jobGroupName)
                            .triggerGroupName(traggerGroupName)
                            .desc(jobAnnotation.desc())
                            .build());
                }
                log.info("[Job Task] Job {} init success", jobBean.getClass().getCanonicalName());
            } catch (Exception e) {
                log.error("[Job Task] Job {} init fail ", jobBean.getClass().getCanonicalName(), e);
            }
        });
    }
}