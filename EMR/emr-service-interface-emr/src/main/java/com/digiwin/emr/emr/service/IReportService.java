package com.digiwin.emr.emr.service;

import com.digiwin.app.service.DWService;

import java.util.Map;

public interface IReportService extends DWService {


    /**
     * 报表：异常原因分析
     * @author jiangzheng
     * @param info get
     * @return 結果
     */
     public Object getAbnormalReason(Map<String,Object> info) throws Exception;



    /**
     * 报表：计划于实际停机时间比较
     * @author jiangzheng
     * @param info get
     * @return 結果
     */
    public Object getStopHourComparation(Map<String,Object> info) throws Exception;

}
