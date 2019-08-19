package com.digiwin.emr.emr.service;

import com.digiwin.app.service.DWService;

import java.util.Map;

public interface IMaintainReportService extends DWService {

    /**
     * 新增维修记录单
     * @author jiangzheng
     * @param info add
     * @return 結果
     */
    public Object post(Map<String, Object> info) throws Exception;


    /**
     * 查询报工记录列表，按时间由大到小排列
     * @author gengmr
     * @param info get
     * @return 結果
     */
    public Object getList(Map<String, Object> info) throws Exception;
}
