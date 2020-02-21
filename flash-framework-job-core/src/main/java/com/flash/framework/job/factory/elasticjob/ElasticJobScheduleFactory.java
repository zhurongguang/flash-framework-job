package com.flash.framework.job.factory.elasticjob;

import com.dangdang.ddframe.job.api.dataflow.DataflowJob;
import com.dangdang.ddframe.job.api.simple.SimpleJob;
import com.dangdang.ddframe.job.config.JobCoreConfiguration;
import com.dangdang.ddframe.job.config.dataflow.DataflowJobConfiguration;
import com.dangdang.ddframe.job.config.simple.SimpleJobConfiguration;
import com.dangdang.ddframe.job.event.rdb.JobEventRdbConfiguration;
import com.dangdang.ddframe.job.executor.handler.JobProperties;
import com.dangdang.ddframe.job.lite.api.JobScheduler;
import com.dangdang.ddframe.job.lite.config.LiteJobConfiguration;
import com.dangdang.ddframe.job.lite.spring.api.SpringJobScheduler;
import com.dangdang.ddframe.job.reg.zookeeper.ZookeeperRegistryCenter;
import com.flash.framework.job.elasticjob.ElasticJobDetailProperties;
import com.flash.framework.job.factory.JobScheduleFactory;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.Objects;

/**
 * @author zhurg
 * @date 2020/2/20 - 10:49 AM
 */
@Slf4j
public class ElasticJobScheduleFactory implements JobScheduleFactory<JobScheduler, ElasticScheduleContext>, ApplicationContextAware {

    public static final String JOB_EVENT_RDB_CONFIGURATION_BEAN_NAME = "JobEventRdbConfiguration";

    private ApplicationContext applicationContext;

    @Setter
    private JobEventRdbConfiguration jobEventRdbConfiguration;

    private final ZookeeperRegistryCenter zookeeperRegistryCenter;

    public ElasticJobScheduleFactory(ZookeeperRegistryCenter zookeeperRegistryCenter) {
        this.zookeeperRegistryCenter = zookeeperRegistryCenter;
    }


    @Override
    public JobScheduler createScheduler(ElasticScheduleContext context) throws Exception {
        JobScheduler jobScheduler;
        if (Objects.isNull(jobEventRdbConfiguration)) {
            jobScheduler = new SpringJobScheduler(context.getJobTask(), zookeeperRegistryCenter, buildLiteJobConfiguration(context), context.getListeners());
        } else {
            jobScheduler = new SpringJobScheduler(context.getJobTask(), zookeeperRegistryCenter, buildLiteJobConfiguration(context), jobEventRdbConfiguration, context.getListeners());
        }
        return jobScheduler;
    }

    @Override
    public void addScheduler(ElasticScheduleContext context) throws Exception {
        JobScheduler jobScheduler = createScheduler(context);
        if (Objects.nonNull(jobScheduler)) {
            jobScheduler.init();
        }
    }

    @Override
    public void modifyScheduler(ElasticScheduleContext context) throws Exception {
        SpringJobScheduler springJobScheduler = applicationContext.getBean(buildJobSchedulerName(context.getJobName()), SpringJobScheduler.class);
        springJobScheduler.getSchedulerFacade().updateJobConfiguration(buildLiteJobConfiguration(context));
    }

    @Override
    public void removeScheduler(ElasticScheduleContext context) throws Exception {
        SpringJobScheduler springJobScheduler = applicationContext.getBean(buildJobSchedulerName(context.getJobName()), SpringJobScheduler.class);
        springJobScheduler.getSchedulerFacade().shutdownInstance();
        zookeeperRegistryCenter.getRawCache(buildJobPath(context.getJobName()));
        zookeeperRegistryCenter.remove(buildJobPath(context.getJobName()));
    }

    /**
     * 构建JobScheduler名字
     *
     * @param jobName
     * @return
     */
    public String buildJobSchedulerName(String jobName) {
        return String.format("%sJobScheduler", jobName);
    }

