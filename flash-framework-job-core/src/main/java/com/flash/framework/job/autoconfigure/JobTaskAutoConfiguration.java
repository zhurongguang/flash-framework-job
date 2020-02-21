package com.flash.framework.job.autoconfigure;

import com.dangdang.ddframe.job.event.JobEventConfiguration;
import com.dangdang.ddframe.job.event.rdb.JobEventRdbConfiguration;
import com.dangdang.ddframe.job.reg.zookeeper.ZookeeperConfiguration;
import com.dangdang.ddframe.job.reg.zookeeper.ZookeeperRegistryCenter;
import com.flash.framework.job.execution.DefaultJobExecutionService;
import com.flash.framework.job.execution.JobExecutionAspect;
import com.flash.framework.job.execution.JobExecutionHandler;
import com.flash.framework.job.execution.JobExecutionService;
import com.flash.framework.job.execution.compensate.jdbc.JdbcCompensateHandler;
import com.flash.framework.job.execution.notice.MailMessageNotice;
import com.flash.framework.job.execution.notice.MessageNotice;
import com.flash.framework.job.factory.elasticjob.ElasticJobScheduleFactory;
import com.flash.framework.job.factory.quartz.QuartzJobScheduleFactory;
import com.flash.framework.job.initializer.JobTaskInitializer;
import com.flash.framework.job.initializer.JobTaskLoader;
import com.flash.framework.job.initializer.elasticjob.ElasticJobTaskLoader;
import com.flash.framework.job.initializer.quartz.QuartzJobTaskLoader;
import org.springframework.beans.BeanUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.quartz.QuartzProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.util.Assert;

import javax.sql.DataSource;

import static com.flash.framework.job.factory.elasticjob.ElasticJobScheduleFactory.JOB_EVENT_RDB_CONFIGURATION_BEAN_NAME;


/**
 * @author zhurg
 * @date 2019/8/19 - 下午3:39
 */
@Configuration
@EnableScheduling
@EnableConfigurationProperties(JobTaskConfigure.class)
public class JobTaskAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public JobExecutionService jobExecutionService() {
        return new DefaultJobExecutionService();
    }

    @Bean
    @ConditionalOnMissingBean
    public JobExecutionHandler jobExecutionHandler() {
        return new JobExecutionHandler();
    }

    @Bean
    @Order(10000)
    public JobExecutionAspect jobExecutionAspect() {
        return new JobExecutionAspect();
    }

    @Bean
    @ConditionalOnProperty(prefix = "tasks", name = "error-message-notice", havingValue = "true")
    @ConditionalOnMissingBean
    public MessageNotice messageNotice() {
        return new MailMessageNotice();
    }

    @Configuration
    @ConditionalOnProperty(prefix = "tasks", name = "type", havingValue = "quartz")
    @EnableConfigurationProperties(QuartzProperties.class)
    public static class QuartzAutoConfiguration {

        @Bean
        public JobTaskLoader jobTaskLoader() {
            return new QuartzJobTaskLoader();
        }

        @Bean
        public QuartzJobScheduleFactory jobScheduleFactory(SchedulerFactoryBean schedulerFactory) {
            return new QuartzJobScheduleFactory(schedulerFactory);
        }
    }

    @Configuration
    @ConditionalOnProperty(prefix = "tasks", name = "type", havingValue = "elastic_job")
    public static class ElasticJobAutoConfiguration {

        @Bean(initMethod = "init")
        public ZookeeperRegistryCenter zookeeperRegistryCenter(JobTaskConfigure jobTaskConfigure) {
            Assert.notNull(jobTaskConfigure.getElasticJob(), "[Job Task] cluster parameter elastic-job can not be null");
            Assert.notNull(jobTaskConfigure.getElasticJob().getServerLists(), "[Job Task] cluster parameter elastic-job.web-lists can not be null");

            ZookeeperConfiguration zookeeperConfiguration = new ZookeeperConfiguration(jobTaskConfigure.getElasticJob().getServerLists(), jobTaskConfigure.getElasticJob().getNamespace());
            BeanUtils.copyProperties(jobTaskConfigure.getElasticJob(), zookeeperConfiguration);
            return new ZookeeperRegistryCenter(zookeeperConfiguration);
        }

        @Bean
        public JobTaskLoader jobTaskLoader() {
            return new ElasticJobTaskLoader();
        }


        @Bean
        public ElasticJobScheduleFactory jobScheduleFactory(ZookeeperRegistryCenter zookeeperRegistryCenter) {
            return new ElasticJobScheduleFactory(zookeeperRegistryCenter);
        }
    }

    @Bean
    public JobTaskInitializer jobTaskInitializer(JobTaskLoader jobTaskLoader) {
        return new JobTaskInitializer(jobTaskLoader);
    }

    @Configuration
    @ConditionalOnProperty(prefix = "tasks", name = "compensate", havingValue = "true")
    public static class CompensateAutoConfiguration {

        @Bean
        public JdbcCompensateHandler compensateHandler() {
            return new JdbcCompensateHandler();
        }
    }
}