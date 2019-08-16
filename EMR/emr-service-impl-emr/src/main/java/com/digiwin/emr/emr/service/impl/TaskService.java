package com.digiwin.emr.emr.service.impl;

import com.digiwin.app.container.exceptions.DWArgumentException;
import com.digiwin.app.dao.DWDao;
import com.digiwin.app.dao.DWServiceResultBuilder;
import com.digiwin.app.service.DWServiceContext;
import com.digiwin.emr.emr.service.ITaskService;
import com.digiwin.emr.emr.service.util.EquipmentUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.*;
import java.util.stream.Collectors;

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
        StringBuffer  planSql= new StringBuffer("-${tenantsid} SELECT a.plan_sid AS id, a.eq_no, a.start_date AS `date`, plan_desc AS `desc`, 'Y' AS isplan, assign_flag AS issend, '' AS docs FROM  r_plan a " +
                " LEFT JOIN r_repair b ON a.plan_sid=b.plan_sid" +
                " WHERE tenantsid=? AND comp_no=? AND site_no=? AND a.start_date<? AND a.close_flag='N'");
        StringBuffer notifySql= new StringBuffer(" SELECT a.notify_sid AS id, a.eq_no, notify_date AS `date`, notify_desc AS `desc`, 'N' AS isplan, assign_flag AS issend, group_concat(doc_id) AS docs FROM  r_notify a" +
                " LEFT JOIN r_repair b ON a.notify_sid=b.notify_sid" +
                " LEFT JOIN r_notify_d2 c ON a.notify_sid=c.notify_sid" +
                " WHERE tenantsid=? AND comp_no=? AND site_no=? AND direct_close='N' AND a.close_flag='N' ");

        if("Y".equals(ismanager)){//管理员任务列表
            if("Y".equals(flag)){//查询已派工未结案
                planSql.append(" AND assign_flag='Y'");
                notifySql.append(" AND assign_flag='Y'");
            }else{//查询未派工的
                planSql.append(" AND assign_flag='N'");
                notifySql.append(" AND assign_flag='N'");
            }
        }else{//维修员任务列表
            if("Y".equals(flag)){//查询别人的或者未派工的
                planSql.append(" AND (assign_flag='N' OR (assign_flag='Y' AND assign_to!='"+user_id+"'))");
                notifySql.append(" AND (assign_flag='N'  OR (assign_flag='Y' AND assign_to!='"+user_id+"'))");
            }else{//查询自己的
                planSql.append(" AND assign_flag='Y' AND assign_to='"+user_id+"'");
                notifySql.append(" AND assign_flag='Y' AND assign_to='"+user_id+"'");
            }
        }
        String sql=planSql.toString()+" UNION ALL "+notifySql.toString();
        List<Map<String, Object>> resultlist = this.dao.select(sql,tenantsid, comp_no, site_no, "2019-09-16 17:20:38", tenantsid, comp_no, site_no);
        //筛选掉id为null的数据
        resultlist = resultlist.stream()
                .filter(eqObject -> eqObject.get("id")!=null).collect(Collectors.toList());
        if(resultlist.size()>0){
            List<Map<String, Object>> Data = EquipmentUtil.callApiForEquipmentByESC(tenantsid+"",comp_no,site_no,new ArrayList<String>(),new ArrayList<String>(),new ArrayList<String>(),"Y");
            //处理数据得到设备ID为key的map
            Map<Object, Object> eqmap = Data.stream()
//				.filter(eqObject -> eqObject.get("eq_id").equals(eqid))//筛选出设备ID为入参的Map
                    .collect(Collectors.toMap(eq -> eq.get("eq_id"), eq ->  eq.get("eq_name")));//将Map中的eq_id:eq_name组成新的Map

            resultlist.stream().forEach(eqob -> {
                eqob.put("eq_name",eqmap.get(eqob.get("eq_no")));
            });
        }

        return DWServiceResultBuilder.build(true,"获取列表成功！", resultlist);
    }

    @Override
    public Object getDetail(Map<String, Object> info) throws Exception {
        return null;
    }
}
