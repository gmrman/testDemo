package com.digiwin.emr.emr.service.impl;

import com.digiwin.app.container.exceptions.DWArgumentException;
import com.digiwin.app.dao.DWDao;
import com.digiwin.app.dao.DWServiceResultBuilder;
import com.digiwin.app.service.DWServiceContext;
import com.digiwin.emr.emr.service.IOutRepairService;
import com.digiwin.emr.emr.service.util.StringUtil;
import com.digiwin.emr.emr.service.util.EquipmentUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

public class OutRepairService implements IOutRepairService {
    @Autowired
    @Qualifier("dw-dao")
    private DWDao dao;

    //@Transactional(propagation = Propagation.REQUIRED, rollbackForClassName = "Exception")
    @Override
    public Object getList(Map<String, Object> info) throws Exception {
        List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();

        Map<String, Object> profile = DWServiceContext.getContext().getProfile();
        Object tenantsid = profile.get("tenantSid");
        String comp_no = (String) info.get("comp_no");
        String site_no = (String) info.get("site_no");

        if (comp_no == null || comp_no.isEmpty())
            throw new DWArgumentException("comp_no", "comp_no is null !");
        if (site_no == null || site_no.isEmpty())
            throw new DWArgumentException("site_no", "site_no is null !");

        String sql = " select a.out_sid,a.notify_sid,a.repair_sid,a.eq_no,a.out_date,a.out_note,a.partner_no,a.contact,a.out_cost,a.close_date,a.close_note,a.doc_id, " +
                " b.notify_date,b.close_flag " +
                " from r_repair_out a " +
                " left join r_notify b on a.notify_sid = b.notify_sid " +
                " where b.tenantsid = ? and b.comp_no = ? and b.site_no = ? -${tenantsid}  ";

        data = dao.select(sql,tenantsid,comp_no,site_no);

        // 获取设备中心设备信息
        List<String> group_list = new ArrayList<String>();
        List<String> ineq_list = new ArrayList<String>();
        List<String> outeq_list = new ArrayList<String>();
        // 调用设备中心服务获取符合条件的设备信息
        List<Map<String,Object>> EqList = EquipmentUtil.callApiForEquipmentByESC(String.valueOf(tenantsid),comp_no,site_no,group_list,outeq_list,ineq_list,"Y");

        for(Map<String,Object> dataMap:data){
            String out_sid = String.valueOf(dataMap.get("out_sid"));
            String notify_sid = String.valueOf(dataMap.get("notify_sid"));
            String eq_no = String.valueOf(dataMap.get("eq_no"));
            String eq_name = "";
            // 获取通知单文档ID
            String dsql1 = " select notify_d2_sid,doc_id from r_notify_d2 where notify_sid = ? -${tenantsid}";
            List<Map<String,Object>> notifyDoc = dao.select(dsql1,notify_sid);
            // 获取叫修记录文档ID
            String dsql2 = " select repair_out_d_sid,doc_id from r_repair_out_d where out_sid = ? -${tenantsid}";
            List<Map<String,Object>> repairOutDoc = dao.select(dsql2,out_sid);
            // 获取设备名称
            for (Map<String, Object> Eq : EqList) {
                if(dataMap.get("eq_no").equals(Eq.get("eq_id"))){
                    eq_name = String.valueOf(Eq.get("eq_name"));
                }
            }
            dataMap.put("eq_name",eq_name);
            dataMap.put("notifyDoc",notifyDoc);
            dataMap.put("repairOutDoc",repairOutDoc);
        }

        return DWServiceResultBuilder.build(true,"Success",data);
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackForClassName = "Exception")
    @Override
    public Object putList(Map<String, Object> info) throws Exception {
        return null;
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackForClassName = "Exception")
    @Override
    public Object postList(Map<String, Object> info) throws Exception {
        String uuid = UUID.randomUUID().toString().replace("-", "");
        String id = (String) info.get("id");
        String contact = (String) info.get("contact");
        String eq_no = (String) info.get("eq_no");
        String out_note = (String) info.get("out_note");
        String partner_no = (String) info.get("partner_no");
        String info_prod = (String) info.get("info_prod");
        String info_ehi = (String) info.get("info_ehi");
        String info_emr = (String) info.get("info_emr");
        String info_daq = (String) info.get("info_daq");

        if (id == null || id.isEmpty())
            throw new DWArgumentException("id", "id is null !");
        if (contact == null || contact.isEmpty())
            throw new DWArgumentException("contact", "contact is null !");
        if (eq_no == null || eq_no.isEmpty())
            throw new DWArgumentException("eq_no", "eq_no is null !");
        if (partner_no == null || out_note.isEmpty())
            throw new DWArgumentException("partner_no", "partner_no is null !");
        if (out_note == null)
            throw new DWArgumentException("out_note", "out_note is null !");
        if (info_prod == null || info_prod.isEmpty())
            info_prod = "Y";
        if (info_ehi == null || info_ehi.isEmpty())
            info_ehi = "Y";
        if (info_emr == null || info_emr.isEmpty())
            info_emr = "Y";
        if (info_daq == null || info_daq.isEmpty())
            info_daq = "Y";

        StringUtil.SQL(id);
        StringUtil.SQL(contact);
        StringUtil.SQL(eq_no);
        StringUtil.SQL(out_note);
        StringUtil.SQL(partner_no);
        StringUtil.SQL(out_note);
        StringUtil.SQL(info_prod);
        StringUtil.SQL(info_ehi);
        StringUtil.SQL(info_emr);
        StringUtil.SQL(info_daq);
        //获取用户信息
        Map<String, Object> profile = DWServiceContext.getContext().getProfile();
        String user_id = (String) profile.get("userId");
        Date now = new Date();
        String servicename = "OutRepairService/postList";

        //查询是否有派工单
        String sql = "-${tenantsid} SELECT repair_sid FROM r_repair WHERE notify_sid = ?";
        List<Map<String, Object>> repairs = this.dao.select(sql, id);
        String repair_sid = "";

        if(repairs.size()>0){
            //存在派工单则获取到派工单ID
            repair_sid = repairs.get(0).get("repair_sid").toString();
            sql = "-${tenantsid} UPDATE r_notify SET out_flag='Y',last_update_date=?,last_update_by=?,last_update_program=? WHERE notify_sid=?";
            this.dao.update(sql, now,user_id,servicename,id);
        }

        sql = "-${tenantsid} UPDATE r_notify SET out_flag='Y',last_update_date=?,last_update_by=?,last_update_program=? WHERE notify_sid=?";
        this.dao.update(sql, now,user_id,servicename,id);
        //插入叫修记录档
        sql = "-${tenantsid} INSERT INTO r_repair_out(out_sid, notify_sid, repair_sid, eq_no, out_date," +
                "                out_note, partner_no, contact, info_prod, info_ehi, info_emr, info_daq," +
                "                create_date, create_by,create_program,last_update_date,last_update_by,last_update_program)" +
                "   VALUES(?,?,?,?,?, ?,?,?,?,?,?,?, ?,?,?,?,?,?)";
        int num = this.dao.update(sql, uuid, id, repair_sid, eq_no, now,
                out_note, partner_no, contact, info_prod, info_ehi, info_emr, info_daq,
                now, user_id, servicename, now, user_id, servicename);
        if(num<=0)
            throw new DWArgumentException("insert", "no data insert!");

        return DWServiceResultBuilder.build(true, "添加叫修记录成功",null);
    }

}
