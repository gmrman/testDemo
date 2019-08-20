package com.digiwin.emr.emr.service;

import com.digiwin.app.service.DWService;

import java.util.Map;

public interface IErrorCodeService extends DWService {
    /**
     * 错误码查询
     * @author Jihh
     */
    //	@AllowAnonymous
    public Object getList(Map<String, Object> info) throws Exception;
}
