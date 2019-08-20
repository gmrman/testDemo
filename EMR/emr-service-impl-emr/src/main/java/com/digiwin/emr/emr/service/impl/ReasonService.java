package com.digiwin.emr.emr.service.impl;

import com.digiwin.app.container.exceptions.DWArgumentException;
import com.digiwin.app.dao.DWDao;
import com.digiwin.app.service.DWServiceContext;
import com.digiwin.emr.emr.service.IReasonService;
import com.digiwin.emr.emr.service.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReasonService implements IReasonService {
    @Autowired
    @Qualifier("dw-dao")
    private DWDao dao;

    //@Transactional(propagation = Propagation.REQUIRED, rollbackForClassName = "Exception")
    @Override
    public Object getList(Map<String, Object> info) throws Exception {
        Map<String, Object> result = new HashMap<String, Object>();
        List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();

        // 记录传入参数
        Map<String, Object> profile = DWServiceContext.getContext().getProfile();
        Object tenantsid = profile.get("tenantSid");
        String comp_no = (String) info.get("comp_no");
        String site_no = (String) info.get("site_no");
        String enable_flag = (String) info.get("enable_flag");
        String search_info = (String) info.get("search_info");

        int pageNum = info.get("page_num") == null ? 0 : (int) Double.parseDouble((String) info.get("page_num"));
        int maxPerPage = info.get("max_perpage") == null ? 0
                : (int) Double.parseDouble(info.get("max_perpage").toString());
        int count = 0;

        if (comp_no == null || comp_no.isEmpty())
            throw new DWArgumentException("comp_no", "comp_no is null !");
        if (site_no == null || site_no.isEmpty())
            throw new DWArgumentException("site_no", "site_no is null !");
        if(enable_flag == null)
            throw new DWArgumentException("enable_flag", "enable_flag is null !");
        if (search_info == null)
            throw new DWArgumentException("search_info", "search_info is null !");

        StringUtil.SQL(enable_flag);
        StringUtil.SQL(search_info);

        // 开始
        int startNum = (pageNum - 1) * maxPerPage;

        // 获取总笔数
        String countsql = " select count(1) as num from r_reason " +
                " where tenantsid = ? and comp_no = ? and site_no = ? -${tenantsid} ";

        if (!"".equals(search_info)) {
            countsql += " and ( reason_code like '%" + search_info + "%' or reason_desc like '%" + search_info + "%' ) ";
        }

        if (!"".equals(enable_flag)) {
            countsql += " and enable_flag = '" + enable_flag + "' ";
        }

        List<Map<String, Object>> countList = dao.select(countsql, tenantsid, comp_no, site_no);

        if (countList.size() > 0) {
            count = (int) Double.parseDouble(countList.get(0).get("num").toString());
        }

        // 获取资料数
        String sql = " select reason_sid,reason_code,reason_desc,enable_flag from r_reason " +
                " where tenantsid = ? and comp_no = ? and site_no = ? -${tenantsid}";

        if (!"".equals(search_info)) {
            sql += " and ( reason_code like '%" + search_info + "%' or reason_desc like '%" + search_info + "%' ) ";
        }

        if (!"".equals(enable_flag)) {
            sql += " and enable_flag = '" + enable_flag + "' ";
        }

        if (pageNum != 0 && maxPerPage != 0) {
            sql += " limit " + startNum + "," + maxPerPage;
        }

        data = dao.select(sql, tenantsid, comp_no, site_no);

        result.put("success", true);
        result.put("message", "Success！");
        result.put("data", data);
        result.put("count", count);
        return result;
    }
}
