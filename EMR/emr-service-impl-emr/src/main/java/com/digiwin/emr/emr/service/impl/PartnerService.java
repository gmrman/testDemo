package com.digiwin.emr.emr.service.impl;

import com.digiwin.app.container.exceptions.DWArgumentException;
import com.digiwin.app.dao.DWDao;
import com.digiwin.app.dao.DWServiceResultBuilder;
import com.digiwin.emr.emr.service.IPartnerService;
import com.digiwin.emr.emr.service.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PartnerService implements IPartnerService {
    @Autowired
    @Qualifier("dw-dao")
    private DWDao dao;

    @Override
    public Object getList(Map<String, Object> info) throws Exception {
        Map<String,Object> result = new HashMap<String, Object>();

        String comp_no = (String) info.get("comp_no");//公司别
        String site_no = (String) info.get("site_no");//厂别
        String page_number = (String) info.get("page_number");//第几页
        String page_size = (String) info.get("page_size");//一页多少条数据
        String flag = (String) info.get("flag");//一页多少条数据
        String partner_name = (String) info.get("partner_name");//企业名称模糊查询

        if (comp_no == null || comp_no.trim().isEmpty())
            throw new DWArgumentException("comp_no", "comp_no is null !");
        if (site_no == null || site_no.trim().isEmpty())
            throw new DWArgumentException("site_no", "site_no is null !");
        if (page_number == null)
            throw new DWArgumentException("page_number", "page_number is null !");
        if (page_size == null)
            throw new DWArgumentException("page_size", "page_size is null !");
        if (flag == null)
            throw new DWArgumentException("flag", "flag is null !");
        if (partner_name == null)
            throw new DWArgumentException("partner_name", "partner_name is null !");

        // 有SQL注入风险的字段进行检验
        StringUtil.SQL(comp_no);
        StringUtil.SQL(site_no);
        StringUtil.SQL(page_number);
        StringUtil.SQL(page_size);
        StringUtil.SQL(flag);
        StringUtil.SQL(partner_name);
        String total ="0";//总共条数
        StringBuffer sql = new StringBuffer("SELECT partner_no,partner_name, partner_role, contact, email, " +
                "telephone, address,partner_tenantsid,partner_comp_no,partner_site_no" +
                " FROM p_partner WHERE comp_no=? AND site_no=? ${tenantsid}");

        //页数和一页显示多少条为空字串时，则查询全部
        if (!"".equals(page_number.trim()) && !"".equals(page_size.trim())) {//web使用
            //获取选中设备的维修项目列表
            int pageNumber = Integer.parseInt(page_number);
            int pageSize = Integer.parseInt(page_size);
            String coutsql = "SELECT count(1) AS count ROM p_partner WHERE comp_no=? AND site_no=? ${tenantsid}";
            if(!"".equals(partner_name.trim())){
                sql.append(" AND partner_name LIKE '%"+partner_name+"%'");
                coutsql = coutsql+" AND partner_name LIKE '%"+partner_name+"%'";
            }
            sql.append(" limit "+(pageNumber-1)*pageSize+","+pageSize);

            List<Map<String,Object>> countList = dao.select(coutsql,comp_no,site_no);
            total = countList.get(0).get("count").toString();
        }else{//app使用
            //查询启用的伙伴关系
            sql.append(" AND enable_flag='Y'");
        }
        List<Map<String,Object>> dataList = this.dao.select(sql.toString(),comp_no,site_no);
        result.put("list", dataList);
        result.put("total", total);
        return DWServiceResultBuilder.build(true,"获取伙伴关系列表", result);
    }
}
