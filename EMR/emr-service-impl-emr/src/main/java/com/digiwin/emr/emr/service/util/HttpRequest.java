package com.digiwin.emr.emr.service.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

//import sun.net.www.protocol.http.HttpURLConnection;

public class HttpRequest {
	
	/**
     * 向指定URL发送GET方法的请求
     * 
     * @param url,param
     *            发送请求的URL/请求参数，请求参数应该是 name1=value1&name2=value2 的形式。
     * @return URL 所代表远程资源的响应结果
     */
    public static String sendGet(String url ) {
        String result = "";
        BufferedReader in = null;
        String urlNameString = url ;
        try {
            URL realUrl = new URL(urlNameString);
            // 打开和URL之间的连接
            HttpURLConnection httpURLConnection = (HttpURLConnection)realUrl.openConnection();
            // 设置通用的请求属性
            httpURLConnection.setRequestProperty("Accept", "application/json");
            httpURLConnection.setRequestProperty("connection", "Keep-Alive");
            httpURLConnection.setRequestProperty("user-agent",
                    "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            //解决发送数据的乱码
            httpURLConnection.setRequestProperty("content-type", "application/json;charset=UTF-8");
            // 建立实际的连接
            httpURLConnection.connect();
            //判斷http response code 具體值401，如果报错，就返回code，不往下执行 
            if(httpURLConnection.getResponseCode() !=200){
//            	result = String.valueOf(httpURLConnection.getResponseCode());
            	result = "-1";
            	return result;
            }
            // 获取所有响应头字段
            // 定义 BufferedReader输入流来读取URL的响应
            in = new BufferedReader(new InputStreamReader(
            		httpURLConnection.getInputStream(),"UTF-8"));
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }
        } catch (Exception e) {
            result = "-1";
            e.printStackTrace();
        }
        // 使用finally块来关闭输入流
        finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
        return result;
    }

    /**
     * 向指定 URL 发送POST方法的请求
     * 
     * @param url
     *            发送请求的 URL
     * @param param
     *            请求参数，请求参数应该是 name1=value1&name2=value2 的形式。
     * @return 所代表远程资源的响应结果
     * @throws IOException 
     */
    public static String sendPost(String url, String param) {
        PrintWriter out = null;
        BufferedReader in = null;
        String result = "";
        try {
            URL realUrl = new URL(url);
            // 打开和URL之间的连接
            URLConnection conn = realUrl.openConnection();
            HttpURLConnection httpURLConnection = (HttpURLConnection)conn;
            // 设置通用的请求属性
            httpURLConnection.setRequestProperty("accept", "*/*");
            httpURLConnection.setRequestProperty("connection", "Keep-Alive");
            httpURLConnection.setRequestProperty("user-agent",
                    "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            httpURLConnection.setRequestMethod("POST");
            //解决发送数据的乱码
            httpURLConnection.setRequestProperty("content-type", "application/json;charset=UTF-8");
            // 发送POST请求必须设置如下两行
            httpURLConnection.setDoOutput(true);
            httpURLConnection.setDoInput(true);
            httpURLConnection.connect();
            // 获取URLConnection对象对应的输出流
            out = new PrintWriter(
            		new OutputStreamWriter(httpURLConnection.getOutputStream(),"UTF-8"));
            // 发送请求参数
            out.print(param);
            // flush输出流的缓冲
            out.flush();
            //判斷http response code 具體值401，如果报错，就返回code，不往下执行 
            if(httpURLConnection.getResponseCode() != 200){
            	result = String.valueOf(httpURLConnection.getResponseCode());
//            	result = "-1";
            	return result;
            }
            // 定义BufferedReader输入流来读取URL的响应
            in = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream(),"UTF-8"));
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }
            
        } catch (Exception e) {
        	result = "-1";
            e.printStackTrace();
        }
        //使用finally块来关闭输出流、输入流
        finally{
            try{
                if(out!=null){
                    out.close();
                }
                if(in!=null){
                    in.close();
                }
            }
            catch(IOException ex){
                ex.printStackTrace();
            }
        }
        return result;
    }    
      
}
