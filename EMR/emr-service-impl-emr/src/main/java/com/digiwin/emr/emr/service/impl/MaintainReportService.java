package com.digiwin.emr.emr.service.impl;

import com.digiwin.app.container.exceptions.DWArgumentException;
import com.digiwin.app.dao.DWDao;
import com.digiwin.app.dao.DWServiceResultBuilder;
import com.digiwin.app.service.DWServiceContext;
import com.digiwin.emr.emr.service.IMaintainReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class MaintainReportService implements IMaintainReportService {

    @Autowired
    @Qualifier("dw-dao")
    private DWDao dao;

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackForClassName = "Exception")
    public Object post(Map<String, Object> info) throws Exception {

        String site_no = (String)info.get("site_no");
        String comp_no = (String)info.get("comp_no");
        //派工单id
        String repair_sid = (String)info.get("repair_sid");
        //通知单id
        String notify_sid = (String)info.get("notify_sid");
        //计划id
        String plan_sid = (String)info.get("plan_sid");
        //设备编号
        String eq_no = (String)info.get("eq_no");
        //序号
        String report_seq = (String)info.get("report_seq");
        //实际工时
        String work_hour = (String)info.get("work_hour");
        //完成时间
        String finish_date = (String)info.get("finish_date");
        //维修说明
        String work_desc = (String)info.get("work_desc");
        //维修结果
        String close_flag = (String)info.get("close_flag");
        //文档id列表
        List<String> docList = (List<String>)info.get("doc_list");

        if(site_no == null || site_no.isEmpty())  throw new DWArgumentException("site_no", "site_no is null !");
        if(comp_no == null || comp_no.isEmpty())  throw new DWArgumentException("comp_no", "comp_no is null !");
        if(notify_sid == null || notify_sid.isEmpty())  throw new DWArgumentException("notify_sid", "notify_sid is null !");
        if(repair_sid == null || repair_sid.isEmpty())  throw new DWArgumentException("repair_sid", "repair_sid is null !");
        if(plan_sid == null || plan_sid.isEmpty())  throw new DWArgumentException("plan_sid", "plan_sid is null !");
        if(eq_no == null || eq_no.isEmpty())  throw new DWArgumentException("eq_no", "eq_no is null !");
        if(report_seq == null || report_seq.isEmpty()) { report_seq = "1";} // 默认第一次
        if(work_hour == null || work_hour.isEmpty()) throw new DWArgumentException("work_hour", "work_hour is null !");
        if(close_flag == null || close_flag.isEmpty()) { close_flag = "CLOSE";} //默认结案

        Map<String, Object> profile = DWServiceContext.getContext().getProfile();
        String user_id = (String) profile.get("userId");

        //insert 维修记录单

        //UUID 创建维修记录单号
        String report_sid = UUID.randomUUID().toString();
        //统一异动时间
        Date date = new Date();

        String insSql = " INSERT INTO r_report (report_sid,repair_sid, notify_sid, plan_sid, eq_no, " +
                        "                       report_seq, work_hour, finish_date, work_desc, close_flag, " +
                        "                       create_date, create_by, create_program, " +
                        "                       last_update_date, last_update_by, last_update_program -${tenantName}) " +
                        "               VALUES (?,?,?,?,?, " +
                        "                       ?,?,?,?,?, " +
                        "                       ?,?,?,?,?,? -{tenantValue} ) ";

        this.dao.update(insSql,report_sid,repair_sid, notify_sid, plan_sid, eq_no,report_seq, work_hour, finish_date, work_desc, close_flag,
                                        date,user_id,"maintainReport",date,user_id,"maintainReport");


        //将文档插入到单身

        String insSql_d = " INSERT INTO r_report_d2 ( report_d2_sid, report_sid, doc_id, " +
                          "                           create_date, create_by, create_program, " +
                          "                           last_update_date, last_update_by, last_update_program -${tenantName} ) " +
                          "                  VALUES (?,?,?,   ?,?,?,   ?,?,? -${tenantValue} ) ";

        for(int i=0; i<docList.size();i++){
            //UUID 创建id
            String report_d2_sid = UUID.randomUUID().toString();
           //插入文档数据
            this.dao.update(insSql_d, report_d2_sid, report_sid, docList.get(i),date,user_id,"maintainReport",date,user_id,"maintainReport");
        }

        //維修結果[維修完成，結案]:將派工單狀態設置為結案
        //維修結果[階段結束，轉外修]:將派工單設置為外修申請(apply_out=Y)

        if("CLOSE".equals(close_flag)){
            String updRepair = " update r_repair set close_flag = ? , close_date = ? ," +
                    "        last_update_date = ? , last_update_by = ? , last_update_program = ? "+
                    "  where repair_sid = ? "+
                    "    -${tenantsid} ";
           this.dao.update(updRepair,'Y',date,date,user_id,"maintainReportService");

        }else if("OUT".equals(close_flag)){
            String updRepair = " update r_repair set out_flag = ? , " +
                    "        last_update_date = ? , last_update_by = ? , last_update_program = ? "+
                    "  where repair_sid = ? "+
                    "    -${tenantsid} ";
            this.dao.update(updRepair,'Y',date,user_id,"maintainReportService");
        }

        return DWServiceResultBuilder.build(true, "", "");
    }
}
