package com.digiwin.emr.emr.service.util;

import com.digiwin.app.service.DWServiceResult;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.json.JSONObject;

import java.util.List;
import java.util.Map;

/**
 * author:lidonga
 * 调用设备中心获取设备基础资料公共方法
 */
public class EquipmentUtil {
    //从设备中心获取设备基础资料
    public static List<Map<String, Object>> callApiForEquipmentByESC(String tenantsid, String comp_no, String site_no, List<String> group_no, List<String> outEqlist, List<String> inEqList, String status) throws Exception {
        JSONObject json = new JSONObject();
        json.put("tenantsid", tenantsid);
        json.put("comp_no", comp_no);
        json.put("site_no", site_no);
        json.put("group_no", group_no);
        json.put("outEqlist", outEqlist);
        json.put("inEqlist", inEqList);
        json.put("status", status);

        DWServiceResult result = (DWServiceResult) HttpClient.get("Esc/Equipment/ExtraList",json);
        if(!result.isSuccess()) {
            throw new Exception(result.geMessage());
        }
        Gson gson = new Gson();
        List<Map<String, Object>> Data = gson.fromJson(result.getData().toString(), new TypeToken<List<Map<String, Object>>>(){}.getType());
        return Data;
    }
}
