package com.flash.framework.job.factory.elasticjob;

import com.dangdang.ddframe.job.api.ElasticJob;
import com.dangdang.ddframe.job.lite.api.listener.ElasticJobListener;
import com.flash.framework.job.elasticjob.ElasticJobDetailProperties;
import com.flash.framework.job.factory.ScheduleContext;
import lombok.Data;

import java.util.Map;

/**
 * @author zhurg
 * @date 2020/2/20 - 11:46 AM
 */
@Data
public class ElasticScheduleContext extends ScheduleContext<ElasticJob> {

    private String desc;

    private String shardingItemParameters;

    private String jobParameter;

    private int shardingTotalCount;

    private ElasticJobDetailProperties properties;

    private String beanName;

    private ElasticJobListener[] listeners;


    private ElasticScheduleContext(String jobName, ElasticJob jobTask, String cron, Map<String, Object> jobParams) {
        super(jobName, jobTask, cron, jobParams);
    }

    public static ElasticScheduleContextBuilder builder(String jobName, ElasticJob jobTask, String cron, Map<String, Object> jobParams) {
        return new ElasticScheduleContextBuilder(new ElasticScheduleContext(jobName, jobTask, cron, jobParams));
    }

    public static class ElasticScheduleContextBuilder {

        private final ElasticScheduleContext context;

        private ElasticScheduleContextBuilder(ElasticScheduleContext context) {
            this.context = context;
        }

        public ElasticScheduleContextBuilder desc(String desc) {
            this.context.setDesc(desc);
            return this;
        }

        public ElasticScheduleContextBuilder shardingItemParameters(String shardingItemParameters) {
            this.context.setShardingItemParameters(shardingItemParameters);
            return this;
        }

        public ElasticScheduleContextBuilder jobParameter(String jobParameter) {
            this.context.setJobParameter(jobParameter);
            return this;
        }

        public ElasticScheduleContextBuilder shardingTotalCount(int shardingTotalCount) {
            this.context.setShardingTotalCount(shardingTotalCount);
            return this;
        }

        public ElasticScheduleContextBuilder properties(ElasticJobDetailProperties properties) {
            this.context.setProperties(properties);
            return this;
        }

        public ElasticScheduleContextBuilder beanBean(String beanName) {
            this.context.setBeanName(beanName);
            return this;
        }

        public ElasticScheduleContextBuilder listeners(ElasticJobListener[] listeners) {
            this.context.setListeners(listeners);
            return this;
        }

        public ElasticScheduleContext build() {
            return this.context;
        }
    }
}