package com.digiwin.emr.emr.service.impl;

import com.digiwin.app.container.exceptions.DWArgumentException;
import com.digiwin.app.dao.DWDao;
import com.digiwin.app.dao.DWServiceResultBuilder;
import com.digiwin.app.service.DWServiceContext;
import com.digiwin.emr.emr.service.IMaintainClosingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Map;

public class MaintainClosingService implements IMaintainClosingService {

    @Autowired
    @Qualifier("dw-dao")
    private DWDao dao;

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackForClassName = "Exception")
    public Object post(Map<String, Object> info) throws Exception {

        String site_no = (String)info.get("site_no");
        String comp_no = (String)info.get("comp_no");
        //通知单id
        String notify_sid = (String)info.get("notify_sid");
        //故障原因码
        String error_reason = (String)info.get("error_reason");
        //故障组件
        String error_part = (String)info.get("error_part");
        //故障说明
        String error_note = (String)info.get("error_note");

        if(site_no == null || site_no.isEmpty())  throw new DWArgumentException("site_no", "site_no is null !");
        if(comp_no == null || comp_no.isEmpty())  throw new DWArgumentException("comp_no", "comp_no is null !");
        if(notify_sid == null || notify_sid.isEmpty())  throw new DWArgumentException("notify_sid", "notify_sid is null !");

        Map<String, Object> profile = DWServiceContext.getContext().getProfile();
        String user_id = (String) profile.get("userId");

        //update 通知单

        String updSql = " update r_notify set reason_code = ? , part = ? , reason_note = ?, " +
                        "        last_update_date = ? , last_update_by = ? , last_update_program = ? "+
                        "  where notify_sid = ? " +
                        "    and comp_no = ? " +
                        "    and site_no = ? "+
                        "    ${tenantSid} ";

        this.dao.update(updSql,error_reason,error_part,error_note,new Date(), user_id, "maintainclosingService",notify_sid,comp_no,site_no);

        return DWServiceResultBuilder.build(true, "", "");
    }
}
