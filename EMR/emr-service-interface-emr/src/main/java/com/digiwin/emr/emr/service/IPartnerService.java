package com.digiwin.emr.emr.service;

import com.digiwin.app.service.DWService;

import java.util.Map;

public interface IPartnerService extends DWService {
    /**
     * 获取伙伴关系服务
     * @author gengmr
     * @param info get
     * @return 結果
     */
    public Object getList(Map<String, Object> info) throws Exception;
}
