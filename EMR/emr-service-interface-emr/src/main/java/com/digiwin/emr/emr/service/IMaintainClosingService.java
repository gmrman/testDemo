package com.digiwin.emr.emr.service;

import com.digiwin.app.service.DWService;

import java.util.Map;

public interface IMaintainClosingService extends DWService {

    /**
     * 維修結案信息回报
     * @author jiangzheng
     * @param info update
     * @return 結果
     */
//    @AllowAnonymous
    public Object post(Map<String, Object> info) throws Exception;
}
