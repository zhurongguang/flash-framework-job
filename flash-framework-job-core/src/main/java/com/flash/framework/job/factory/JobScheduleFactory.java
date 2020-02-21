package com.flash.framework.job.factory;

/**
 * @author zhurg
 * @date 2020/2/20 - 10:04 AM
 */
public interface JobScheduleFactory<S, C> {

    /**
     * 创建任务
     *
     * @param context
     * @return
     */
    S createScheduler(C context) throws Exception;

    /**
     * 创建并启动任务
     *
     * @param context
     * @p
     */
    void addScheduler(C context) throws Exception;

    /**
     * 修改任务
     *
     * @param context
     */
    void modifyScheduler(C context) throws Exception;

    /**
     * 移除任务
     *
     * @param context
     */
    void removeScheduler(C context) throws Exception;
}