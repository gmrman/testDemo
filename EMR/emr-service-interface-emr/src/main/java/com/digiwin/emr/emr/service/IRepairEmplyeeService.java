package com.digiwin.emr.emr.service;

import com.digiwin.app.service.DWService;

import java.util.Map;

public interface IRepairEmplyeeService extends DWService {

    /**
     * 获取派工人员及其相关数值
     * @author lidonga
     * @param info 查詢信息
     * @return 結果
     */
//    @AllowAnonymous
    public Object getList(Map<String, Object> info) throws Exception;
}
