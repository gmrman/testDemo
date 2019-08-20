package com.digiwin.emr.emr.service.impl;

import com.digiwin.app.container.exceptions.DWArgumentException;
import com.digiwin.app.dao.DWDao;
import com.digiwin.app.dao.DWServiceResultBuilder;
import com.digiwin.app.service.DWServiceContext;
import com.digiwin.emr.emr.service.IMaintainReportService;
import com.digiwin.emr.emr.service.util.EquipmentUtil;
import com.digiwin.emr.emr.service.util.Excel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

public class MaintainReportService implements IMaintainReportService {

    @Autowired
    @Qualifier("dw-dao")
    private DWDao dao;

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackForClassName = "Exception")
    public Object post(Map<String, Object> info) throws Exception {

        String site_no = (String) info.get("site_no");
        String comp_no = (String) info.get("comp_no");
        //派工单id
        String repair_sid = (String) info.get("repair_sid");
        //通知单id
        String notify_sid = (String) info.get("notify_sid");
        //计划id
        String plan_sid = (String) info.get("plan_sid");
        //设备编号
        String eq_no = (String) info.get("eq_no");
        //实际工时
        String work_hour = (String) info.get("work_hour");
        //完成时间
        String finish_date = (String) info.get("finish_date");
        //维修说明
        String work_desc = (String) info.get("work_desc");
        //维修结果
        String close_flag = (String) info.get("close_flag");
        //故障原因信息(来源故障通知单才会有故障原因选择)
        Map<String, Object> closeDetail = (Map<String, Object>) info.get("close_detail");
        //文档id列表
        List<String> docList = (List<String>) info.get("doc_list");

        if (site_no == null || site_no.isEmpty()) throw new DWArgumentException("site_no", "site_no is null !");
        if (comp_no == null || comp_no.isEmpty()) throw new DWArgumentException("comp_no", "comp_no is null !");
        if (eq_no == null || eq_no.isEmpty()) throw new DWArgumentException("eq_no", "eq_no is null !");
        if (work_hour == null || work_hour.isEmpty()) throw new DWArgumentException("work_hour", "work_hour is null !");
        if ((notify_sid == null || notify_sid.isEmpty()) && (plan_sid == null || plan_sid.isEmpty())) {
            throw new DWArgumentException("sid", "plan_sid or notify_sid is null !");
        }
        if (close_flag == null || close_flag.isEmpty()) {
            close_flag = "CLOSE";
        } //默认结案

        Map<String, Object> profile = DWServiceContext.getContext().getProfile();
        String user_id = (String) profile.get("userId");

        //insert 维修记录单

        //获取当前维修记录次数最大码+1 作为当前维修次数
        String seqSql = " select IFNULL(report_seq,0)+1 AS report_seq from r_report " +
                "  where repair_sid = ? " +
                "    and notify_sid = ? " +
                "    and plan_sid = ? " +
                "    and  eq_no = ? " +
                "    -${tenantsid} ";

        int seq = 1;
        List<Map<String, Object>> seqList = this.dao.select(seqSql, repair_sid, notify_sid, plan_sid, eq_no);
        if (seqList.size() > 0) {
            seq = Integer.parseInt(seqList.get(0).get("report_seq").toString());
        }

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

        this.dao.update(insSql, report_sid, repair_sid, notify_sid, plan_sid, eq_no, seq, work_hour, finish_date, work_desc, close_flag,
                date, user_id, "maintainReport", date, user_id, "maintainReport");


        //将文档插入到单身

        String insSql_d = " INSERT INTO r_report_d2 ( report_d2_sid, report_sid, doc_id, " +
                "                           create_date, create_by, create_program, " +
                "                           last_update_date, last_update_by, last_update_program -${tenantName} ) " +
                "                  VALUES (?,?,?,   ?,?,?,   ?,?,? -${tenantValue} ) ";

        for (int i = 0; i < docList.size(); i++) {
            //UUID 创建id
            String report_d2_sid = UUID.randomUUID().toString();
            //插入文档数据
            this.dao.update(insSql_d, report_d2_sid, report_sid, docList.get(i), date, user_id, "maintainReport", date, user_id, "maintainReport");
        }

        //維修結果[維修完成，結案]:將派工單狀態設置為結案
        //維修結果[階段結束，轉外修]:將派工單設置為外修申請(apply_out=Y)

        if ("CLOSE".equals(close_flag)) {
            String updRepair = " update r_repair set close_flag = ? , close_date = ? ," +
                    "        last_update_date = ? , last_update_by = ? , last_update_program = ? " +
                    "  where repair_sid = ? " +
                    "    -${tenantsid} ";
            this.dao.update(updRepair, 'Y', date, date, user_id, "maintainReportService");

            //故障通知单时，回写故障原因 ，
            if (!(notify_sid == null || notify_sid.isEmpty())) {
                //故障原因码
                String error_reason = (String) closeDetail.get("error_reason");
                //故障组件
                String error_part = (String) closeDetail.get("error_part");
                //故障说明
                String error_note = (String) closeDetail.get("error_note");
                if (error_reason == null || error_reason.isEmpty())
                    throw new DWArgumentException("error_reason", "error_reason is null !");

                //update 通知单
                String updSql = " update r_notify set reason_code = ? , part = ? , reason_note = ?, " +
                        "        last_update_date = ? , last_update_by = ? , last_update_program = ? " +
                        "  where notify_sid = ? " +
                        "    and comp_no = ? " +
                        "    and site_no = ? " +
                        "    ${tenantSid} ";

                this.dao.update(updSql, error_reason, error_part, error_note, new Date(), user_id, "maintainReport", notify_sid, comp_no, site_no);
            }


        } else if ("OUT".equals(close_flag)) {
            String updRepair = " update r_repair set out_flag = ? , " +
                    "        last_update_date = ? , last_update_by = ? , last_update_program = ? " +
                    "  where repair_sid = ? " +
                    "    -${tenantsid} ";
            this.dao.update(updRepair, 'Y', date, user_id, "maintainReportService");
        }

        return DWServiceResultBuilder.build(true, "", "");
    }

    @Override
    public Object getList(Map<String, Object> info) throws Exception {
        Map<String, Object> data=new HashMap<String, Object>();
        String id = (String) info.get("id");
        String comp_no = (String) info.get("comp_no");
        String site_no = (String) info.get("site_no");
        if (id == null || id.isEmpty())
            throw new DWArgumentException("关联单", "关联单据ID is null !");
        if (comp_no == null || comp_no.isEmpty())
            throw new DWArgumentException("comp_no", "comp_no is null !");
        if (site_no == null || site_no.isEmpty())
            throw new DWArgumentException("site_no", "site_no is null !");


        String sql = "-${tenantsid} SELECT eq_no, report_seq, work_hour, DATE_FORMAT(finish_date,'%m/%d') AS date, " +
                " DATE_FORMAT(finish_date,'%H:%i') AS time, work_desc, group_concat(doc_id) AS doc" +
                " FROM r_report a " +
                " LEFT JOIN r_report_d2 b ON a.report_sid = b.report_sid " +
                " WHERE repair_sid = ? OR notify_sid = ?  ORDER BY report_seq DESC";

        List<Map<String, Object>> list = this.dao.select(sql, id, id);
        if(list.size()>0){
            Map<String, Object> profile = DWServiceContext.getContext().getProfile();
            Long tenantsid = (Long) profile.get("tenantSid");
            String eq_no = list.get(0).get("eq_no").toString();
            Excel ec = new Excel();
            //获取设备ID为eq_no的设备信息
            List<Map<String, Object>> DataList = EquipmentUtil.callApiForEquipmentByESC(tenantsid+"", comp_no, site_no,new ArrayList<String>(),
                    new ArrayList<String>(),Arrays.asList(eq_no),"Y");
            //为返参组参数
            data.put("eq_no",eq_no);
            data.put("eq_name",DataList.size()>0? DataList.get(0).get("eq_name"):"");
            for(Map<String, Object> s:list){
                s.put("docs", ec.getFiletype(s.get("doc").toString()));
            }
            data.put("reportList",list);
        }
        return DWServiceResultBuilder.build(true, "获取报工记录清单", data);
    }
}
