package com.flash.framework.job;

import com.dangdang.ddframe.job.lite.api.listener.ElasticJobListener;
import com.flash.framework.job.elasticjob.ElasticJobDetailProperties;
import com.flash.framework.job.elasticjob.ElasticJobPropertiesHelper;
import com.flash.framework.job.execution.JobExecutionService;
import com.flash.framework.job.execution.jdbc.JdbcJobExecutionService;
import com.flash.framework.job.job.ErrorJobTaskImpl;
import com.flash.framework.job.job.NormalJobTaskImpl;
import com.flash.framework.job.listener.LogElasticJobListener;
import com.google.common.collect.Lists;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.List;
import java.util.Map;

/**
 * @author zhurg
 * @date 2020/2/21 - 3:03 PM
 */
@Configuration
public class JobTaskTestConfiguration {

    /**
     * 记录JobTask的执行日志，写入到数据库
     *
     * @return
     */
    @Bean
    @Primary
    public JobExecutionService jobExecutionService() {
        return new JdbcJobExecutionService();
    }

    /**
     * ElasticJob 配置辅助工具，可以扩展配置每个job
     *
     * @return
     */
    @Bean
    public ElasticJobPropertiesHelper elasticJobPropertiesHelper() {
        return new ElasticJobPropertiesHelper() {
            /**
             * 单个任务配置
             * @param jobDetailProperties 配置
             */
            @Override
            public void addJobProperties(Map<Class<?>, ElasticJobDetailProperties> jobDetailProperties) {
                jobDetailProperties.put(ErrorJobTaskImpl.class, new ElasticJobDetailProperties());
            }

            /**
             * 全局配置
             * @return
             */
            @Override
            public ElasticJobDetailProperties setGlobalProperties() {
                return new ElasticJobDetailProperties();
            }

            /**
             * 单个任务的Listener配置
             * @param jobListeners
             */
            @Override
            public void addJobListeners(Map<Class<?>, List<ElasticJobListener>> jobListeners) {
                jobListeners.put(NormalJobTaskImpl.class, Lists.newArrayList(new LogElasticJobListener()));
            }
        };
    }
}