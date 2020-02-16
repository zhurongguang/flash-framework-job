package com.flash.framework.job.task.initializer;

import com.flash.framework.job.task.autoconfigure.JobTaskConfigure;
import com.flash.framework.job.task.task.Job;
import org.apache.commons.collections.MapUtils;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.Assert;

import java.util.Map;

/**
 * @author zhurg
 * @date 2019/8/19 - 上午11:07
 */
public class JobTaskInitializer {

    private final JobTaskConfigure jobTaskConfigure;

    private final JobTaskLoader jobTaskLoader;

    public JobTaskInitializer(JobTaskConfigure jobTaskConfigure, JobTaskLoader jobTaskLoader) {
        this.jobTaskConfigure = jobTaskConfigure;
        this.jobTaskLoader = jobTaskLoader;
    }

    @EventListener
    public void init(ContextRefreshedEvent contextRefreshedEvent) {
        ApplicationContext applicationContext = contextRefreshedEvent.getApplicationContext();
        DefaultListableBeanFactory factory = (DefaultListableBeanFactory) applicationContext.getAutowireCapableBeanFactory();

        Map<String, Object> jobTaskMap = applicationContext.getBeansWithAnnotation(Job.class);

        if (MapUtils.isNotEmpty(jobTaskMap)) {

            //校验
            validateJobParam(jobTaskMap);

            //初始化JobTask
            jobTaskLoader.loadJobs(applicationContext, factory, jobTaskMap);
        }
    }

    private void validateJobParam(Map<String, Object> jobTaskMap) {
        jobTaskMap.forEach((beanName, jobBean) -> {
            Job job = AnnotationUtils.findAnnotation(jobBean.getClass(), Job.class);
            Assert.notNull(job, String.format("[Job Task] Job %s must have annotation @Job", jobBean.getClass().getCanonicalName()));
            Assert.notNull(job.name(), String.format("[Job Task] Job %s ,@Job param name can not be null", jobBean.getClass().getCanonicalName()));
            Assert.notNull(job.cron(), String.format("[Job Task] Job %s ,@Job param cron can not be null", jobBean.getClass().getCanonicalName()));
        });
    }
}