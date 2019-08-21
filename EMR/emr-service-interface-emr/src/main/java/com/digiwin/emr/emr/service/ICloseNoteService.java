package com.digiwin.emr.emr.service;

import com.digiwin.app.service.DWService;

import java.util.Map;

public interface ICloseNoteService extends DWService {
    /**
     * 关闭原因查询
     * @author Jihh
     */
    //	@AllowAnonymous
    public Object getList(Map<String, Object> info) throws Exception;

    /**
     * 关闭原因新增
     * @author Jihh
     */
    //	@AllowAnonymous
    public Object postList(Map<String, Object> info) throws Exception;

    /**
     * 关闭原因删除
     * @author Jihh
     */
    //	@AllowAnonymous
    public Object deleteList(Map<String, Object> info) throws Exception;

    /**
     * 关闭原因保存并更新通知单与派工单
     * @author Jihh
     */
    //	@AllowAnonymous
    public Object putList(Map<String, Object> info) throws Exception;
}
