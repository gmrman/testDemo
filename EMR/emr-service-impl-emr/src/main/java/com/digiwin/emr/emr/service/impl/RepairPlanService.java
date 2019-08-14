package com.digiwin.emr.emr.service.impl;

import com.digiwin.app.dao.DWDao;
import com.digiwin.emr.emr.service.IRepairPlanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.Map;

public class RepairPlanService implements IRepairPlanService {

    @Autowired
    @Qualifier("dw-dao")
    private DWDao dao;

    //@Transactional(propagation = Propagation.REQUIRED, rollbackForClassName = "Exception")
    @Override
    public Object getList(Map<String, Object> info) throws Exception {
        return 0;
    }

}
