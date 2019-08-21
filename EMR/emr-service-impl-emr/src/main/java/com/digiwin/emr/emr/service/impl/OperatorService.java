package com.digiwin.emr.emr.service.impl;

import com.digiwin.app.container.exceptions.DWArgumentException;
import com.digiwin.app.dao.DWDao;
import com.digiwin.app.dao.DWServiceResultBuilder;
import com.digiwin.app.service.DWServiceContext;
import com.digiwin.emr.emr.service.IOperatorService;
import com.digiwin.emr.emr.service.util.EquipmentUtil;
import com.digiwin.emr.emr.service.util.Excel;
import com.digiwin.emr.emr.service.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public class OperatorService implements IOperatorService {

    private static final String UPLOADPATH = "Path"+File.separator+"document"+ File.separator+"upload"+File.separator;

    @Autowired
    @Qualifier("dw-dao")
    private DWDao dao;

    @Override
    public Object getRepairList(Map<String, Object> info) throws Exception {
        String comp_no = (String) info.get("comp_no");
        String site_no = (String) info.get("site_no");
        String eq_no = (String) info.get("eq_no");
        if (comp_no == null || comp_no.trim().isEmpty())
            throw new DWArgumentException("comp_no", "comp_no is null !");
        if (site_no == null || site_no.trim().isEmpty())
            throw new DWArgumentException("site_no", "site_no is null !");
        if (eq_no == null)
            throw new DWArgumentException("eq_no", "eq_no is null !");

        //获取租户
        Map<String, Object> profile = DWServiceContext.getContext().getProfile();
        Long tenantsid = (Long) profile.get("tenantSid");

        // 有SQL注入风险的字段进行检验
        StringUtil.SQL(eq_no);

        if(!eq_no.isEmpty()){
            String appendSql = " and eq_no = '"+eq_no+"' ";
        }
        /**
         * notify_sid:id
         * eq_no:设备编号
         * notify_date:报修时间
         * notify_desc:故障描述
         * close_flag:是否结案
         * doc_id:文档id
         */
        String sql = "select a.notify_sid,eq_no,notify_date,notify_desc" +
                ",close_flag,group_concat(doc_id)as doc_id " +
                "from r_notify a left join r_notify_d2 b on b.notify_sid = a.notify_sid " +
                "where comp_no = ? and site_no = ? ${tenantsid} " +
                "group by notify_sid ";

        List<Map<String,Object>> dataList = new ArrayList<Map<String,Object>>();
        if(!eq_no.isEmpty()){
            String appendSql = " and eq_no = '"+eq_no+"' ";
            dataList = dao.select(sql+appendSql,comp_no,site_no);
        }else{
            dataList = dao.select(sql,comp_no,site_no);
        }

        if(dataList.size()>0){
            List<Map<String, Object>> Data = EquipmentUtil.callApiForEquipmentByESC(tenantsid+"",comp_no,site_no,new ArrayList<String>(),new ArrayList<String>(),new ArrayList<String>(),"Y");
            //处理数据得到设备ID为key的map
            Map<Object, Object> eqmap = Data.stream()
                    .collect(Collectors.toMap(eq -> eq.get("eq_id"), eq ->  eq.get("eq_name")));//将Map中的eq_id:eq_name组成新的Map
            //给dataList添加eq_name
            dataList.stream().forEach(eqob -> {
                eqob.put("eq_name",eqmap.get(eqob.get("eq_no")));
            });
        }

        //根据doc_id获取文档格式类型
        String doc_id = "";
        for(int i = 0;i<dataList.size();i++){
            doc_id = dataList.get(i).get("doc_id").toString();
            //调用公共服务获取文件id与类型的List<Map>
            Excel excel = new Excel();
            List<Map<String,Object>> fileType = excel.getFiletype(doc_id);
            dataList.get(i).put("doc_type",fileType);
        }

        return DWServiceResultBuilder.build(true, "取得作业员报修列表成功！", dataList);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackForClassName = "Exception")
    public Object postRepairList(Map<String, Object> info) throws Exception {
        String comp_no = (String) info.get("comp_no");
        String site_no = (String) info.get("site_no");
        String eq_no = (String) info.get("eq_no");
        String contact = (String) info.get("contact");
        String error_code = (String) info.get("error_code");
        String notify_desc = (String) info.get("notify_desc");
        String notify_type = (String) info.get("notify_type");
        String folder = (String) info.get("folder");
        if (comp_no == null || comp_no.trim().isEmpty())
            throw new DWArgumentException("comp_no", "comp_no is null !");
        if (site_no == null || site_no.trim().isEmpty())
            throw new DWArgumentException("site_no", "site_no is null !");
        if (eq_no == null || site_no.trim().isEmpty())
            throw new DWArgumentException("eq_no", "eq_no is null !");
        if (contact == null || contact.trim().isEmpty())
            throw new DWArgumentException("contact", "contact is null !");
        if (error_code == null || error_code.trim().isEmpty())
            throw new DWArgumentException("error_code", "error_code is null !");
        if (notify_desc == null)
            throw new DWArgumentException("notify_desc", "notify_desc is null !");
        if (notify_type == null || notify_type.trim().isEmpty())
            throw new DWArgumentException("notify_type", "notify_type is null !");
        if (folder == null || folder.trim().isEmpty())
            throw new DWArgumentException("folder", "folder is null !");

        List<String> doc_id_list = (List<String>) info.get("doc_id_list");
        String notify_sid = UUID.randomUUID().toString().replace("-", "");
        Date nowTime = new Date();
        //获取用户ID
        Map<String, Object> profile = DWServiceContext.getContext().getProfile();
        Long tenantsid = (Long) profile.get("tenantSid");
        String user_id = profile.get("userId").toString();

        //写入单头
        String headSql = "insert into r_notify (notify_sid,comp_no,site_no,eq_no" +
                ",notify_date,contact,error_code,notify_desc,notify_type" +
                ",create_date,create_by,create_program" +
                ",last_update_date,last_update_by,last_update_program,tenantsid)" +
                "values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,? ${tenantValue})";
        dao.update(headSql,notify_sid,comp_no,site_no,eq_no,nowTime,contact,error_code,notify_desc,notify_type
                ,nowTime,user_id,"Operator/postRepairList",nowTime,user_id,"Operator/postRepairList");
        //写入单身
        for(int i = 0;i<doc_id_list.size();i++){
            String notify_d2_sid = UUID.randomUUID().toString().replace("-", "");
            String bodySql = "insert into r_notify_d2 (notify_d2_sid,notify_sid" +
                    ",doc_id,create_date,create_by,create_program" +
                    ",last_update_date,last_update_by,last_update_program)" +
                    "value(?,?,?,?,?,?,?,?,? -${tenantValue})";
            dao.update(bodySql,notify_d2_sid,notify_sid,doc_id_list.get(i)
                    ,nowTime,user_id,"Operator/postRepairList",nowTime,user_id,"Operator/postRepairList");
        }

//        if (doc_id_list.size() > 0) {//将文件进行转移并删除
//            Excel excel = new Excel();
//
//            for(String doc_id:doc_id_list){
//                excel.moveFile(doc_id, UPLOADPATH + tenantsid + File.separator + notify_sid +File.separator);
//            }
//            //删除文件夹
//            excel.deleteFolder(excel.getFolderId(null,UPLOADPATH + tenantsid + File.separator + folder +File.separator));
//        }

        String message = "报修成功！";

        try {
            //TODO 检查自动派工规则，符合规则就派工，不符合就不处理
            String assign_to = checkRule(tenantsid,comp_no,site_no,eq_no);
            if(!"".equals(assign_to)){
                insertRepair(notify_sid,eq_no,assign_to,doc_id_list);
                message = "报修成功,已自动派工";
            }
        }catch (Exception e){
            System.out.println("e:"+e);
            message = "报修成功,但自动派工失败";
        }


        return DWServiceResultBuilder.build(true, message, null);
    }

    public String checkRule(Long tenantsid,String comp_no,String site_no,String eq_no) throws Exception {
        List<String> EqList = new ArrayList<String>();
        EqList.add(eq_no);
        //获取报修设备的相关咨询(包括设备编号、机型、设备分组、设备商、工厂别)
        List<Map<String,Object>> eqDetail = EquipmentUtil.callApiForEquipmentByESC(tenantsid+"",comp_no,site_no,new ArrayList<String>(),new ArrayList<String>(),EqList,"Y");
        String eq_type = eqDetail.get(0).get("eq_type").toString();
        String group_no = eqDetail.get(0).get("group_no").toString();
        //TODO 设备商暂时没有
        String vendor = "";
        //自动派工规则下的人员
        String assign_to = "";

        //获取自动派工规则
        String sql = "select e.rule_sid,tenantsid,comp_no,site_no,rule_seq,rule_type,type_value,rule_d_sid,assign_to from r_assign_rule e " +
                "left join r_assign_rule_d d on d.rule_sid = e.rule_sid " +
                "where comp_no = ? and site_no = ? ${tenantsid} order by rule_seq asc ";
        List<Map<String,Object>> ruleList = dao.select(sql,comp_no,site_no);
        for(int i = 0;i<ruleList.size();i++){
            if(ruleList.get(i).get("rule_type").equals("EQ")){
                if(ruleList.get(i).get("type_value").equals(eq_no)){
                    assign_to = ruleList.get(i).get("assign_to").toString();
                    break;
                }
                continue;
            }else if(ruleList.get(i).get("rule_type").equals("MODEL")){
                if(ruleList.get(i).get("type_value").equals(eq_type)){
                    assign_to = ruleList.get(i).get("assign_to").toString();
                    break;
                }
                continue;
            }else if(ruleList.get(i).get("rule_type").equals("GROUP")){
                if(ruleList.get(i).get("type_value").equals(group_no)){
                    assign_to = ruleList.get(i).get("assign_to").toString();
                    break;
                }
                continue;
            }else if(ruleList.get(i).get("rule_type").equals("VENDOR")){
                if(ruleList.get(i).get("type_value").equals(vendor)){
                    assign_to = ruleList.get(i).get("assign_to").toString();
                    break;
                }
                continue;
            }else if(ruleList.get(i).get("rule_type").equals("SITE")){
                if(ruleList.get(i).get("type_value").equals(site_no)){
                    assign_to = ruleList.get(i).get("assign_to").toString();
                    break;
                }
                continue;
            }
        }
        return assign_to;
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackForClassName = "Exception")
    public void insertRepair(String notify_sid,String eq_no,String assign_to,List<String> doc_id_list) throws Exception {
        String repair_sid = UUID.randomUUID().toString().replace("-", "");
        Date nowTime = new Date();
        //获取用户ID
        Map<String, Object> profile = DWServiceContext.getContext().getProfile();
        String user_id = profile.get("userId").toString();

        String headSql = "insert into r_repair (repair_sid,notify_sid,plan_sid,eq_no,assign_by" +
                ",estimate_hour,assign_to,start_date,create_date,create_by,create_program" +
                ",last_update_date,last_update_by,last_update_program)values(?,?,?,?,?,?,?,?,?,?,?,?,?,? -${tenantValue})";
        //写入单头
        dao.update(headSql,repair_sid,notify_sid,null,eq_no,"AUTO",null,assign_to,nowTime,nowTime,user_id,"insertRepair"
                ,nowTime,user_id,"insertRepair");
        //写入单身
        for(int i = 0;i<doc_id_list.size();i++) {
            String repair_d2_sid = UUID.randomUUID().toString().replace("-", "");
            String bodySql = "insert into r_repair_d2 (repair_d2_sid,repair_sid" +
                    ",doc_id,create_date,create_by,create_program" +
                    ",last_update_date,last_update_by,last_update_program)" +
                    "value(?,?,?,?,?,?,?,?,? -${tenantValue})";
            dao.update(bodySql, repair_d2_sid, repair_sid, doc_id_list.get(i)
                    , nowTime, user_id, "insertRepair", nowTime, user_id, "insertRepair");
        }
    }

    public static void main(String[] args){
        System.out.println("" == null);
        System.out.println("".trim().isEmpty());
    }
}
