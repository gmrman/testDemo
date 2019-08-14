package com.digiwin.emr.emr.service;

import com.digiwin.app.service.DWService;

import java.util.Map;

public interface IRepairPlanService extends DWService {

    /**
     * 查询维修计划
     * @author Jihh
     */
    //	@AllowAnonymous
    public Object getList(Map<String, Object> info) throws Exception;

}
