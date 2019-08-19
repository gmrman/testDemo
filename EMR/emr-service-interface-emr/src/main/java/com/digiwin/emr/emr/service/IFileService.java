package com.digiwin.emr.emr.service;

import java.util.List;
import java.util.Map;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.digiwin.app.dao.DWPagableQueryInfo;
import com.digiwin.app.data.DWDataSet;
import com.digiwin.app.service.AllowAnonymous;
import com.digiwin.app.service.DWFile;
import com.digiwin.app.service.DWService;

public interface IFileService extends DWService {

	/**
	 *获取模板文件
	 */
	@AllowAnonymous
	public Object getModel(Map<String, Object> info) throws Exception;

	/**
	 *获取文件（新方法）
	 */
	@AllowAnonymous
	public Object getFile(String fileId) throws Exception;


//	@AllowAnonymous
	public Object postMedia(DWFile file) throws Exception;
	/**
	 *获取模板文档ID
	 */
//	@AllowAnonymous
//	public Object getTemplateFileIds(List<Map<String, Object>> info) throws Exception;
	//删除文件
	@AllowAnonymous
	public Object delete(List<Map<String, Object>> info) throws Exception;

	
	
}
