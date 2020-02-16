package com.flash.framework.job.task.autoconfigure;

import com.dangdang.ddframe.job.event.JobEventConfiguration;
import com.dangdang.ddframe.job.event.rdb.JobEventRdbConfiguration;
import com.dangdang.ddframe.job.reg.zookeeper.ZookeeperConfiguration;
import com.dangdang.ddframe.job.reg.zookeeper.ZookeeperRegistryCenter;
import com.flash.framework.job.task.execution.DefaultJobExecutionService;
import com.flash.framework.job.task.execution.JobExecutionAspect;
import com.flash.framework.job.task.execution.JobExecutionHandler;
import com.flash.framework.job.task.execution.JobExecutionService;
import com.flash.framework.job.task.execution.compensate.CompensateService;
import com.flash.framework.job.task.execution.compensate.jdbc.JdbcCompensateHandler;
import com.flash.framework.job.task.execution.notice.MailMessageNotice;
import com.flash.framework.job.task.execution.notice.MessageNotice;
import com.flash.framework.job.task.initializer.JobTaskInitializer;
import com.flash.framework.job.task.initializer.JobTaskLoader;
import com.flash.framework.job.task.initializer.elasticjob.ElasticJobTaskLoader;
import com.flash.framework.job.task.initializer.quartz.QuartzJobTaskLoader;
import com.flash.framework.job.task.quartz.QuartzJobManager;
import org.springframework.beans.BeanUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.quartz.QuartzProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.util.Assert;

import javax.sql.DataSource;

/**
 * @author zhurg
 * @date 2019/8/19 - 下午3:39
 */
@Configuration
@EnableScheduling
@EnableAspectJAutoProxy(proxyTargetClass = true)
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
        public QuartzJobManager quartzManager() {
            return new QuartzJobManager();
        }
    }

    @Configuration
    @ConditionalOnProperty(prefix = "tasks", name = "type", havingValue = "elastic_job")
    public static class ElasticJobAutoConfiguration {

        @Bean
        public JobTaskLoader jobTaskLoader() {
            return new ElasticJobTaskLoader();
        }

        @Bean(initMethod = "init")
        public ZookeeperRegistryCenter zookeeperRegistryCenter(JobTaskConfigure jobTaskConfigure) {
            Assert.notNull(jobTaskConfigure.getElasticJob(), "[Job Task] cluster parameter elastic-job can not be null");
            Assert.notNull(jobTaskConfigure.getElasticJob().getServerLists(), "[Job Task] cluster parameter elastic-job.web-lists can not be null");

            ZookeeperConfiguration zookeeperConfiguration = new ZookeeperConfiguration(jobTaskConfigure.getElasticJob().getServerLists(), jobTaskConfigure.getElasticJob().getNamespace());
            BeanUtils.copyProperties(jobTaskConfigure.getElasticJob(), zookeeperConfiguration);
            return new ZookeeperRegistryCenter(zookeeperConfiguration);
        }

        @Bean
        public JobEventConfiguration jobEventConfiguration(DataSource dataSource) {
            return new JobEventRdbConfiguration(dataSource);
        }
    }

    @Bean
    public JobTaskInitializer jobTaskInitializer(JobTaskConfigure jobTaskConfigure, JobTaskLoader jobTaskLoader) {
        return new JobTaskInitializer(jobTaskConfigure, jobTaskLoader);
    }

    @Configuration
    @ConditionalOnProperty(prefix = "tasks", name = "compensate", havingValue = "true")
    public static class CompensateAutoConfiguration {

        @Bean
        public JdbcCompensateHandler compensateHandler() {
            return new JdbcCompensateHandler();
        }

        @Bean
        public CompensateService compensateService() {
            return new CompensateService();
        }
    }
}