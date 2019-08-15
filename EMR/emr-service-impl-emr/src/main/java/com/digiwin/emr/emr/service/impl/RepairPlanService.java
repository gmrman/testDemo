package com.digiwin.emr.emr.service.impl;

import com.digiwin.app.container.exceptions.DWArgumentException;
import com.digiwin.app.dao.DWDao;
import com.digiwin.app.dao.DWServiceResultBuilder;
import com.digiwin.app.service.DWServiceContext;
import com.digiwin.emr.emr.service.IRepairPlanService;
import com.digiwin.emr.emr.service.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Statement;
import java.util.*;

public class RepairPlanService implements IRepairPlanService {

    @Autowired
    @Qualifier("dw-dao")
    private DWDao dao;

    //@Transactional(propagation = Propagation.REQUIRED, rollbackForClassName = "Exception")
    @Override
    public Object getList(Map<String, Object> info) throws Exception {

        List<Map<String,Object>> data = new ArrayList<Map<String,Object>>();

        Map<String, Object> profile = DWServiceContext.getContext().getProfile();
        Object tenantsid = profile.get("tenantSid");
        String comp_no = (String) info.get("comp_no");
        String site_no = (String) info.get("site_no");
        String s_date = (String) info.get("s_date");
        String e_date = (String) info.get("e_date");
        String group_no = (String) info.get("group_no");
        String eq_no = (String) info.get("eq_no");

        if (comp_no == null || comp_no.isEmpty())
            throw new DWArgumentException("comp_no", "comp_no is null !");
        if (site_no == null || site_no.isEmpty())
            throw new DWArgumentException("site_no", "site_no is null !");
        if (s_date == null)
            throw new DWArgumentException("s_date", "s_date is null !");
        if (e_date == null)
            throw new DWArgumentException("e_date", "e_date is null !");
        if (group_no == null)
            throw new DWArgumentException("group_no", "group_no is null !");
        if (eq_no == null)
            throw new DWArgumentException("eq_no", "eq_no is null !");

        StringUtil.SQL(eq_no);


        // 调用设备中心服务获取符合条件的设备信息

        // 进行查询
        String sql = " select plan_sid,eq_no,plan_desc,color_code,start_date,finish_date,day_flag,stop_hour,assign_flag " +
                     " from r_plan where tenantsid = ? and comp_no = ? and site_no = ? -${tenantsid} " +
                     " and ((start_date >= ? AND start_date <= ?) OR (start_date <= ? AND finish_date >= ?) OR (finish_date >= ? AND finish_date <= ?)) ";

        if(!"".equals(group_no)){
            sql += "  ";
        }

        if(!"".equals(eq_no)){
            sql += " and eq_no = '"+ eq_no +"' ";
        }

        List<Map<String,Object>> planList = dao.select(sql,tenantsid,comp_no,site_no,s_date,e_date,s_date,e_date,s_date,e_date);

        //循环单头，查询单身信息
        for(Map<String,Object> plan:planList){
            List<Map<String,Object>> detail = new ArrayList<Map<String,Object>>();
            String plan_sid = String.valueOf(plan.get("plan_sid"));
            String sql_detail = " select plan_d_sid,part,work_desc,std_working_hour from r_plan_d " +
                                " where plan_sid = ? ";

            detail = dao.select(sql_detail,plan_sid);

            // 获取设备名称
            plan.put("eq_name","");
            plan.put("detail",detail);
            data.add(plan);
        }

        return DWServiceResultBuilder.build(true,"Success",data);
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackForClassName = "Exception")
    @Override
    public Object postList(Map<String, Object> info) throws Exception {
        Map<String, Object> profile = DWServiceContext.getContext().getProfile();
        Object tenantsid = profile.get("tenantSid");
        String user_id = (String) profile.get("userId");
        String comp_no = (String) info.get("comp_no");
        String site_no = (String) info.get("site_no");
        String eq_no = (String) info.get("eq_no");
        String plan_desc = (String) info.get("plan_desc");
        String color_code = (String) info.get("color_code");
        String start_date = (String) info.get("start_date");
        String finish_date = (String) info.get("finish_date");
        String day_flag = (String) info.get("day_flag");
        String stop_hour = (String) info.get("stop_hour");
        String assign_flag = (String) info.get("assign_flag");
        List<Map<String,Object>> detail = (List<Map<String,Object>>)info.get("detail");
        for (Map<String,Object> detailMap:detail){
            if ((String) detailMap.get("part") == null)
                throw new DWArgumentException("detail", "part is null !");
            if ((String) detailMap.get("work_desc") == null)
                throw new DWArgumentException("detail", "work_desc is null !");
            if ((String) detailMap.get("std_working_hour") == null)
                throw new DWArgumentException("detail", "std_working_hour is null !");
        }

        Date date = new Date();
        String  plan_sid = UUID.randomUUID().toString().replaceAll("-", "");

        if (comp_no == null || comp_no.isEmpty())
            throw new DWArgumentException("comp_no", "comp_no is null !");
        if (site_no == null || site_no.isEmpty())
            throw new DWArgumentException("site_no", "site_no is null !");
        if (eq_no == null)
            throw new DWArgumentException("eq_no", "eq_no is null !");
        if (plan_desc == null)
            throw new DWArgumentException("plan_desc", "plan_desc is null !");
        if (color_code == null)
            throw new DWArgumentException("color_code", "color_code is null !");
        if (start_date == null)
            throw new DWArgumentException("start_date", "start_date is null !");
        if (finish_date == null)
            throw new DWArgumentException("finish_date", "finish_date is null !");
        if (day_flag == null)
            throw new DWArgumentException("day_flag", "day_flag is null !");
        if (stop_hour == null)
            throw new DWArgumentException("stop_hour", "stop_hour is null !");
        if (assign_flag == null)
            throw new DWArgumentException("assign_flag", "assign_flag is null !");

        // 主键为时间戳，无需检查重复，直接新增
        // 新增单头
        String sql1 = " insert into r_plan (plan_sid,tenantsid,comp_no,site_no,eq_no,plan_desc,color_code,start_date,finish_date,day_flag,stop_hour,assign_flag,create_date,create_by,create_program,last_update_date,last_update_by,last_update_program -${tenantName}) "
                + " values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,? -${tenantValue}) ";

        dao.update(sql1,plan_sid,tenantsid,comp_no,site_no,eq_no,plan_desc,color_code,start_date,finish_date,day_flag,stop_hour,assign_flag,date,user_id,"RepairPlan/postList",date,user_id,"RepairPlan/postList");

        // 新增单身
        for(Map<String,Object> detailMap:detail){
            String  plan_d_sid = UUID.randomUUID().toString().replaceAll("-", "");
            // 检核单身
            String part = (String) detailMap.get("part");
            String work_desc = (String) detailMap.get("work_desc");
            String std_working_hour = (String) detailMap.get("std_working_hour");

            String sql2 = " insert into r_plan_d (plan_d_sid,plan_sid,part,work_desc,std_working_hour,create_date,create_by,create_program,last_update_date,last_update_by,last_update_program -${tenantName}) "
                    + " values (?,?,?,?,?,?,?,?,?,?,? -${tenantValue}) ";
            dao.update(sql2,plan_d_sid,plan_sid,part,work_desc,std_working_hour,date,user_id,"RepairPlan/postList",date,user_id,"RepairPlan/postList");
        }

        return DWServiceResultBuilder.build(true,"Success",null);
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackForClassName = "Exception")
    @Override
    public Object putList(Map<String, Object> info) throws Exception {
        Map<String, Object> profile = DWServiceContext.getContext().getProfile();
        Object tenantsid = profile.get("tenantSid");
        String plan_sid = (String) profile.get("plan_sid");
        String user_id = (String) profile.get("userId");
        String comp_no = (String) info.get("comp_no");
        String site_no = (String) info.get("site_no");
        String eq_no = (String) info.get("eq_no");
        String plan_desc = (String) info.get("plan_desc");
        String color_code = (String) info.get("color_code");
        String start_date = (String) info.get("start_date");
        String finish_date = (String) info.get("finish_date");
        String day_flag = (String) info.get("day_flag");
        String stop_hour = (String) info.get("stop_hour");
        String assign_flag = (String) info.get("assign_flag");
        List<Map<String,Object>> detail = (List<Map<String,Object>>)info.get("detail");
        for (Map<String,Object> detailMap:detail){
            if ((String) detailMap.get("part") == null)
                throw new DWArgumentException("detail", "part is null !");
            if ((String) detailMap.get("work_desc") == null)
                throw new DWArgumentException("detail", "work_desc is null !");
            if ((String) detailMap.get("std_working_hour") == null)
                throw new DWArgumentException("detail", "std_working_hour is null !");
        }

        Date date = new Date();

        if (comp_no == null || comp_no.isEmpty())
            throw new DWArgumentException("comp_no", "comp_no is null !");
        if (site_no == null || site_no.isEmpty())
            throw new DWArgumentException("site_no", "site_no is null !");
        if (eq_no == null)
            throw new DWArgumentException("eq_no", "eq_no is null !");
        if (plan_desc == null)
            throw new DWArgumentException("plan_desc", "plan_desc is null !");
        if (color_code == null)
            throw new DWArgumentException("color_code", "color_code is null !");
        if (start_date == null)
            throw new DWArgumentException("start_date", "start_date is null !");
        if (finish_date == null)
            throw new DWArgumentException("finish_date", "finish_date is null !");
        if (day_flag == null)
            throw new DWArgumentException("day_flag", "day_flag is null !");
        if (stop_hour == null)
            throw new DWArgumentException("stop_hour", "stop_hour is null !");
        if (assign_flag == null)
            throw new DWArgumentException("assign_flag", "assign_flag is null !");

        // 修改单头
        String sql1 = " update r_plan set eq_no = ?,plan_desc = ?,color_code = ?,start_date = ?,finish_date = ?,day_flag = ?,stop_hour = ?," +
                      " assign_flag = ?,last_update_date = ?,last_update_by = ?,last_update_program = ? " +
                      " where plan_sid = ? -${tenantsid}) ";

        dao.update(sql1,eq_no,plan_desc,color_code,start_date,finish_date,day_flag,stop_hour,assign_flag,date,user_id,"RepairPlan/putList");

        // 删除单身
        String sql2 = " delete from r_plan_d where plan_sid = ? -${tenantsid} ";
        dao.update(sql2,plan_sid);

        // 新增单身
        for(Map<String,Object> detailMap:detail){
            String  plan_d_sid = UUID.randomUUID().toString().replaceAll("-", "");
            // 检核单身
            String part = (String) detailMap.get("part");
            String work_desc = (String) detailMap.get("work_desc");
            String std_working_hour = (String) detailMap.get("std_working_hour");

            String sql3 = " insert into r_plan_d (plan_d_sid,plan_sid,part,work_desc,std_working_hour,create_date,create_by,create_program,last_update_date,last_update_by,last_update_program -${tenantName}) "
                    + " values (?,?,?,?,?,?,?,?,?,?,? -${tenantValue}) ";
            dao.update(sql3,plan_d_sid,plan_sid,part,work_desc,std_working_hour,date,user_id,"RepairPlan/putList",date,user_id,"RepairPlan/putList");
        }

        return DWServiceResultBuilder.build(true,"Success",null);
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackForClassName = "Exception")
    @Override
    public Object deleteList(Map<String, Object> info) throws Exception {
        Map<String, Object> profile = DWServiceContext.getContext().getProfile();
        // Object tenantsid = profile.get("tenantSid");
        String plan_sid = (String)info.get("plan_sid");

        if (plan_sid == null)
            throw new DWArgumentException("plan_sid", "plan_sid is null !");

        // 先删除单头，再删除单身
        String sql1 = " delete from r_plan where plan_sid = ? -${tenantsid} ";
        String sql2 = " delete from r_plan_d where plan_sid = ? -${tenantsid} ";
        dao.update(sql1,plan_sid);
        dao.update(sql2,plan_sid);

        return DWServiceResultBuilder.build(true,"Success",null);
    }

}
