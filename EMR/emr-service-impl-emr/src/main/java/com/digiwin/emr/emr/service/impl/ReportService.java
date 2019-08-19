package com.digiwin.emr.emr.service.impl;

import com.digiwin.app.container.exceptions.DWException;
import com.digiwin.app.dao.DWDao;
import com.digiwin.emr.emr.service.IReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.Map;

public class ReportService implements IReportService {

    @Autowired
    @Qualifier("dw-dao")
    private DWDao dao;

    @Override
    public Object getAbnormalReason(Map<String, Object> info) throws DWException {
        return null;
    }
}
