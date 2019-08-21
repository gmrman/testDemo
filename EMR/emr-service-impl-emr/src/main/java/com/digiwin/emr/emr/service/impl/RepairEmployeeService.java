package com.digiwin.emr.emr.service.impl;

import com.digiwin.app.container.exceptions.DWArgumentException;
import com.digiwin.app.dao.DWDao;
import com.digiwin.app.dao.DWServiceResultBuilder;
import com.digiwin.app.service.DWServiceContext;
import com.digiwin.emr.emr.service.IRepairEmplyeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.List;
import java.util.Map;

//选择派工维修员有关接口
public class RepairEmployeeService implements IRepairEmplyeeService {

    @Autowired
    @Qualifier("dw-dao")
    private DWDao dao;

    @Override
    public Object getList(Map<String, Object> info) throws Exception {
        String comp_no = (String) info.get("comp_no");
        String site_no = (String) info.get("site_no");
        if (comp_no == null || comp_no.trim().isEmpty())
            throw new DWArgumentException("comp_no", "comp_no is null !");
        if (site_no == null || site_no.trim().isEmpty())
            throw new DWArgumentException("site_no", "site_no is null !");

        String sql = "select a.emp01 as emp_no,a.emp02 as emp_name,a.user_id,a.count as over_count,a.hour as over_hour,b.count as week_count,b.hour as week_hour,c.count as month_count,c.hour as month_hour  " +
                //获取未结案且逾期的任务笔数及逾期任务的预估工时
                "from (select emp01,emp02,user_id,IFNULL(hour,0) as hour,IFNULL(count,0) as count from d_employee e " +
                "left join (select *,sum(IFNULL(estimate_hour,0)) as hour,count(1) as count from r_repair  " +
                "where close_date is null  " +
                "and case when estimate_hour is null then date_add(start_date, interval 1 day) < now()  " +
                "         else date_add(start_date, interval CONCAT(substring_index(estimate_hour,'.',1),':',substring_index(estimate_hour,'.',-1)*6) hour_minute) < now() end  " +
                "group by assign_to) r on r.assign_to = e.emp01  " +
                "where comp_no = ? and site_no = ? ${tenantsid}) a " +
                //获取未结案且排除逾期的任务之外的近一周内的任务笔数及任务的预估工时
                "LEFT JOIN (select emp01,emp02,user_id,IFNULL(hour,0) as hour,IFNULL(count,0) as count from d_employee e " +
                "left join (select *,sum(IFNULL(estimate_hour,0)) as hour,count(1) as count from r_repair  " +
                "where close_date is null and start_date < date_add(now(), interval 7 day) " +
                "and case when estimate_hour is null then date_add(start_date, interval 1 day) > now()  " +
                "         else date_add(start_date, interval CONCAT(substring_index(estimate_hour,'.',1),':',substring_index(estimate_hour,'.',-1)*6) hour_minute) > now() end  " +
                "group by assign_to) r on r.assign_to = e.emp01  " +
                "where comp_no = ? and site_no = ? ${tenantsid}) b ON b.emp01 = a.emp01  " +
                //获取未结案且排除逾期的任务之外的近一个月内的任务笔数及任务的预估工时
                "LEFT JOIN (select emp01,emp02,user_id,IFNULL(hour,0) as hour,IFNULL(count,0) as count from d_employee e " +
                "left join (select *,sum(IFNULL(estimate_hour,0)) as hour,count(1) as count from r_repair  " +
                "where close_date is null and start_date < date_add(now(), interval 1 month) " +
                "and case when estimate_hour is null then date_add(start_date, interval 1 day) > now()  " +
                "         else date_add(start_date, interval CONCAT(substring_index(estimate_hour,'.',1),':',substring_index(estimate_hour,'.',-1)*6) hour_minute) > now() end  " +
                "group by assign_to) r on r.assign_to = e.emp01  " +
                "where comp_no = ? and site_no = ? ${tenantsid}) c ON c.emp01 = a.emp01";

        List<Map<String,Object>> repairEmployeeList = dao.select(sql,comp_no,site_no,comp_no,site_no,comp_no,site_no);

        return DWServiceResultBuilder.build(true, "获取派工维修员列表成功！", repairEmployeeList);
    }
}
