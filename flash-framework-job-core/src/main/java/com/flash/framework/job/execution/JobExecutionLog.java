package com.flash.framework.job.execution;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

/**
 * @author zhurg
 * @date 2018/11/4 - 下午8:45
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class JobExecutionLog implements Serializable {


    private static final long serialVersionUID = -3577708851685761976L;

    /**
     * ID
     */
    private Long id;

    /**
     * 任务Bean
     */
    private String jobClass;

    /**
     * 任务名称
     */
    private String jobName;

    /**
     * 开始时间
     */
    private Date startTime;

    /**
     * 结束时间
     */
    private Date endTime;

    /**
     * 异常信息
     */
    private String errMsg;

    /**
     * 任务状态 1:执行成功 2:执行失败
     */
    private Integer status;

    /**
     * 消耗时间
     */
    private Long consumeTime;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 任务参数
     */
    private Map<String, Object> jobParam;

    /**
     * 当前实例IP
     */
    private String ip;
}
