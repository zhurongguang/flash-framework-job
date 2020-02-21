package com.flash.framework.job.initializer.elasticjob;

import com.alibaba.fastjson.JSON;
import com.dangdang.ddframe.job.api.ElasticJob;
import com.dangdang.ddframe.job.api.dataflow.DataflowJob;
import com.dangdang.ddframe.job.event.rdb.JobEventRdbConfiguration;
import com.dangdang.ddframe.job.lite.api.listener.ElasticJobListener;
import com.flash.framework.job.autoconfigure.JobTaskConfigure;
import com.flash.framework.job.elasticjob.ElasticJobDetailProperties;
import com.flash.framework.job.elasticjob.ElasticJobPropertiesHelper;
import com.flash.framework.job.exception.JobTaskException;
import com.flash.framework.job.execution.compensate.CompensateHandler;
import com.flash.framework.job.execution.compensate.CompensateJobHelper;
import com.flash.framework.job.execution.compensate.job.CompensateJobTask;
import com.flash.framework.job.factory.elasticjob.ElasticJobScheduleFactory;
import com.flash.framework.job.factory.elasticjob.ElasticScheduleContext;
import com.flash.framework.job.initializer.JobTaskLoader;
import com.flash.framework.job.task.Job;
import com.flash.framework.job.task.JobTask;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.env.Environment;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.flash.framework.job.factory.elasticjob.ElasticJobScheduleFactory.JOB_EVENT_RDB_CONFIGURATION_BEAN_NAME;

/**
 * ElasticJob 加载器
 *
 * @author zhurg
 * @date 2019/8/19 - 下午2:36
 */
@Slf4j
public class ElasticJobTaskLoader implements JobTaskLoader, ApplicationContextAware {

    @Autowired
    private ElasticJobScheduleFactory jobScheduleFactory;

    private Map<Class<?>, ElasticJobDetailProperties> jobDetailProperties = Maps.newConcurrentMap();

    private Map<Class<?>, List<ElasticJobListener>> jobListeners = Maps.newConcurrentMap();

    private ElasticJobDetailProperties globalProperties = new ElasticJobDetailProperties();

    @Autowired(required = false)
    private CompensateJobHelper compensateJobHelper;

    private JobEventRdbConfiguration jobEventRdbConfiguration;

    @Autowired
    private JobTaskConfigure jobTaskConfigure;

