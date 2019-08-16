package com.digiwin.emr.emr.service;

import java.util.Map;

public interface IMaintainClosingService {

    /**
     * 維修結案信息回报
     * @author jiangzheng
     * @param info update
     * @return 結果
     */
//    @AllowAnonymous
    public Object post(Map<String, Object> info) throws Exception;
}
