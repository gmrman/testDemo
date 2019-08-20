package com.digiwin.emr.emr.service.impl;

import com.digiwin.app.container.exceptions.DWArgumentException;
import com.digiwin.app.dao.DWDao;
import com.digiwin.app.dao.DWServiceResultBuilder;
import com.digiwin.app.service.DWServiceContext;
import com.digiwin.emr.emr.service.IOutRepairService;
import com.digiwin.emr.emr.service.util.EquipmentUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class OutRepairService implements IOutRepairService {
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
        return null;
    }

}
