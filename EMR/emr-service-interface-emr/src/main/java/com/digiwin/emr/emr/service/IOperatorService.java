package com.digiwin.emr.emr.service;

import com.digiwin.app.service.DWService;

import java.util.Map;

public interface IOperatorService extends DWService {
    /**
     * 取得作业员报修列表
     * @author lidonga
     * @param info 查詢信息
     * @return 結果
     */
//    @AllowAnonymous
    public Object getRepairList(Map<String, Object> info) throws Exception;

    /**
     * 作业员提交报修内容
     * @author lidonga
     * @param info 查詢信息
     * @return 結果
     */
//    @AllowAnonymous
    public Object postRepairList(Map<String, Object> info) throws Exception;
}
