package com.digiwin.emr.emr.service.impl;

import com.digiwin.app.container.exceptions.DWArgumentException;
import com.digiwin.app.dao.DWDao;
import com.digiwin.app.dao.DWServiceResultBuilder;
import com.digiwin.app.service.DWServiceContext;
import com.digiwin.emr.emr.service.IReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.math.BigDecimal;
import java.util.*;

public class ReportService implements IReportService {

    @Autowired
    @Qualifier("dw-dao")
    private DWDao dao;

    @Override
    public Object getAbnormalReason(Map<String, Object> info) throws Exception {

        String comp_no = (String)info.get("comp_no");
        String site_no = (String)info.get("site_no");
        String year = (String)info.get("year"); //年期

        if(comp_no == null || comp_no.isEmpty()) throw new DWArgumentException("comp_no", "comp_no is null !");
        if(site_no == null || site_no.isEmpty()) throw new DWArgumentException("site_no", "site_no is null !");
        if(year == null || year.isEmpty()) throw new DWArgumentException("year", "year is null !");

        //获取tenantsid
        Map<String, Object> profile = DWServiceContext.getContext().getProfile();
        Long tenantsid = (Long) profile.get("tenantSid");

        //根据年期获取原因数据
        //通知单，维修记录单 ， 通知单的时间需要在年期内，如果停止叨的故障原因没有回报，就不计算
        String sql = " select rn.reason_code , rrea.reason_desc , SUM(rrep.work_hour) AS reason_hour "+
                     "   from r_notify rn " +
                     "   left join r_reason  rrea" +
                     "     on rn.tenantsid = rrea.tenantsid " +
                     "    and rn.comp_no = rrea.comp_no " +
                     "    and rn.site_no = rrea.site_no " +
                     "    and rn.reason_code = rrea.reason_code "+
                     "   left join r_report rrep "+
                     "     on rn.notify_sid = rrep.notify_sid " +
                     "    and rn.eq_no = rrep.eq_no "+
                     "  where rn.tenantsid = ? " +
                     "    and rn.comp_no = ? " +
                     "    and rn.site_no = ? " +
                     "    and DATE_FORMAT(rn.notify_date,'%Y') = ? " +
                     "    and rn.reason_code is not null " +
                     "  group by rn.reason_code " +
                     "  -${tenantsid} ";

        List<Map<String,Object>> reasonList = this.dao.select(sql, tenantsid, comp_no, site_no, year);

        //计算原因占比，和total
        double total_count = (double) 0;
        double reason_percent = (double) 0;
        for(Map<String,Object> reasonMap : reasonList){
            total_count += Double.parseDouble(reasonMap.get("reason_hour").toString());
        }

        //使用bigDecimal 计算，防止精度计算不准确
        BigDecimal total = BigDecimal.valueOf(total_count);

        for(Map<String,Object> reasonMap : reasonList){
            BigDecimal reason_hour = BigDecimal.valueOf(Double.parseDouble(reasonMap.get("reason_hour").toString()));
            reason_percent = (reason_hour.divide(total)).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
            reasonMap.put("reason_percent",reason_percent);
        }

        Map<String,Object> resultMap = new HashMap<>();
        resultMap.put("total_count",total_count);
        resultMap.put("abnormal_reason_list",reasonList);

        return DWServiceResultBuilder.build(true, "", resultMap);
    }

    @Override
    public Object getAbnormalMaintain(Map<String, Object> info) throws Exception {

        String comp_no = (String)info.get("comp_no");
        String site_no = (String)info.get("site_no");
        String year = (String)info.get("year"); //年期

        if(comp_no == null || comp_no.isEmpty()) throw new DWArgumentException("comp_no", "comp_no is null !");
        if(site_no == null || site_no.isEmpty()) throw new DWArgumentException("site_no", "site_no is null !");
        if(year == null || year.isEmpty()) throw new DWArgumentException("year", "year is null !");

        //获取tenantsid
        Map<String, Object> profile = DWServiceContext.getContext().getProfile();
        Long tenantsid = (Long) profile.get("tenantSid");

        //異常維修次數:依月份統計異常維修次數
        //停機時數累計:依月份統計異常維修工時
        //邏輯說明:
        //表:故障通知單、維修紀錄單
        //已通知單的時間為基準，統計該維修的總工時，如果總工時為0，不計入次數
        //例如:2019/08發生五次故障維修，五次總工時為10小時

        //获取停机时间
        String hourSql = " select month(rn.notify_date) as month , SUM(IFNULL(rre.work_hour,0)) as stop_hour " +
                           "   from r_notify rn " +
                           "   left join r_report rre " +
                           "     on rre.notify_sid = rn.notify_sid " +
                           "  where rn.tenantsid = ? " +
                           "    and rn.comp_no = ? " +
                           "    and rn.site_no = ? " +
                           "    and DATE_FORMAT(rn.notify_date,'%Y') = ? " +
                           "    group by month(rn.notify_date) " +
                           "  -${tenantsid} " ;

        List<Map<String,Object>> hourList = this.dao.select(hourSql, tenantsid, comp_no, site_no, year);
        //补全月份
        for(int i=1;i<=12;i++){
            if(!hourList.contains(i)){
                Map<String,Object> temp = new HashMap<>();
                temp.put("month",i);
                temp.put("stop_hour",0);
                hourList.add(temp);
            }
        }
        //根据月份排序
        Collections.sort(hourList, new Comparator<Map<String, Object>>() {
            public int compare(Map<String, Object> c1, Map<String, Object> c2) {
                return ( c1.get("month").toString()).compareTo(c2.get("month").toString());
            }
        });

        //获取维修次数
        String timesSql = " select month(rn.notify_date) as month , COUNT(1) as maintain_times " +
                          "   from r_notify rn " +
                          "   left join r_report rre " +
                          "     on rre.notify_sid = rn.notify_sid " +
                          "  where rn.tenantsid = ? " +
                          "    and rn.comp_no = ? " +
                          "    and rn.site_no = ? " +
                          "    and DATE_FORMAT(rn.notify_date,'%Y') = ? " +
                          "    and rre.work_hour <> 0 "+
                          "    group by month(rn.notify_date) " +
                          "  -${tenantsid} " ;

        List<Map<String,Object>> timesList = this.dao.select(timesSql, tenantsid, comp_no, site_no, year);
        //补全月份
        for(int i=1;i<=12;i++){
            if(!timesList.contains(i)){
                Map<String,Object> temp = new HashMap<>();
                temp.put("month",i);
                temp.put("maintain_times",0);
                timesList.add(temp);
            }
        }
        //根据月份排序
        Collections.sort(timesList, new Comparator<Map<String, Object>>() {
            public int compare(Map<String, Object> c1, Map<String, Object> c2) {
                return ( c1.get("month").toString()).compareTo(c2.get("month").toString());
            }
        });

        Map<String,Object> resultMap = new HashMap<>();
        resultMap.put("times",timesList);
        resultMap.put("hours",hourList);
        return DWServiceResultBuilder.build(true, "", resultMap);
    }

