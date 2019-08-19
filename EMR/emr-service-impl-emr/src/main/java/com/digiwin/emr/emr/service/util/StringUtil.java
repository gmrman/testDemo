package com.digiwin.emr.emr.service.util;

import java.util.HashMap;
import java.util.Map;

import com.digiwin.app.container.exceptions.DWArgumentException;

public class StringUtil {
	
	public static  String SQL(String str) throws Exception {
		
		String Strnew = str.replaceAll(" ", "").toLowerCase();
		if(Strnew.contains("deletefrom") ||
			Strnew.contains("droptable") ||
			Strnew.contains("truncatetable")) {
			throw new Exception("有sql注入危险:"+str);
		}
		return  str;
	}

	
	public static void main(String[] args) throws Exception {
		Map<String,String> info=new HashMap<String,String>();

		String comp_no = (String)info.get("comp_no");

		if (comp_no == null || comp_no.isEmpty()) throw new DWArgumentException("comp_no", "comp_no is null !");
		StringUtil.SQL(comp_no);
	}
	
}
