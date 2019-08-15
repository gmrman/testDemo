package com.digiwin.emr.emr.service.util;

import java.net.URLEncoder;

import com.digiwin.app.module.DWModuleConfigUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.jboss.logging.Logger;
import org.json.JSONObject;

import com.digiwin.app.dao.DWServiceResultBuilder;
import com.digiwin.app.module.utils.DWModuleResourceUtils;
import com.digiwin.app.service.DWServiceContext;
import com.digiwin.app.service.DWServiceResult;

//import sun.net.www.protocol.http.HttpURLConnection;

public class HttpClient {
	private static Logger log = Logger.getLogger(HttpClient.class);
	private static final int SUCCESS_CODE = 200;

	public static DWServiceResult get(String url,JSONObject param) throws Exception {
		CloseableHttpClient httpClient = null;
		CloseableHttpResponse response = null;
		String esc_url = DWModuleConfigUtils.getCurrentModuleProperty("esc_url");
		log.info("esc_url:"+esc_url);
//		try {

			httpClient = HttpClients.createDefault();
			URIBuilder uriBuilder = new URIBuilder(esc_url+"/restful/service/"+url+"?info="+URLEncoder.encode(param.toString(),"UTF-8"));
			HttpGet get = new HttpGet(uriBuilder.build());
			
			get.setHeader(new BasicHeader("accept", "*/*"));
			get.setHeader(new BasicHeader("connection", "Keep-Alive"));
			get.setHeader(new BasicHeader("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)"));
			get.setHeader(new BasicHeader("content-type", "application/json;charset=UTF-8"));
			String token = DWServiceContext.getContext().getToken();
			get.setHeader(new BasicHeader("token", token));
			response = httpClient.execute(get);
			int statusCode = response.getStatusLine().getStatusCode();
			HttpEntity entity = response.getEntity();
			String result = EntityUtils.toString(entity, "UTF-8");
			JSONObject jsonResult = new JSONObject(result);
			if (SUCCESS_CODE == statusCode) {
				JSONObject jsonResult1 = (JSONObject)jsonResult.get("response");
				return DWServiceResultBuilder.build((boolean)jsonResult1.get("success"),jsonResult1.get("message").toString(),jsonResult1.get("data"));
			} else {
				log.error(jsonResult.get("errorMessage"));
//				return "-1";
				return DWServiceResultBuilder.build(false,jsonResult.get("errorMessage").toString(),"");
			}
//		} catch (Exception e) {
//			log.error(url+":post请求失败！");
////			return "-1";
//			return DWServiceResultBuilder.build(false,url+":get请求失败！","");
//		}
	}
	//将中文字符转成url码
	public static String toUtf8String(String s) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c >= 0 && c <= 255) {
                sb.append(c);
            } else {
                byte[] b;
                try {
                    b = String.valueOf(c).getBytes("utf-8");
                } catch (Exception ex) {
                    System.out.println(ex);
                    b = new byte[0];
                }
                for (int j = 0; j < b.length; j++) {
                    int k = b[j];
                    if (k < 0)
                        k += 256;
                    sb.append("%" + Integer.toHexString(k).toUpperCase());
                }
            }
        }
        return sb.toString();
    }
	//post方法
	public static DWServiceResult post(String url, JSONObject param) throws Exception {
		String esc_url = DWModuleConfigUtils.getCurrentModuleProperty("esc_url");
//		try {
			CloseableHttpClient httpClient = HttpClients.createDefault();
			HttpPost post = new HttpPost(esc_url+"/restful/service/"+url);
			//插入http的请求header
			post.addHeader("accept", "*/*");
			post.addHeader("connection", "Keep-Alive");
			post.addHeader("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
			post.addHeader("content-type", "application/json;charset=UTF-8");
			String token = DWServiceContext.getContext().getToken();
			post.addHeader("token", token);
			JSONObject json = new JSONObject();
			//组传参
			json.put("info", param);
			HttpEntity httpEntity = new StringEntity(json.toString());
			
			post.setEntity(httpEntity);
			//执行请求
			CloseableHttpResponse response = httpClient.execute(post);
			//获取请求的状态码
			int statusCode = response.getStatusLine().getStatusCode();
			HttpEntity entity = response.getEntity();
			//获取返回的整体返参
			String result = EntityUtils.toString(entity, "UTF-8");
			JSONObject jsonResult = new JSONObject(result);
			if (SUCCESS_CODE == statusCode) {
				JSONObject jsonResult1 = (JSONObject)jsonResult.get("response");
//				System.out.println(jsonResult.get("response"));
				return DWServiceResultBuilder.build((boolean)jsonResult1.get("success"),jsonResult1.get("message").toString(),jsonResult1.get("data"));
			} else {
				return DWServiceResultBuilder.build(false,jsonResult.get("errorMessage").toString(),"");
			}
//		} catch (Exception e) {
//			log.error(url+":post请求失败！");
//			return DWServiceResultBuilder.build(false,url+":post请求失败！","");
//		}
	}

	public static void main(String[] args) {
//		JSONObject json = new JSONObject();
//		json.put("lang_type", "zh_CN");
////		json.put("comp_no", "DSC");
////		json.put("site_no", "DSKHC");
////		json.put("eq_id", "");
//		DWServiceResult result = HttpClient.get("http://192.168.9.38:8085/restful/service/DWth/CurrencyInfo/List",json);
////		System.out.println(result.toString());
////		JSONObject json1 = new JSONObject();
////		json1.put("info", json);
////		Object result = HttpClient.post("https://oee-test.apps.digiwincloud.com.cn/restful/service/DWOee/DownTimeCalendar",json);
//		System.out.println(result.geMessage());//获取错误信息
//		System.out.println(result.isSuccess());//是否执行成功
//		System.out.println(result.getData());//获取成功请求的返参
		
		String s = "{\"duration\":723,\"statusDescription\":\"OK\",\"response\":{\"message\":\"\",\"success\":true,\"data\":[]},\"profile\":{},\"uuid\":\"\",\"status\":200}";
		JSONObject jsonResult = new JSONObject(s);
		System.out.println(jsonResult.get("response"));
		JSONObject jsonResulta = (JSONObject)jsonResult.get("response");

		
		
		
		

	}

}
