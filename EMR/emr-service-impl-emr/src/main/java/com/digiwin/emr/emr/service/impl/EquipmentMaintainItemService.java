package com.digiwin.emr.emr.service.impl;

import com.digiwin.app.container.exceptions.DWArgumentException;
import com.digiwin.app.dao.DWDao;
import com.digiwin.app.dao.DWServiceResultBuilder;
import com.digiwin.app.service.DWServiceContext;
import com.digiwin.emr.emr.service.IEquipmentMaintainItemService;
import com.digiwin.emr.emr.service.util.EquipmentUtil;
import com.digiwin.emr.emr.service.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

public class EquipmentMaintainItemService implements IEquipmentMaintainItemService {

    @Autowired
    @Qualifier("dw-dao")
    private DWDao dao;

    @Override
    public Object getEqList(Map<String, Object> info) throws Exception {
        String comp_no = (String) info.get("comp_no");
        String site_no = (String) info.get("site_no");
        String open_status = (String) info.get("open_status");
        if (comp_no == null || comp_no.trim().isEmpty())
            throw new DWArgumentException("comp_no", "comp_no is null !");
        if (site_no == null || site_no.trim().isEmpty())
            throw new DWArgumentException("site_no", "site_no is null !");

        //获取租户
        Map<String, Object> profile = DWServiceContext.getContext().getProfile();
        Long tenantsid = (Long) profile.get("tenantSid");
        //获取设备单头编号列表
        List<String> EqList = getEqHead(comp_no,site_no);
        List<Map<String, Object>> Data = new ArrayList<Map<String,Object>>();
        if(!"open".equals(open_status)){
            //根据设备单头从设备中心获取设备基础资料
            Data = EquipmentUtil.callApiForEquipmentByESC(tenantsid+"",comp_no,site_no,new ArrayList<String>(),new ArrayList<String>(),EqList,"Y");
        }else{
            //根据设备单头从设备中心获取单头设备以外的设备基础资料
            Data = EquipmentUtil.callApiForEquipmentByESC(tenantsid+"",comp_no,site_no,new ArrayList<String>(),EqList,new ArrayList<String>(),"Y");
        }

        return DWServiceResultBuilder.build(true, "获取设备资料成功！", Data);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackForClassName = "Exception")
    public Object postEqList(Map<String, Object> info) throws Exception {
        String comp_no = (String) info.get("comp_no");
        String site_no = (String) info.get("site_no");
        List<String> eq_no_list = (List<String>) info.get("eq_no_list");
        if (comp_no == null || comp_no.trim().isEmpty())
            throw new DWArgumentException("comp_no", "comp_no is null !");
        if (site_no == null || site_no.trim().isEmpty())
            throw new DWArgumentException("site_no", "site_no is null !");

        //获取用户ID
        Map<String, Object> profile = DWServiceContext.getContext().getProfile();
        String user_id = profile.get("userId").toString();
        Date nowTime = new Date();
        String sql = "";

        for(int i = 0;i<eq_no_list.size();i++){
            String uuid = UUID.randomUUID().toString().replace("-", "");
            /**
             * eq_sid:id
             * comp_no:公司别
             * site_no:厂别
             * eq_no:设备编号
             * create_date:建立日期
             * create_by:建立人员
             * create_program:建立功能
             * last_update_date:最后修改日期
             * last_update_by:最后修改人员
             * last_update_program:最后修改功能
             */
            sql = "insert into r_eq (eq_sid,comp_no,site_no,eq_no" +
                    ",create_date,create_by,create_program,last_update_date" +
                    ",last_update_by,last_update_program,tenantsid) " +
                    "values(?,?,?,?,?,?,?,?,?,? ${tenantValue})";
            dao.update(sql,uuid,comp_no,site_no,eq_no_list.get(i),nowTime,user_id,"EquipmentMaintainItem/postEqList",nowTime,user_id,"EquipmentMaintain/postEqList");
        }

        return DWServiceResultBuilder.build(true, "添加设备成功", null);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackForClassName = "Exception")
    public Object deleteEqList(Map<String, Object> info) throws Exception {
        String eq_sid = (String) info.get("eq_sid");
        if (eq_sid == null || eq_sid.trim().isEmpty())
            throw new DWArgumentException("eq_sid", "eq_sid is null !");

        //删除设备列表中的设备
        String deleteEqSql = "delete from r_eq where eq_sid = ? -${tenantsid}";
        //删除设备对应的维修项目
        String deletePjSql = "delete from r_eq_list where eq_sid = ? -${tenantsid}";
        //删除设备对应的备品
        String deleteItemSql = "delete from r_eq_item where eq_sid = ? -${tenantsid}";
        //先删除关联表再删除主表
        dao.update(deletePjSql,eq_sid);
        dao.update(deleteItemSql,eq_sid);
        dao.update(deleteEqSql,eq_sid);

        return DWServiceResultBuilder.build(true, "删除设备成功", null);
    }

    @Override
    public Object getEqMaintainList(Map<String, Object> info) throws Exception {
        String eq_sid = (String) info.get("eq_sid");
        String page_number = (String) info.get("page_number");
        String page_size = (String) info.get("page_size");
        if (eq_sid == null || eq_sid.trim().isEmpty())
            throw new DWArgumentException("eq_sid", "eq_sid is null !");
        if (page_number == null || page_number.trim().isEmpty())
            throw new DWArgumentException("page_number", "page_number is null !");
        if (page_size == null || page_size.trim().isEmpty())
            throw new DWArgumentException("page_size", "page_size is null !");

        // 有SQL注入风险的字段进行检验
        StringUtil.SQL(page_number);
        StringUtil.SQL(page_size);

        //获取设备维修项目的总笔数
        String countSql = "select count(1) as count from r_eq_list where eq_sid = ? -${tenantsid}";
        List<Map<String,Object>> countList = dao.select(countSql,eq_sid);
        String total = countList.get(0).get("count").toString();

        //获取选中设备的维修项目列表
        int pageNumber = Integer.parseInt(page_number);
        int pageSize = Integer.parseInt(page_size);
        String sql = "select * from r_eq_list where eq_sid = ? -${tenantsid} limit "+(pageNumber-1)*pageSize+","+pageSize;
        List<Map<String,Object>> EqMaintainList = dao.select(sql,eq_sid);

        Map<String,Object> data = new HashMap<String,Object>();
        data.put("success",true);
        data.put("message","获取设备维修项目列表成功");
        data.put("data",EqMaintainList);
        data.put("total",total);

        return data;
    }

    //获取设备单头
    public List<String> getEqHead(String comp_no,String site_no) throws Exception {
        String sql = "select * from r_eq where comp_no = ? and site_no = ? ${tenantsid} ";
        List<Map<String,Object>> eqList = dao.select(sql,comp_no,site_no);
        List<String> EqList = new ArrayList<String>();
        for(int i = 0;i<eqList.size();i++){
            EqList.add(eqList.get(i).get("eq_no").toString());
        }
        return EqList;
    }

    public static void main(String[] args){

    }
}
