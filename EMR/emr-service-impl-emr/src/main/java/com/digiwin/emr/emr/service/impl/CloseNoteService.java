package com.digiwin.emr.emr.service.impl;

import com.digiwin.app.container.exceptions.DWArgumentException;
import com.digiwin.app.dao.DWDao;
import com.digiwin.app.dao.DWServiceResultBuilder;
import com.digiwin.app.service.DWServiceContext;
import com.digiwin.emr.emr.service.ICloseNoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

public class CloseNoteService implements ICloseNoteService {

    @Autowired
    @Qualifier("dw-dao")
    private DWDao dao;

    //@Transactional(propagation = Propagation.REQUIRED, rollbackForClassName = "Exception")
    @Override
    public Object getList(Map<String, Object> info) throws Exception {
        List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();

        // 记录传入参数
        Map<String, Object> profile = DWServiceContext.getContext().getProfile();
        Object tenantsid = profile.get("tenantSid");
        String user_id = (String) profile.get("userId");
        String comp_no = (String) info.get("comp_no");
        String site_no = (String) info.get("site_no");

        if (comp_no == null || comp_no.isEmpty())
            throw new DWArgumentException("comp_no", "comp_no is null !");
        if (site_no == null || site_no.isEmpty())
            throw new DWArgumentException("site_no", "site_no is null !");

        String sql = " select note_sid,close_note from r_close_note " +
                " where tenantsid = ? and comp_no = ? and site_no = ? and user_id = ? -${tenantsid} ";

        data = dao.select(sql,tenantsid,comp_no,site_no,user_id);

        return DWServiceResultBuilder.build(true,"Success",data);
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackForClassName = "Exception")
    @Override
    public Object postList(Map<String, Object> info) throws Exception {
        // 记录传入参数
        Map<String, Object> profile = DWServiceContext.getContext().getProfile();
        Object tenantsid = profile.get("tenantSid");
        String user_id = (String) profile.get("userId");
        String comp_no = (String) info.get("comp_no");
        String site_no = (String) info.get("site_no");
        String close_note = (String) info.get("close_note");
        String note_sid = UUID.randomUUID().toString().replace("-", "");
        Date date = new Date();

        if (comp_no == null || comp_no.isEmpty())
            throw new DWArgumentException("comp_no", "comp_no is null !");
        if (site_no == null || site_no.isEmpty())
            throw new DWArgumentException("site_no", "site_no is null !");
        if (close_note == null)
            throw new DWArgumentException("close_note", "close_note is null !");

        String sql = " insert into r_close_note(note_sid,tenantsid,comp_no,site_no,user_id,close_note,create_date,create_by,create_program,last_update_date,last_update_by,last_update_program -${tenantName}) " +
                " values (?,?,?,?,?, ?,?,?,?,?, ?,? -${tenantValue}) ";

        dao.update(sql,note_sid,tenantsid,comp_no,site_no,user_id,close_note,date,user_id,"CloseNote/postList",date,user_id,"CloseNote/postList");

        return DWServiceResultBuilder.build(true,"Success",null);
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackForClassName = "Exception")
    @Override
    public Object deleteList(Map<String, Object> info) throws Exception {
        String note_sid = (String) info.get("note_sid");
        if (note_sid == null)
            throw new DWArgumentException("note_sid", "note_sid is null !");

        String sql = " delete from r_close_note where note_sid = ? -${tenantsid} ";

        dao.update(sql,note_sid);

        return DWServiceResultBuilder.build(true,"Success",null);
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackForClassName = "Exception")
    @Override
    public Object putList(Map<String, Object> info) throws Exception {
        Map<String, Object> profile = DWServiceContext.getContext().getProfile();
        Object tenantsid = profile.get("tenantSid");
        String user_id = (String) profile.get("userId");
        String comp_no = (String) info.get("comp_no");
        String site_no = (String) info.get("site_no");
        String close_note = (String) info.get("close_note");
        String notify_sid = (String) info.get("notify_sid");
        String issend = (String) info.get("issend");
        Date date = new Date();

        if (comp_no == null || comp_no.isEmpty())
            throw new DWArgumentException("comp_no", "comp_no is null !");
        if (site_no == null || site_no.isEmpty())
            throw new DWArgumentException("site_no", "site_no is null !");
        if (close_note == null)
            throw new DWArgumentException("close_note", "close_note is null !");
        if (notify_sid == null)
            throw new DWArgumentException("notify_sid", "notify_sid is null !");
        if (issend == null)
            throw new DWArgumentException("issend", "issend is null !");

        // 更新通知单（如果有派工单则更新派工单）
        String sql1 = " update r_notify set close_flag = 'Y',close_date = ?,direct_close = 'Y',direct_close_comment = ?,last_update_date = ?,last_update_by = ?,last_update_program = ? " +
                " where notify_sid = ? -${tenantsid}";
        dao.update(sql1,date,close_note,date,user_id,"CloseNote/putList",notify_sid);

        // 若已派工，则更新派工单为结案
        if("Y".equals(issend)){
            String sql2 = " update r_repair set close_flag = 'Y',close_date = ?,last_update_date = ?,last_update_by = ?,last_update_program = ? " +
                    " where notify_sid = ? -${tenantsid}";
            dao.update(sql2,date,date,user_id,"CloseNote/putList",notify_sid);
        }

        return DWServiceResultBuilder.build(true,"Success",null);
    }

}
