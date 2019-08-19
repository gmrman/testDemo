package com.digiwin.emr.emr.service;

import com.digiwin.app.service.DWService;

import java.util.Map;

public interface ITaskService extends DWService {
    /**
     * 获取任务清单
     * @author gengmr
     * @param info 查詢信息
     * @return 結果
     */
//    @AllowAnonymous
    public Object getList(Map<String, Object> info) throws Exception;

    /**
     * 获取计划工单详情
     * @author jiangzheng
     * @param info 查詢信息
     * @return 結果
     */
//    @AllowAnonymous
    public Object getPlanDetail(Map<String, Object> info) throws Exception;


    /**
     * 故障任务通知单关闭
     * @author gengmr
     * @param info 查詢信息
     * @return 結果
     */
//    @AllowAnonymous
    public Object postTaskClose(Map<String, Object> info) throws Exception;


    /**
     * 获取故障工单详情
     * @author jiangzheng
     * @param info 查詢信息
     * @return 結果
     */
    //@AllowAnonymous
    public Object getErrorDetail(Map<String, Object> info) throws Exception;
}
