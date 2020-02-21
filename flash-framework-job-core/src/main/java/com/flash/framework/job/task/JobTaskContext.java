package com.flash.framework.job.task;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

/**
 * @author zhurg
 * @date 2019/3/15 - 下午3:43
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class JobTaskContext implements Serializable {

    private static final long serialVersionUID = -591513124190946677L;

    /**
     * 作业名称.
     */
    private String jobName;

    /**
     * 作业任务ID.
     */
    private String taskId;

    /**
     * 分片总数.
     */
    private int shardingTotalCount;

    /**
     * 作业参数
     */
    private Map<String, Object> jobParameter;

    /**
     * 分配于本作业实例的分片项.
     */
    private int shardingItem;

    /**
     * 分配于本作业实例的分片参数.
     */
    private String shardingParameter;

    /**
     * 任务执行时间
     */
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date jobExecuteTime = new Date();

    /**
     * 是否是补偿任务
     */
    private boolean compensate;
}