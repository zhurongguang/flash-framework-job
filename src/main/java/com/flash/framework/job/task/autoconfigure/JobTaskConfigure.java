package com.flash.framework.job.task.autoconfigure;

import com.flash.framework.job.task.elasticjob.ElasticJobProperties;
import com.flash.framework.job.task.task.JobType;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * @author zhurg
 * @date 2018/10/28 - 下午9:16
 */
@Data
@ConfigurationProperties(prefix = "tasks")
public class JobTaskConfigure {

    /**
     * 任务类型
     */
    private JobType type;

    /**
     * ElasticJob集群配置
     */
    @NestedConfigurationProperty
    private ElasticJobProperties elasticJob;

    /**
     * 是否启用任务补偿
     */
    private boolean compensate;

    /**
     * 是否启用异常邮件通知
     */
    private boolean errorMessageNotice;

    /**
     * 异常邮件接受组，多个收件人用逗号分隔
     */
    private String mailGroup;
}
