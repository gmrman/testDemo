package com.digiwin.emr.emr.service.impl;

import com.digiwin.app.container.exceptions.DWArgumentException;
import com.digiwin.app.dao.DWDao;
import com.digiwin.app.dao.DWServiceResultBuilder;
import com.digiwin.app.service.DWServiceContext;
import com.digiwin.emr.emr.service.ITaskService;
import com.digiwin.emr.emr.service.util.EquipmentUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

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

    @Override
    @Transactional(propagation = Propagation.REQUIRED,rollbackForClassName = "Exception")
    public Object postTaskClose(Map<String, Object> info) throws Exception {
        //处理接收参数
        String comp_no = (String) info.get("comp_no");
        String site_no = (String) info.get("site_no");
        String id = (String) info.get("id");//故障ID
        String closeDesc = (String) info.get("closeDesc");
        if (comp_no == null || comp_no.trim().isEmpty())
            throw new DWArgumentException("comp_no", "comp_no is null !");
        if (site_no == null || site_no.trim().isEmpty())
            throw new DWArgumentException("site_no", "site_no is null !");
        if (id == null || id.trim().isEmpty())
            throw new DWArgumentException("id", "id is null !");

        String sql="UPDATE r_notify SET direct_close='Y',direct_close_comment=? WHERE  notify_sid=? AND comp_no=? AND site_no=? ${tenantsid}";
        this.dao.update(sql,closeDesc,id,comp_no,site_no);
        return DWServiceResultBuilder.build(true,"故障通知关闭成功！",null);
    }
}
