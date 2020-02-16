package com.flash.framework.job.task.initializer.elasticjob;

import com.dangdang.ddframe.job.api.dataflow.DataflowJob;
import com.dangdang.ddframe.job.api.script.ScriptJob;
import com.dangdang.ddframe.job.api.simple.SimpleJob;
import com.dangdang.ddframe.job.config.JobCoreConfiguration;
import com.dangdang.ddframe.job.config.dataflow.DataflowJobConfiguration;
import com.dangdang.ddframe.job.config.simple.SimpleJobConfiguration;
import com.dangdang.ddframe.job.event.rdb.JobEventRdbConfiguration;
import com.dangdang.ddframe.job.executor.handler.JobProperties;
import com.dangdang.ddframe.job.lite.api.listener.ElasticJobListener;
import com.dangdang.ddframe.job.lite.config.LiteJobConfiguration;
import com.dangdang.ddframe.job.lite.spring.api.SpringJobScheduler;
import com.dangdang.ddframe.job.reg.zookeeper.ZookeeperRegistryCenter;
import com.flash.framework.job.task.elasticjob.ElasticJobDetailProperties;
import com.flash.framework.job.task.elasticjob.ElasticJobPropertiesHelper;
import com.flash.framework.job.task.exception.JobTaskException;
import com.flash.framework.job.task.initializer.JobTaskLoader;
import com.flash.framework.job.task.task.Job;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.env.Environment;

import java.util.Map;

/**
 * ElasticJob 加载器
 *
 * @author zhurg
 * @date 2019/8/19 - 下午2:36
 */
@Slf4j
public class ElasticJobTaskLoader implements JobTaskLoader, ApplicationContextAware {

    private Map<Class<?>, ElasticJobDetailProperties> jobDetailProperties = Maps.newConcurrentMap();

    private ElasticJobDetailProperties globalProperties = new ElasticJobDetailProperties();

