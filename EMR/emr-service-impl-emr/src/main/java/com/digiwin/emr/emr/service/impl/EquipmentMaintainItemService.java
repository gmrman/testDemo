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
    public Object getEqMaintainItemList(Map<String, Object> info) throws Exception {
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
        /**
         * eq_list_sid:设备维修项目的id
         * eq_sid:單頭id
         * part:維修部件
         * work_desc:維修說明
         * std_working_hour:標準工時
         * enable_flag:啟用
         *
         */
        String sql = "select eq_list_sid,eq_sid,part,work_desc,std_working_hour,enable_flag " +
                "from r_eq_list where eq_sid = ? -${tenantsid} limit "+(pageNumber-1)*pageSize+","+pageSize;
        List<Map<String,Object>> EqMaintainList = dao.select(sql,eq_sid);

        Map<String,Object> data = new HashMap<String,Object>();
        data.put("success",true);
        data.put("message","获取设备维修项目列表成功");
        data.put("data",EqMaintainList);
        data.put("total",total);

        return data;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackForClassName = "Exception")
    public Object postEqMaintainItemList(Map<String, Object> info) throws Exception {
        String eq_sid = (String) info.get("eq_sid");
        String part = (String) info.get("part");
        String work_desc = (String) info.get("work_desc");
        String std_working_hour = (String) info.get("std_working_hour");
        if (eq_sid == null || eq_sid.trim().isEmpty())
            throw new DWArgumentException("eq_sid", "eq_sid is null !");
        if (part == null || part.trim().isEmpty())
            throw new DWArgumentException("part", "part is null !");
        if (work_desc == null || work_desc.trim().isEmpty())
            throw new DWArgumentException("work_desc", "work_desc is null !");
        if (std_working_hour == null || std_working_hour.trim().isEmpty())
            throw new DWArgumentException("std_working_hour", "std_working_hour is null !");

        String uuid = UUID.randomUUID().toString().replace("-", "");
        //获取用户ID
        Map<String, Object> profile = DWServiceContext.getContext().getProfile();
        String user_id = profile.get("userId").toString();
        Date nowTime = new Date();
        /**
         * eq_list_sid:设备维修项目的id
         * eq_sid:單頭id
         * part:維修部件
         * work_desc:維修說明
         * std_working_hour:標準工時
         * create_date:建立日期
         * create_by:建立人员
         * create_program:建立功能
         * last_update_date:最后修改日期
         * last_update_by:最后修改人员
         * last_update_program:最后修改功能
         */
        String sql = "insert into r_eq_list (eq_list_sid,eq_sid,part,work_desc,std_working_hour" +
                ",create_date,create_by,create_program,last_update_date,last_update_by,last_update_program)" +
                "values(?,?,?,?,?,?,?,?,?,?,? -${tenantValue})";
        dao.update(sql,uuid,eq_sid,part,work_desc,std_working_hour,nowTime,user_id
                ,"EquipmentMaintainItem/postEqMaintainItemList",nowTime,user_id
                ,"EquipmentMaintainItem/postEqMaintainItemList");

        return DWServiceResultBuilder.build(true, "添加设备维修项目成功", null);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackForClassName = "Exception")
    public Object putEqMaintainItemList(Map<String, Object> info) throws Exception {
        String eq_list_sid = (String) info.get("eq_list_sid");
        String part = (String) info.get("part");
        String work_desc = (String) info.get("work_desc");
        String std_working_hour = (String) info.get("std_working_hour");
        String enable_flag = (String) info.get("enable_flag");
        if (eq_list_sid == null || eq_list_sid.trim().isEmpty())
            throw new DWArgumentException("eq_list_sid", "eq_list_sid is null !");
        if (part == null || part.trim().isEmpty())
            throw new DWArgumentException("part", "part is null !");
        if (work_desc == null || work_desc.trim().isEmpty())
            throw new DWArgumentException("work_desc", "work_desc is null !");
        if (std_working_hour == null || std_working_hour.trim().isEmpty())
            throw new DWArgumentException("std_working_hour", "std_working_hour is null !");
        if (enable_flag == null || enable_flag.trim().isEmpty())
            throw new DWArgumentException("enable_flag", "enable_flag is null !");

        String uuid = UUID.randomUUID().toString().replace("-", "");
        //获取用户ID
        Map<String, Object> profile = DWServiceContext.getContext().getProfile();
        String user_id = profile.get("userId").toString();
        Date nowTime = new Date();
        /**
         * eq_list_sid:设备维修项目的id
         * part:維修部件
         * work_desc:維修說明
         * std_working_hour:標準工時
         * enable_flag:啟用
         * last_update_date:最后修改日期
         * last_update_by:最后修改人员
         * last_update_program:最后修改功能
         */
        String sql = "update r_eq_list set part = ?,work_desc = ?" +
                ",std_working_hour = ?,enable_flag = ?,last_update_date = ?" +
                ",last_update_by = ?,last_update_program = ? where eq_list_sid = ? -${tenantsid}";
        dao.update(sql,part,work_desc,std_working_hour,enable_flag,nowTime,user_id
                ,"EquipmentMaintainItem/putEqMaintainItemList",eq_list_sid);

        return DWServiceResultBuilder.build(true, "添加设备维修项目成功", null);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackForClassName = "Exception")
    public Object putEqMaintainItemEnable(Map<String, Object> info) throws Exception {
        String eq_list_sid = (String) info.get("eq_list_sid");
        String enable_flag = (String) info.get("enable_flag");
        if (eq_list_sid == null || eq_list_sid.trim().isEmpty())
            throw new DWArgumentException("eq_list_sid", "eq_list_sid is null !");
        if (enable_flag == null || enable_flag.trim().isEmpty())
            throw new DWArgumentException("enable_flag", "enable_flag is null !");

        //获取用户ID
        Map<String, Object> profile = DWServiceContext.getContext().getProfile();
        String user_id = profile.get("userId").toString();
        Date nowTime = new Date();

        String sql = "update r_eq_list set enable_flag = ?" +
                ",last_update_date = ?,last_update_by = ?" +
                ",last_update_program = ? where eq_list_sid = ? -${tenantsid}";
        dao.update(sql,enable_flag,nowTime,user_id,"EquipmentMaintainItem/putEqMaintainItemEnable",eq_list_sid);

        return DWServiceResultBuilder.build(true, "修改设备维修项目状态成功", null);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackForClassName = "Exception")
    public Object deleteEqMaintainItemList(Map<String, Object> info) throws Exception {
        String eq_list_sid = (String) info.get("eq_list_sid");
        if (eq_list_sid == null || eq_list_sid.trim().isEmpty())
            throw new DWArgumentException("eq_list_sid", "eq_list_sid is null !");

        String sql = "delete from r_eq_list where eq_list_sid = ? -${tenantsid}";
        dao.update(sql,eq_list_sid);

        return DWServiceResultBuilder.build(true, "删除设备维修项目成功", null);
    }

    @Override
    public Object getEqSparePartsList(Map<String, Object> info) throws Exception {
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

        //获取设备备品的总笔数
        String countSql = "select count(1) as count from r_eq_item where eq_sid = ? -${tenantsid}";
        List<Map<String,Object>> countList = dao.select(countSql,eq_sid);
        String total = countList.get(0).get("count").toString();

        //获取选中设备的备品列表
        int pageNumber = Integer.parseInt(page_number);
        int pageSize = Integer.parseInt(page_size);
        /**
         * eq_item_sid:设备备品的id
         * eq_sid:單頭id
         * item_no:備品編號
         * item_name:備品名稱
         * item_cost:備品成本
         * enable_flag:啟用
         */
        String sql = "select eq_item_sid,eq_sid,item_no,item_name,item_cost,enable_flag " +
                "from r_eq_item where eq_sid = ? -${tenantsid} limit "+(pageNumber-1)*pageSize+","+pageSize;
        List<Map<String,Object>> EqSparePartsList = dao.select(sql,eq_sid);

        Map<String,Object> data = new HashMap<String,Object>();
        data.put("success",true);
        data.put("message","获取设备备品列表成功");
        data.put("data",EqSparePartsList);
        data.put("total",total);

        return data;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackForClassName = "Exception")
    public Object postEqSparePartsList(Map<String, Object> info) throws Exception {
        String eq_sid = (String) info.get("eq_sid");
        String item_no = (String) info.get("item_no");
        String item_name = (String) info.get("item_name");
        String item_cost = (String) info.get("item_cost");
        if (eq_sid == null || eq_sid.trim().isEmpty())
            throw new DWArgumentException("eq_sid", "eq_sid is null !");
        if (item_no == null || item_no.trim().isEmpty())
            throw new DWArgumentException("item_no", "item_no is null !");
        if (item_name == null || item_name.trim().isEmpty())
            throw new DWArgumentException("item_name", "item_name is null !");
        if (item_cost == null || item_cost.trim().isEmpty())
            throw new DWArgumentException("item_cost", "item_cost is null !");

        //检查备品编号是否重复
        String checkSql = "select item_no from r_eq_item where eq_sid = ? and item_no = ? -${tenantsid}";
        List<Map<String,Object>> itemNoList = dao.select(checkSql,eq_sid,item_no);
        if(itemNoList.size() == 0){
            String uuid = UUID.randomUUID().toString().replace("-", "");
            //获取用户ID
            Map<String, Object> profile = DWServiceContext.getContext().getProfile();
            String user_id = profile.get("userId").toString();
            Date nowTime = new Date();
            /**
             * eq_item_sid:设备备品的id
             * eq_sid:單頭id
             * item_no:備品編號
             * item_name:備品名稱
             * item_cost:備品成本
             * create_date:建立日期
             * create_by:建立人员
             * create_program:建立功能
             * last_update_date:最后修改日期
             * last_update_by:最后修改人员
             * last_update_program:最后修改功能
             */
            String sql = "insert into r_eq_item (eq_item_sid,eq_sid,item_no,item_name,item_cost" +
                    ",create_date,create_by,create_program,last_update_date,last_update_by,last_update_program)" +
                    "values(?,?,?,?,?,?,?,?,?,?,? -${tenantValue})";
            dao.update(sql,uuid,eq_sid,item_no,item_name,item_cost,nowTime,user_id
                    ,"EquipmentMaintainItem/postEqSparePartsList",nowTime,user_id
                    ,"EquipmentMaintainItem/postEqSparePartsList");

            return DWServiceResultBuilder.build(true, "添加设备备品成功", null);
        }else{
            return DWServiceResultBuilder.build(false, "备品编号已存在！", null);
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackForClassName = "Exception")
    public Object putEqSparePartsList(Map<String, Object> info) throws Exception {
        String eq_sid = (String) info.get("eq_sid");
        String eq_item_sid = (String) info.get("eq_item_sid");
        String old_item_no = (String) info.get("old_item_no");
        String item_no = (String) info.get("item_no");
        String item_name = (String) info.get("item_name");
        String item_cost = (String) info.get("item_cost");
        String enable_flag = (String) info.get("enable_flag");
        if (eq_sid == null || eq_sid.trim().isEmpty())
            throw new DWArgumentException("eq_sid", "eq_sid is null !");
        if (eq_item_sid == null || eq_item_sid.trim().isEmpty())
            throw new DWArgumentException("eq_item_sid", "eq_item_sid is null !");
        if (old_item_no == null || old_item_no.trim().isEmpty())
            throw new DWArgumentException("old_item_no", "old_item_no is null !");
        if (item_no == null || item_no.trim().isEmpty())
            throw new DWArgumentException("item_no", "item_no is null !");
        if (item_name == null || item_name.trim().isEmpty())
            throw new DWArgumentException("item_name", "item_name is null !");
        if (item_cost == null || item_cost.trim().isEmpty())
            throw new DWArgumentException("item_cost", "item_cost is null !");
        if (enable_flag == null || enable_flag.trim().isEmpty())
            throw new DWArgumentException("enable_flag", "enable_flag is null !");

        String uuid = UUID.randomUUID().toString().replace("-", "");
        //获取用户ID
        Map<String, Object> profile = DWServiceContext.getContext().getProfile();
        String user_id = profile.get("userId").toString();
        Date nowTime = new Date();
        Boolean success = true;
        String message = "编辑设备备品成功";
        /**
         * eq_item_sid:设备备品的id
         * eq_sid:單頭id
         * item_no:備品編號
         * item_name:備品名稱
         * item_cost:備品成本
         * enable_flag:啟用
         * last_update_date:最后修改日期
         * last_update_by:最后修改人员
         * last_update_program:最后修改功能
         */
        String sql = "update r_eq_item set item_no = ?,item_name = ?,item_cost = ?,enable_flag = ?" +
                ",last_update_date = ?,last_update_by = ?,last_update_program = ? where eq_item_sid = ? -${tenantsid}";
        //如果备品编号变更则需要检查重复性
        if(!old_item_no.equals(item_no)){
            //检查备品编号是否重复
            String checkSql = "select item_no from r_eq_item where eq_sid = ? and item_no = ? -${tenantsid}";
            List<Map<String,Object>> itemNoList = dao.select(checkSql,eq_sid,item_no);
            if(itemNoList.size() == 0){
                success = true;
            }else{
                success = false;
                message = "备品编号已存在！";
            }
        }
        if(success){
            dao.update(sql,item_no,item_name,item_cost,enable_flag,nowTime,user_id
                    ,"EquipmentMaintainItem/putEqSparePartsList",eq_item_sid);
        }

        return DWServiceResultBuilder.build(success, message, null);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackForClassName = "Exception")
    public Object putEqSparePartsEnable(Map<String, Object> info) throws Exception {
        String eq_item_sid = (String) info.get("eq_item_sid");
        String enable_flag = (String) info.get("enable_flag");
        if (eq_item_sid == null || eq_item_sid.trim().isEmpty())
            throw new DWArgumentException("eq_item_sid", "eq_item_sid is null !");
        if (enable_flag == null || enable_flag.trim().isEmpty())
            throw new DWArgumentException("enable_flag", "enable_flag is null !");

        //获取用户ID
        Map<String, Object> profile = DWServiceContext.getContext().getProfile();
        String user_id = profile.get("userId").toString();
        Date nowTime = new Date();

        String sql = "update r_eq_item set enable_flag = ?" +
                ",last_update_date = ?,last_update_by = ?" +
                ",last_update_program = ? where eq_item_sid = ? -${tenantsid}";
        dao.update(sql,enable_flag,nowTime,user_id,"EquipmentMaintainItem/putEqSparePartsEnable",eq_item_sid);

        return DWServiceResultBuilder.build(true, "修改设备备品状态成功", null);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackForClassName = "Exception")
    public Object deleteEqSparePartsList(Map<String, Object> info) throws Exception {
        String eq_item_sid = (String) info.get("eq_item_sid");
        if (eq_item_sid == null || eq_item_sid.trim().isEmpty())
            throw new DWArgumentException("eq_item_sid", "eq_item_sid is null !");

        String sql = "delete from r_eq_item where eq_item_sid = ? -${tenantsid}";
        dao.update(sql,eq_item_sid);

        return DWServiceResultBuilder.build(true, "删除设备备品成功", null);
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

}
