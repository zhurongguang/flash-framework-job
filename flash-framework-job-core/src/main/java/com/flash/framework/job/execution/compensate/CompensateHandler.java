package com.flash.framework.job.execution.compensate;

import java.util.List;
import java.util.Map;

/**
 * 补偿任务信息接口
 *
 * @author zhurg
 * @date 2019/4/30 - 下午3:17
 */
public interface CompensateHandler {

    /**
     * 保存补偿任务
     *
     * @param compensateJobInfo
     */
    void saveCompensateJobInfo(CompensateJobInfo compensateJobInfo);

    /**
     * 更新补偿任务
     *
     * @param compensateJobInfo
     */
    void updateCompensateJobInfo(CompensateJobInfo compensateJobInfo);

    /**
     * 获取需要补偿的任务数量
     *
     * @param criteria 条件
     * @return
     */
    long countFailedCompensate(Map<String, Object> criteria);

    /**
     * 获取需要补偿任务
     *
     * @param criteria 条件
     * @return
     */
    List<CompensateJobInfo> pagingFailedCompensate(long offset, long limit, Map<String, Object> criteria);
}