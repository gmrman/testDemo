package com.digiwin.emr.emr.service;

import com.digiwin.app.service.DWService;

import java.util.Map;

public interface ICallRepairService extends DWService {
    /**
     * 查询外修计划
     * @author Jihh
     */
    //	@AllowAnonymous
    public Object getList(Map<String, Object> info) throws Exception;

    /**
     * 外修结案保存
     * @author Jihh
     */
    //	@AllowAnonymous
    public Object putList(Map<String, Object> info) throws Exception;

    /**
     * 外修任务新建
     * @author Jihh
     */
    //	@AllowAnonymous
    public Object postList(Map<String, Object> info) throws Exception;
}