    @Override
    public void loadJobs(ApplicationContext applicationContext, DefaultListableBeanFactory beanFactory, Map<String, Object> jobTasks) {
        Environment environment = applicationContext.getEnvironment();
        jobTasks.forEach((beanName, jobBean) -> {
            if (!SimpleJob.class.isAssignableFrom(jobBean.getClass()) && !DataflowJob.class.isAssignableFrom(jobBean.getClass())) {
                throw new JobTaskException("[Job Task] Job " + jobBean.getClass().getCanonicalName() + " must assignable from JobTask or DataflowJob!");
            }
            Job jobAnnotation = AnnotationUtils.findAnnotation(jobBean.getClass(), Job.class);
            String jobName = environment.resolvePlaceholders(jobAnnotation.name());
            String desc = StringUtils.isNotBlank(jobAnnotation.desc()) ? environment.resolvePlaceholders(jobAnnotation.desc()) : null;
            String shardingItemParameters = StringUtils.isNotBlank(jobAnnotation.shardingItemParameters()) ? environment.resolvePlaceholders(jobAnnotation.shardingItemParameters()) : null;
            String cron = environment.resolvePlaceholders(jobAnnotation.cron());
            String jobParameter = StringUtils.isNotBlank(jobAnnotation.jobParameter()) ? environment.resolvePlaceholders(jobAnnotation.jobParameter()) : null;
            String shardingTotalCount = StringUtils.isNumeric(jobAnnotation.shardingTotalCount()) ? jobAnnotation.shardingTotalCount() : environment.resolvePlaceholders(jobAnnotation.shardingTotalCount());
            JobCoreConfiguration jobCoreConfiguration = JobCoreConfiguration.newBuilder(jobName, cron, StringUtils.isNumeric(shardingTotalCount) ? Integer.valueOf(shardingTotalCount) : 1)
                    .description(desc)
                    .shardingItemParameters(shardingItemParameters)
                    .jobParameter(jobParameter)
                    .jobProperties(JobProperties.JobPropertiesEnum.JOB_EXCEPTION_HANDLER.getKey(), JobProperties.JobPropertiesEnum.JOB_EXCEPTION_HANDLER.getDefaultValue())
                    .jobProperties(JobProperties.JobPropertiesEnum.EXECUTOR_SERVICE_HANDLER.getKey(), JobProperties.JobPropertiesEnum.EXECUTOR_SERVICE_HANDLER.getDefaultValue())
                    .build();

            LiteJobConfiguration liteJobConfiguration = null;

            ElasticJobDetailProperties properties = getJobProperties(jobBean.getClass());

            if (SimpleJob.class.isAssignableFrom(jobBean.getClass())) {
                liteJobConfiguration = LiteJobConfiguration.newBuilder(new SimpleJobConfiguration(jobCoreConfiguration, jobBean.getClass().getName()))
                        .monitorPort(properties.getMonitorPort())
                        .monitorExecution(properties.isMonitorExecution())
                        .maxTimeDiffSeconds(properties.getMaxTimeDiffSeconds())
                        .jobShardingStrategyClass(properties.getJobShardingStrategyClass())
                        .reconcileIntervalMinutes(properties.getReconcileIntervalMinutes())
                        .disabled(properties.isDisabled())
                        .overwrite(properties.isOverwrite())
                        .build();
            } else if (DataflowJob.class.isAssignableFrom(jobBean.getClass())) {
                liteJobConfiguration = LiteJobConfiguration.newBuilder(new DataflowJobConfiguration(jobCoreConfiguration, jobBean.getClass().getName(), true))
                        .monitorPort(properties.getMonitorPort())
                        .monitorExecution(properties.isMonitorExecution())
                        .maxTimeDiffSeconds(properties.getMaxTimeDiffSeconds())
                        .jobShardingStrategyClass(properties.getJobShardingStrategyClass())
                        .reconcileIntervalMinutes(properties.getReconcileIntervalMinutes())
                        .disabled(properties.isDisabled())
                        .overwrite(properties.isOverwrite())
                        .build();
            } else if (ScriptJob.class.isAssignableFrom(jobBean.getClass())) {
                log.warn("[Job Task] unsupported job type ScriptJob");
                return;
            }

            BeanDefinitionBuilder springJobSchedulerBeanDefinitionBuilder = BeanDefinitionBuilder.rootBeanDefinition(SpringJobScheduler.class);
            springJobSchedulerBeanDefinitionBuilder.addConstructorArgValue(jobBean);
            springJobSchedulerBeanDefinitionBuilder.addConstructorArgValue(applicationContext.getBean(ZookeeperRegistryCenter.class));
            springJobSchedulerBeanDefinitionBuilder.addConstructorArgValue(liteJobConfiguration);
            springJobSchedulerBeanDefinitionBuilder.addConstructorArgValue(applicationContext.getBean(JobEventRdbConfiguration.class));

            Map<String, ElasticJobListener> listenerMap = applicationContext.getBeansOfType(ElasticJobListener.class);
            if (MapUtils.isNotEmpty(listenerMap)) {
                springJobSchedulerBeanDefinitionBuilder.addConstructorArgValue(listenerMap.values().toArray(new ElasticJobListener[]{}));
            } else {
                springJobSchedulerBeanDefinitionBuilder.addConstructorArgValue(new ElasticJobListener[]{});
            }
            beanFactory.registerBeanDefinition(String.format("%sJobScheduler", beanName), springJobSchedulerBeanDefinitionBuilder.getBeanDefinition());
            ((SpringJobScheduler) applicationContext.getBean(String.format("%sJobScheduler", beanName))).init();
            log.info("[Job Task] Job {} init success", jobBean.getClass().getCanonicalName());
        });
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        try {
            ElasticJobPropertiesHelper elasticJobPropertiesHelper = applicationContext.getBean(ElasticJobPropertiesHelper.class);
            elasticJobPropertiesHelper.addJobProperties(jobDetailProperties);
            ElasticJobDetailProperties properties = elasticJobPropertiesHelper.setGlobalProperties();
            if (null != properties) {
                BeanUtils.copyProperties(properties, globalProperties);
            }
        } catch (Exception e) {
        }
    }

    private ElasticJobDetailProperties getJobProperties(Class<?> job) {
        if (MapUtils.isEmpty(jobDetailProperties)) {
            return globalProperties;
        }
        return jobDetailProperties.getOrDefault(job, globalProperties);
    }
}