package com.digiwin.emr.emr.service;

import com.digiwin.app.service.AllowAnonymous;
import com.digiwin.app.service.DWService;

import java.util.Map;

public interface IEquipmentMaintainService extends DWService {
    /**
     * 取得设备清单
     * @author lidonga
     * @param info 查詢信息
     * @return 結果
     */
//    @AllowAnonymous
    public Object getEqList(Map<String, Object> info) throws Exception;

    /**
     * 保存设备清单
     * @author lidonga
     * @param info 查詢信息
     * @return 結果
     */
//    @AllowAnonymous
    public Object postEqList(Map<String, Object> info) throws Exception;

    /**
     * 删除设备
     * @author lidonga
     * @param info 查詢信息
     * @return 結果
     */
//    @AllowAnonymous
    public Object deleteEqList(Map<String, Object> info) throws Exception;

    /**
     * 浏览设备维修项目
     * @author lidonga
     * @param info 查詢信息
     * @return 結果
     */
//    @AllowAnonymous
    public Object getEqMaintainList(Map<String, Object> info) throws Exception;

}
