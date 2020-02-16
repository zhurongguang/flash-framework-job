package com.flash.framework.job.task.initializer.quartz;

import com.alibaba.fastjson.JSON;
import com.flash.framework.job.task.exception.JobTaskException;
import com.flash.framework.job.task.initializer.JobTaskLoader;
import com.flash.framework.job.task.quartz.QuartzJobManager;
import com.flash.framework.job.task.task.Job;
import com.flash.framework.job.task.task.JobTask;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.env.Environment;

import java.util.Map;

/**
 * @author zhurg
 * @date 2019/8/19 - 上午11:27
 */
@Slf4j
public class QuartzJobTaskLoader implements JobTaskLoader {

    @Autowired
    private QuartzJobManager quartzJobManager;

    @Override
    public void loadJobs(ApplicationContext applicationContext, DefaultListableBeanFactory beanFactory, Map<String, Object> jobTasks) {
        Environment environment = applicationContext.getEnvironment();
        jobTasks.forEach((beanName, jobBean) -> {
            if (!org.quartz.Job.class.isAssignableFrom(jobBean.getClass())) {
                throw new JobTaskException("[Job Task] Job " + jobBean.getClass().getCanonicalName() + " must assignable from JobTask!");
            }
            JobTask jobTask = (JobTask) jobBean;
            Job jobAnnotation = AnnotationUtils.findAnnotation(jobBean.getClass(), Job.class);
            try {
                String jobName = StringUtils.isNotBlank(jobAnnotation.name()) ? environment.resolvePlaceholders(jobAnnotation.name()) : beanName;
                String cron = environment.resolvePlaceholders(jobAnnotation.cron());
                if (!quartzJobManager.isJobAdded(jobName, QuartzJobManager.JOB_GROUP_NAME)) {
                    String jobParameter = jobAnnotation.jobParameter();
                    Map<String, Object> params = StringUtils.isNotBlank(jobParameter) ? JSON.parseObject(environment.resolvePlaceholders(jobParameter)) : null;
                    quartzJobManager.addJob(jobName, QuartzJobManager.JOB_GROUP_NAME, String.format("%sTrigger", jobName), QuartzJobManager.TRIGGER_GROUP_NAME,
                            jobTask.getClass(), cron, params);
                    log.info("[Job Task] Job {} init success", jobBean.getClass().getCanonicalName());
                } else {
                    log.warn("[Job Task] Job {} areadly init", jobBean.getClass().getCanonicalName());
                }
            } catch (SchedulerException e) {
                log.error("[Job Task] Job {} init fail ", jobBean.getClass().getCanonicalName(), e);
            }
        });
    }
}