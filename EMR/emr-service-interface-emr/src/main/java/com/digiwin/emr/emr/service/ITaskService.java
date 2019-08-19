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
     * 获取任务详情
     * @author gengmr
     * @param info 查詢信息
     * @return 結果
     */
//    @AllowAnonymous
    public Object getDetail(Map<String, Object> info) throws Exception;

    /**
     * 故障任务通知单关闭
     * @author gengmr
     * @param info 查詢信息
     * @return 結果
     */
//    @AllowAnonymous
    public Object postTaskClose(Map<String, Object> info) throws Exception;

}
