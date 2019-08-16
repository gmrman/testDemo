package com.digiwin.emr.emr.service.impl;

import com.digiwin.app.container.exceptions.DWArgumentException;
import com.digiwin.app.dao.DWDao;
import com.digiwin.app.dao.DWServiceResultBuilder;
import com.digiwin.app.service.DWServiceContext;
import com.digiwin.emr.emr.service.ITaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.*;

public class TaskService implements ITaskService {

    @Autowired
    @Qualifier("dw-dao")
    private DWDao dao;

    @Override
    public Object getList(Map<String, Object> info) throws Exception {
        //处理接收参数
        String comp_no = (String) info.get("comp_no");
        String site_no = (String) info.get("site_no");
        String ismanager = (String) info.get("ismanager");
        String user_id = (String) info.get("user_id");
        String flag = (String) info.get("flag");
        if (comp_no == null || comp_no.trim().isEmpty())
            throw new DWArgumentException("comp_no", "comp_no is null !");
        if (site_no == null || site_no.trim().isEmpty())
            throw new DWArgumentException("site_no", "site_no is null !");
        if (flag == null || flag.trim().isEmpty())
            throw new DWArgumentException("flag", "flag is null !");
        if (ismanager == null || ismanager.trim().isEmpty())
            throw new DWArgumentException("ismanager", "ismanager is null !");
        if("N".equals(ismanager)){
            if (user_id == null || user_id.trim().isEmpty())
                throw new DWArgumentException("user_id", "user_id is null !");
        }
        //获取租户
        Map<String, Object> profile = DWServiceContext.getContext().getProfile();
        Long tenantsid = (Long) profile.get("tenantSid");

        Calendar rightNow = Calendar.getInstance();
        rightNow.setTime(new Date());
        rightNow.add(Calendar.MONTH, 1);
        Date now = rightNow.getTime();

        //根据不同的情况拼接SQL
        StringBuffer  planSql= new StringBuffer("-${tenantsid} SELECT a.plan_sid AS id, a.eq_no, a.start_date AS `date`, plan_desc AS `desc`, assign_flag AS isplan, 'Y' AS issend, '' AS docs FROM  r_plan a " +
                " LEFT JOIN r_repair b ON a.plan_sid=b.plan_sid" +
                " WHERE tenantsid=? AND comp_no=? AND site_no=? AND start_date<?");
        StringBuffer notifySql= new StringBuffer(" SELECT a.notify_sid AS id, a.eq_no, notify_date AS `date`, notify_desc AS `desc`, 'N' AS isplan, assign_flag AS issend, group_concat(doc_id) AS docs FROM  r_notify a" +
                " LEFT JOIN r_repair b ON a.notify_sid=b.notify_sid" +
                " LEFT JOIN r_notify_d2 c ON a.notify_sid=c.notify_sid" +
                " WHERE tenantsid=? AND comp_no=? AND site_no=? AND direct_close='N'");

        if("Y".equals(ismanager)){//管理员任务列表
            if("Y".equals(flag)){//查询已派工未结案
                planSql.append(" AND assign_flag='Y' AND close_flag='N'");
                notifySql.append(" AND assign_flag='Y' AND close_flag='N'");
            }else{//查询未派工的
                planSql.append(" AND assign_flag='N' AND close_flag='N'");
                notifySql.append(" AND assign_flag='N' AND close_flag='N'");
            }
        }else{//维修员任务列表
            if("Y".equals(flag)){//查询别人的或者未派工的
                planSql.append(" AND ((assign_flag='N' AND close_flag='N') OR (assign_flag='Y' AND close_flag='N' AND assign_to!='1'))");
                notifySql.append(" AND ((assign_flag='N' AND close_flag='N') OR (assign_flag='Y' AND close_flag='N' AND assign_to!='1'))");
            }else{//查询自己的
                planSql.append(" AND assign_flag='Y' AND close_flag='N' AND assign_to='"+user_id+"'");
                notifySql.append(" AND assign_flag='Y' AND close_flag='N' AND assign_to='"+user_id+"'");
            }
        }


        return null;
    }