    @Override
    public void loadJobs(ApplicationContext applicationContext, DefaultListableBeanFactory beanFactory, Map<String, Object> jobTasks) {
        Environment environment = applicationContext.getEnvironment();
        jobTasks.forEach((beanName, jobBean) -> {
            if (!JobTask.class.isAssignableFrom(jobBean.getClass()) && !DataflowJob.class.isAssignableFrom(jobBean.getClass())) {
                throw new JobTaskException("[Job Task] Job " + jobBean.getClass().getCanonicalName() + " must assignable from JobTask or DataflowJob!");
            }

            if (jobTaskConfigure.getElasticJob().isEnableDatasource() && Objects.isNull(jobEventRdbConfiguration)) {
                DataSource dataSource = applicationContext.getBean(DataSource.class);
                BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.rootBeanDefinition(JobEventRdbConfiguration.class);
                beanDefinitionBuilder.addConstructorArgValue(dataSource);
                beanFactory.registerBeanDefinition(JOB_EVENT_RDB_CONFIGURATION_BEAN_NAME, beanDefinitionBuilder.getBeanDefinition());
                this.jobEventRdbConfiguration = applicationContext.getBean(JobEventRdbConfiguration.class);
                jobScheduleFactory.setJobEventRdbConfiguration(jobEventRdbConfiguration);
            }

            Job jobAnnotation = AnnotationUtils.findAnnotation(jobBean.getClass(), Job.class);
            String jobName = environment.resolvePlaceholders(jobAnnotation.name());
            String desc = StringUtils.isNotBlank(jobAnnotation.desc()) ? environment.resolvePlaceholders(jobAnnotation.desc()) : null;
            String shardingItemParameters = StringUtils.isNotBlank(jobAnnotation.shardingItemParameters()) ? environment.resolvePlaceholders(jobAnnotation.shardingItemParameters()) : null;
            String cron = environment.resolvePlaceholders(jobAnnotation.cron());
            String jobParameter = StringUtils.isNotBlank(jobAnnotation.jobParameter()) ? environment.resolvePlaceholders(jobAnnotation.jobParameter()) : null;
            String shardingTotalCount = StringUtils.isNumeric(jobAnnotation.shardingTotalCount()) ? jobAnnotation.shardingTotalCount() : environment.resolvePlaceholders(jobAnnotation.shardingTotalCount());
            ElasticJobDetailProperties properties = getJobProperties(jobBean.getClass());

            jobScheduleFactory.addScheduler(ElasticScheduleContext.builder(jobName, (ElasticJob) jobBean, cron, null)
                            .desc(desc)
                            .shardingItemParameters(shardingItemParameters)
                            .jobParameter(jobParameter)
                            .shardingTotalCount(NumberUtils.isDigits(shardingTotalCount) ? Integer.parseInt(shardingTotalCount) : 1)
                            .properties(properties)
                            .listeners(jobListeners.containsKey(jobBean.getClass()) ? jobListeners.get(jobBean.getClass()).toArray(new ElasticJobListener[]{}) : new ElasticJobListener[]{})
                            .build(),
                    beanFactory);

            //创建对应的补偿任务
            if (jobAnnotation.compensate() && jobTaskConfigure.isCompensate() && JobTask.class.isAssignableFrom(jobBean.getClass())) {
                String compensateCron = jobAnnotation.compensateCron();
                if (StringUtils.isBlank(compensateCron)) {
                    throw new JobTaskException("[Job Task] Job " + jobName + " need compensateCron properties");
                }
                CompensateJobTask compensateJobTask = compensateJobHelper.createCompensateJob(jobName, beanFactory);
                compensateJobTask.setCompensateHandler(applicationContext.getBean(CompensateHandler.class));
                compensateJobTask.setApplicationContext(applicationContext);
                compensateJobTask.setCompensateJobHelper(compensateJobHelper);
                compensateJobTask.setJobTaskConfigure(jobTaskConfigure);
                jobScheduleFactory.addScheduler(ElasticScheduleContext.builder(compensateJobHelper.buildCompensateJobName(jobName), compensateJobTask, environment.resolvePlaceholders(compensateCron), null)
                                .desc(desc)
                                .shardingItemParameters(shardingItemParameters)
                                .jobParameter(JSON.toJSONString(ImmutableMap.of("jobName", jobName)))
                                .shardingTotalCount(1)
                                .properties(globalProperties)
                                .listeners(new ElasticJobListener[]{})
                                .build(),
                        beanFactory);
            }
            log.info("[Job Task] Job {} init success", jobBean.getClass().getCanonicalName());
        });
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        try {
            //可以自定义任务的配置及全局配置
            ElasticJobPropertiesHelper elasticJobPropertiesHelper = applicationContext.getBean(ElasticJobPropertiesHelper.class);
            elasticJobPropertiesHelper.addJobProperties(jobDetailProperties);
            elasticJobPropertiesHelper.addJobListeners(jobListeners);
            ElasticJobDetailProperties properties = elasticJobPropertiesHelper.setGlobalProperties();
            if (Objects.nonNull(properties)) {
                BeanUtils.copyProperties(properties, globalProperties);
            }
        } catch (Exception e) {
            log.info("[Job Task] elasticjob customize properties not fund,useing default");
        }
    }

    /**
     * 获取任务配置
     *
     * @param job
     * @return
     */
    private ElasticJobDetailProperties getJobProperties(Class<?> job) {
        return jobDetailProperties.getOrDefault(job, globalProperties);
    }
}