package com.digiwin.emr.emr.service.impl;

import com.digiwin.app.container.exceptions.DWArgumentException;
import com.digiwin.app.service.DWServiceContext;
import com.digiwin.emr.emr.service.ITaskService;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;

public class TaskService implements ITaskService {

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
    public Object getDetail(Map<String, Object> info) throws Exception {
        return null;
    }
}