    @Override
    public Object getStopHourComparation(Map<String, Object> info) throws Exception {

        String comp_no = (String)info.get("comp_no");
        String site_no = (String)info.get("site_no");
        String year = (String)info.get("year"); //年期

        if(comp_no == null || comp_no.isEmpty()) throw new DWArgumentException("comp_no", "comp_no is null !");
        if(site_no == null || site_no.isEmpty()) throw new DWArgumentException("site_no", "site_no is null !");
        if(year == null || year.isEmpty()) throw new DWArgumentException("year", "year is null !");

        //获取tenantsid
        Map<String, Object> profile = DWServiceContext.getContext().getProfile();
        Long tenantsid = (Long) profile.get("tenantSid");


        //計畫停機時間:年度計畫停機時間總時數
        //實際停機時間:年度實際停機時間總時數
        //邏輯說明:
        //表:維修計畫、派工單、維修紀錄單
        //以計畫類型派工單的開始時間為基準，統計該月份的報工總工時，作為實際停機時間
        //計畫停機時間:維修計畫

        String sql = " select month(rep.start_date) as month, " +
                     "        AVG(IFNULL(rp.stop_hour,0)) AS plan_stop_hour, " +
                     "        SUM(IFNULL(rre.work_hour,0)) AS actual_stop_hour "+
                     "   from r_repair rep " +
                     "   left join r_plan rp " +
                     "     on rep.plan_sid = rp.plan_sid " +
                     "    and DATE_FORMAT(rep.start_date,'%Y%m') = DATE_FORMAT(rp.start_date,'%Y%m') " +
                     "   left join r_repair rre " +
                     "     on rep.plan_sid = rre.plan_sid " +
                     "    and DATE_FORMAT(rep.start_date,'%Y%m') = DATE_FORMAT(rre.finish_date,'%Y%m') " +
                     "  where rp.tenantsid = ? " +
                     "    and rp.comp_no = ? " +
                     "    and rp.site_no = ? " +
                     "    and DATE_FORMAT(rep.start_date,'%Y') = ? " +
                     "    and rep.plan_sid is not null  " +
                     "  group by month(rep.start_date) " +
                     "  -${tenantsid} ";

        List<Map<String,Object>> stopList = this.dao.select(sql, tenantsid, comp_no, site_no, year);

        double total_plan_stop = (double)0;
        double total_actual_stop = (double)0;
        List<Integer> monthList = new ArrayList<>();
        for(Map<String,Object> stopMap : stopList){
            total_plan_stop += Double.parseDouble(stopMap.get("plan_stop_hour").toString());
            total_actual_stop += Double.parseDouble(stopMap.get("actual_stop_hour").toString());
            monthList.add(Integer.parseInt(stopMap.get("month").toString()));
        }

        //补全月份
        int month = 0;
        for(int i=1;i<=12;i++){
            if(!monthList.contains(i)){
                Map<String,Object> temp = new HashMap<>();
                temp.put("month",i);
                temp.put("plan_stop_hour",0);
                temp.put("actual_stop_hour",0);
                stopList.add(temp);
            }
        }
        //根据月份排序
        Collections.sort(stopList, new Comparator<Map<String, Object>>() {
            public int compare(Map<String, Object> c1, Map<String, Object> c2) {
                return ( c1.get("month").toString()).compareTo(c2.get("month").toString());
            }
        });

        Map<String,Object> resultMap = new HashMap<>();
        resultMap.put("total_plan_stop",total_plan_stop);
        resultMap.put("total_actual_stop",total_actual_stop);
        resultMap.put("stop_hour_list",stopList);

        return DWServiceResultBuilder.build(true, "", resultMap);
    }


}