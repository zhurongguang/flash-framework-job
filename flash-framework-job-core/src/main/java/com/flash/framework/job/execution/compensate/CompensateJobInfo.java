package com.flash.framework.job.execution.compensate;

import com.flash.framework.job.task.JobTaskContext;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * @author zhurg
 * @date 2019/4/30 - 下午3:30
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompensateJobInfo implements Serializable {

    private static final long serialVersionUID = 3624915156550488645L;

    /**
     * ID
     */
    private Long id;

    /**
     * 任务类型
     */
    private String jobTask;

    /**
     * 任务bean的名称
     */
    private String jobBeanName;

    /**
     * 失败时上下文
     */
    private JobTaskContext jobTaskContext;

    /**
     * 失败次数
     */
    private Integer failedTimes;

    /**
     * 任务状态 1:执行成功 2:执行失败
     */
    private Integer status;

    /**
     * 异常信息
     */
    private String errMsg;

    /**
     * 创建时间
     */
    private Date createdAt;

    /**
     * 更新时间
     */
    private Date updatedAt;
}