package com.digiwin.emr.emr.service;

import com.digiwin.app.service.DWService;

import java.util.Map;

public interface IDispatchService extends DWService {

    /**
     * 派工保存服务
     * @author gengmr
     * @param info 查詢信息
     * @return 結果
     */
//    @AllowAnonymous
    public Object post(Map<String, Object> info) throws Exception;
}