    /**
     * 添加任务
     *
     * @param context
     * @param beanFactory
     */
    public void addScheduler(ElasticScheduleContext context, DefaultListableBeanFactory beanFactory) {
        BeanDefinitionBuilder springJobSchedulerBeanDefinitionBuilder = BeanDefinitionBuilder.rootBeanDefinition(SpringJobScheduler.class);
        springJobSchedulerBeanDefinitionBuilder.addConstructorArgValue(context.getJobTask());
        springJobSchedulerBeanDefinitionBuilder.addConstructorArgValue(zookeeperRegistryCenter);
        springJobSchedulerBeanDefinitionBuilder.addConstructorArgValue(buildLiteJobConfiguration(context));
        if (Objects.nonNull(jobEventRdbConfiguration)) {
            springJobSchedulerBeanDefinitionBuilder.addConstructorArgValue(jobEventRdbConfiguration);
        }
        //设置监听器
        springJobSchedulerBeanDefinitionBuilder.addConstructorArgValue(context.getListeners());
        beanFactory.registerBeanDefinition(buildJobSchedulerName(context.getJobName()), springJobSchedulerBeanDefinitionBuilder.getBeanDefinition());
        ((SpringJobScheduler) applicationContext.getBean(buildJobSchedulerName(context.getJobName()))).init();
    }

    /**
     * 构建 JobCoreConfiguration
     *
     * @param context
     * @return
     */
    private JobCoreConfiguration buildJobCoreConfiguration(ElasticScheduleContext context) {
        return JobCoreConfiguration.newBuilder(context.getJobName(), context.getCron(), context.getShardingTotalCount())
                .description(context.getDesc())
                .shardingItemParameters(context.getShardingItemParameters())
                .jobParameter(context.getJobParameter())
                .jobProperties(JobProperties.JobPropertiesEnum.JOB_EXCEPTION_HANDLER.getKey(), JobProperties.JobPropertiesEnum.JOB_EXCEPTION_HANDLER.getDefaultValue())
                .jobProperties(JobProperties.JobPropertiesEnum.EXECUTOR_SERVICE_HANDLER.getKey(), JobProperties.JobPropertiesEnum.EXECUTOR_SERVICE_HANDLER.getDefaultValue())
                .build();
    }

    /**
     * 构建 LiteJobConfiguration
     *
     * @param context
     * @return
     */
    private LiteJobConfiguration buildLiteJobConfiguration(ElasticScheduleContext context) {
        ElasticJobDetailProperties properties = context.getProperties();
        if (SimpleJob.class.isAssignableFrom(context.getJobTask().getClass())) {
            return LiteJobConfiguration.newBuilder(new SimpleJobConfiguration(buildJobCoreConfiguration(context), context.getJobTask().getClass().getName()))
                    .monitorPort(properties.getMonitorPort())
                    .monitorExecution(properties.isMonitorExecution())
                    .maxTimeDiffSeconds(properties.getMaxTimeDiffSeconds())
                    .jobShardingStrategyClass(properties.getJobShardingStrategyClass())
                    .reconcileIntervalMinutes(properties.getReconcileIntervalMinutes())
                    .disabled(properties.isDisabled())
                    .overwrite(properties.isOverwrite())
                    .build();
        } else if (DataflowJob.class.isAssignableFrom(context.getJobTask().getClass())) {
            return LiteJobConfiguration.newBuilder(new DataflowJobConfiguration(buildJobCoreConfiguration(context), context.getJobTask().getClass().getName(), true))
                    .monitorPort(properties.getMonitorPort())
                    .monitorExecution(properties.isMonitorExecution())
                    .maxTimeDiffSeconds(properties.getMaxTimeDiffSeconds())
                    .jobShardingStrategyClass(properties.getJobShardingStrategyClass())
                    .reconcileIntervalMinutes(properties.getReconcileIntervalMinutes())
                    .disabled(properties.isDisabled())
                    .overwrite(properties.isOverwrite())
                    .build();
        } else {
            log.warn("[Job Task] unsupported elasticjob type");
            return null;
        }
    }

    /**
     * 任务在zk中的目录
     *
     * @param jobName
     * @return
     */
    protected String buildJobPath(String jobName) {
        return "/" + jobName;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