    @Override
    public Object getPlanDetail(Map<String, Object> info) throws Exception {

        String site_no = (String)info.get("site_no");
        String comp_no = (String)info.get("comp_no");
        //计划单id
        String plan_sid = (String)info.get("plan_sid");

        if(site_no == null || site_no.isEmpty())  throw new DWArgumentException("site_no", "site_no is null !");
        if(comp_no == null || comp_no.isEmpty())  throw new DWArgumentException("comp_no", "comp_no is null !");
        if(plan_sid == null || plan_sid.isEmpty())  throw new DWArgumentException("plan_sid", "plan_sid is null !");
        //获取租户
        Map<String, Object> profile = DWServiceContext.getContext().getProfile();
        Long tenantsid = (Long) profile.get("tenantSid");
        //获取对应计划单和工单信息
        String detailSql = " select rp.eq_no, rp.plan_desc, rp.start_date, rp.finish_date, rp.stop_hour, " +
                           "        rr.repair_sid, rr.assign_by, IFNULL(rr.estimate_hour,0) AS estimate_hour, rr.start_date " +
                           "   from r_plan rp  " +
                           "   left join r_repair rr " +
                           "     on rp.plan_sid = rr.plan_sid " +
                           "    and rp.eq_no = rr.eq_no " +
                           "  where rp.plan_sid = ? " +
                           "    and rp.tenantsid = ? " +
                           "    and rp.comp_no = ? " +
                           "    and rp.site_no = ? " +
                           "    -${tenantsid}" ;

        List<Map<String,Object>> detailList = this.dao.select(detailSql,plan_sid, tenantsid, comp_no, site_no);
        Map<String,Object> dataMap = new HashMap<>();
        if(detailList.size() > 0) {
            //获取维修部件信息
            String partSql = " select part, work_desc, std_working_hour " +
                    "   from r_repair_d " +
                    "  where repair_sid = ? ";


            dataMap = detailList.get(0);
            List<Map<String, Object>> partList = this.dao.select(partSql, dataMap.get("repair_sid"));

            dataMap.put("repair_part", partList);
        }
        return DWServiceResultBuilder.build(true, "", dataMap);
    }

    @Override
    public Object getErrorDetail(Map<String, Object> info) throws Exception {

        String site_no = (String)info.get("site_no");
        String comp_no = (String)info.get("comp_no");
        //通知单id
        String nofity_sid = (String)info.get("nofity_sid");

        if(site_no == null || site_no.isEmpty())  throw new DWArgumentException("site_no", "site_no is null !");
        if(comp_no == null || comp_no.isEmpty())  throw new DWArgumentException("comp_no", "comp_no is null !");
        if(nofity_sid == null || nofity_sid.isEmpty())  throw new DWArgumentException("nofity_sid", "nofity_sid is null !");
        //获取租户
        Map<String, Object> profile = DWServiceContext.getContext().getProfile();
        Long tenantsid = (Long) profile.get("tenantSid");
        //获取对应通知单和工单信息
        String detailSql = " select rn.eq_no, rn.notify_date, rn.contact, " +
                "        rr.repair_sid, rr.assign_by, IFNULL(rr.estimate_hour,0) AS estimate_hour, " +
                "        rr.start_date, rr.repair_desc " +
                "   from r_notify rn  " +
                "   left join r_repair rr " +
                "     on rn.nofity_sid = rr.nofity_sid " +
                "    and rn.eq_no = rr.eq_no " +
                "  where rn.nofity_sid = ? " +
                "    and rn.tenantsid = ? " +
                "    and rn.comp_no = ? " +
                "    and rn.site_no = ? " +
                "    -${tenantsid}" ;

        List<Map<String,Object>> detailList = this.dao.select(detailSql,nofity_sid, tenantsid, comp_no, site_no);
        Map<String,Object> dataMap = new HashMap<>();
        if(detailList.size() > 0) {
            //获取维修部件信息
            String docSql = " select doc_id " +
                    "   from r_repair_d2 " +
                    "  where repair_sid = ? ";

            dataMap = detailList.get(0);
            List<Map<String, Object>> docList = this.dao.select(docSql, dataMap.get("repair_sid"));

            dataMap.put("doc_list", docList);
        }
        return DWServiceResultBuilder.build(true, "", dataMap);
    }
}
