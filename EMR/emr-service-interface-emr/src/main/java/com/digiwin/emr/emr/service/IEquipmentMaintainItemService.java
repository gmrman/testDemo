package com.digiwin.emr.emr.service;

import com.digiwin.app.service.AllowAnonymous;
import com.digiwin.app.service.DWService;

import java.util.Map;

public interface IEquipmentMaintainItemService extends DWService {
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
    public Object getEqMaintainItemList(Map<String, Object> info) throws Exception;

    /**
     * 新增设备维修项目
     * @author lidonga
     * @param info 查詢信息
     * @return 結果
     */
//    @AllowAnonymous
    public Object postEqMaintainItemList(Map<String, Object> info) throws Exception;

    /**
     * 编辑设备维修项目
     * @author lidonga
     * @param info 查詢信息
     * @return 結果
     */
//    @AllowAnonymous
    public Object putEqMaintainItemList(Map<String, Object> info) throws Exception;

    /**
     * 编辑项目是否启用
     * @author lidonga
     * @param info 查詢信息
     * @return 結果
     */
//    @AllowAnonymous
    public Object putEqMaintainItemEnable(Map<String, Object> info) throws Exception;

    /**
     * 删除设备维修项目
     * @author lidonga
     * @param info 查詢信息
     * @return 結果
     */
//    @AllowAnonymous
    public Object deleteEqMaintainItemList(Map<String, Object> info) throws Exception;

    /**
     * 浏览设备备品
     * @author lidonga
     * @param info 查詢信息
     * @return 結果
     */
//    @AllowAnonymous
    public Object getEqSparePartsList(Map<String, Object> info) throws Exception;

    /**
     * 新增设备备品
     * @author lidonga
     * @param info 查詢信息
     * @return 結果
     */
//    @AllowAnonymous
    public Object postEqSparePartsList(Map<String, Object> info) throws Exception;

    /**
     * 编辑设备备品
     * @author lidonga
     * @param info 查詢信息
     * @return 結果
     */
//    @AllowAnonymous
    public Object putEqSparePartsList(Map<String, Object> info) throws Exception;

    /**
     * 编辑备品是否启用
     * @author lidonga
     * @param info 查詢信息
     * @return 結果
     */
//    @AllowAnonymous
    public Object putEqSparePartsEnable(Map<String, Object> info) throws Exception;

    /**
     * 删除设备备品
     * @author lidonga
     * @param info 查詢信息
     * @return 結果
     */
//    @AllowAnonymous
    public Object deleteEqSparePartsList(Map<String, Object> info) throws Exception;

}
