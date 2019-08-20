package com.digiwin.emr.emr.service.impl;

import com.digiwin.app.container.exceptions.DWArgumentException;
import com.digiwin.app.dao.DWDao;
import com.digiwin.app.dao.DWServiceResultBuilder;
import com.digiwin.app.service.DWServiceContext;
import com.digiwin.emr.emr.service.IDispatchService;
import com.digiwin.emr.emr.service.util.Excel;
import com.digiwin.emr.emr.service.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
//派工单有关接口
public class DispatchService implements IDispatchService {

    private static final String UPLOADPATH = "Path"+File.separator+"document"+ File.separator+"upload"+File.separator;

    @Autowired
    @Qualifier("dw-dao")
    private DWDao dao;

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackForClassName = "Exception")
    public Object post(Map<String, Object> info) throws Exception {
        String uuid = UUID.randomUUID().toString().replace("-", "");
        String id = (String) info.get("id");
        String type = (String) info.get("type");
        String eq_no = (String) info.get("eq_no");
        String assign_by = (String) info.get("assign_by");
        String assign_to = (String) info.get("assign_to");
        String estimate_hour = (String) info.get("estimate_hour");
        String start_date = (String) info.get("start_date");
        String repair_desc = (String) info.get("repair_desc");
        String folder = (String) info.get("folder");
        List<Map<String, Object>> dispatchDetail = (List<Map<String, Object>>) info.get("dispatchDetail");
        List<String> dispatchDocs = (List<String>) info.get("dispatchDocs");

        if (id == null || id.isEmpty())
            throw new DWArgumentException("关联单", "关联单据ID is null !");
        if (type == null || type.isEmpty())
            throw new DWArgumentException("type", "type is null !");
        if (eq_no == null || eq_no.isEmpty())
            throw new DWArgumentException("eq_no", "eq_no is null !");
        if (assign_by == null || assign_by.isEmpty())
            throw new DWArgumentException("assign_by", "assign_by is null !");
        if (assign_to == null || assign_to.isEmpty())
            throw new DWArgumentException("assign_to", "assign_to is null !");
        if (estimate_hour == null || estimate_hour.isEmpty())
            throw new DWArgumentException("estimate_hour", "estimate_hour is null !");
        if (start_date == null || start_date.isEmpty())
            throw new DWArgumentException("start_date", "start_date is null !");
        if (folder == null || folder.isEmpty())
            throw new DWArgumentException("folder", "folder is null !");
//        if (repair_desc == null || repair_desc.isEmpty())
//            throw new DWArgumentException("repair_desc", "repair_desc is null !");
        StringUtil.SQL(id);
        StringUtil.SQL(type);
        StringUtil.SQL(eq_no);
        StringUtil.SQL(assign_by);
        StringUtil.SQL(assign_to);
        StringUtil.SQL(estimate_hour);
        StringUtil.SQL(start_date);
        StringUtil.SQL(folder);

        //判断通知单ID还是计划ID
        String notify_sid = null, plan_sid = null;
        switch (type) {
            case "1"://通知单
                notify_sid = id;
                break;
            case "2"://计划单
                plan_sid = id;
                break;
            default:
                throw new DWArgumentException("type", "Unexpected value: " + type);
        }
        ;
        Date now = new Date();
        Map<String, Object> profile = DWServiceContext.getContext().getProfile();
        Long tenantsid = (Long)profile.get("tenantSid");
        String user_id = (String) profile.get("userId");
        String servicename="DispatchService/post";

        //新增派工单
        String sql = "-${tenantsid} INSERT INTO r_repair(repair_sid,notify_sid,plan_sid,eq_no,assign_by," +
                "                                      assign_to,estimate_hour,start_date,repair_desc,create_date," +
                "                                      create_by,create_program,last_update_date,last_update_by,last_update_program)" +
                "   VALUES(?,?,?,?,?, ?,?,?,?,?, ?,?,?,?,?)";

        this.dao.update(sql, uuid, notify_sid, plan_sid, eq_no, assign_by,
                assign_to, estimate_hour, start_date, repair_desc, now,
                user_id, servicename, now, user_id, servicename);

        if ("2".equals(type) && dispatchDetail.size() > 0) {//如果是维修计划，插入维修部件
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String nowStr = sdf.format(now);
            String detailuuid = "";
            detailuuid = UUID.randomUUID().toString().replace("-", "");
            StringBuffer dispatchDetailStr = new StringBuffer("-${tenantsid} INSERT INTO r_repair_d(repair_d_sid, repair_sid, part, work_desc, std_working_hour," +
                    " create_date, create_by,create_program,last_update_date,last_update_by,last_update_program) VALUES");
            for (Map<String, Object> detail : dispatchDetail) {
                dispatchDetailStr.append("('" + detailuuid + "','" + uuid + "','" + detail.get("part") + "','" + detail.get("work_desc") + "','" + detail.get("std_working_hour") + "'," +
                        "'"+nowStr+"','"+user_id+"','"+servicename+"','"+nowStr+"','"+user_id+"','"+servicename+"'),");
            }
            sql = dispatchDetailStr.toString();
            sql = sql.substring(0, sql.length() - 1);
            this.dao.update(sql);
        }
        if (dispatchDocs.size() > 0) {//将文件进行转移并删除
            Excel ec = new Excel();

            for(String docid:dispatchDocs){
                ec.moveFile(docid, UPLOADPATH + tenantsid + File.separator + id +File.separator);
            }
            //删除文件夹
            ec.deleteFolder(ec.getFolderId(null,UPLOADPATH + tenantsid + File.separator + folder +File.separator));
        }


        return DWServiceResultBuilder.build(true,"派工成功！",null);
    }
}
