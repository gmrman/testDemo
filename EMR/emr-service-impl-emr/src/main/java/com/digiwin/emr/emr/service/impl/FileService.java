package com.digiwin.emr.emr.service.impl;


import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;

import com.digiwin.app.container.exceptions.DWArgumentException;
import com.digiwin.emr.emr.service.IFileService;
import com.digiwin.emr.emr.service.util.Excel;
import org.apache.commons.io.IOUtils;
import sun.misc.BASE64Encoder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.digiwin.app.dao.DWDao;
import com.digiwin.app.service.DWFile;
import com.digiwin.app.service.DWServiceContext;
import com.digiwin.dmc.sdk.entity.FileInfo;
import com.digiwin.dmc.sdk.service.IDocumentStorageService;
import com.digiwin.dmc.sdk.service.impl.DocumentStorageService;

public class FileService implements IFileService {

	private static final SimpleDateFormat formatterDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private static final String MODELPATH = "Path"+File.separator+"document"+File.separator+"model"+File.separator;
	private static final String UPLOADPATH = "Path"+File.separator+"document"+File.separator+"upload"+File.separator;
	@Autowired
	@Qualifier("dw-dao")
	private DWDao dao;

	// 上传文件
	@Override
	public Object postMedia(DWFile file) throws Exception {
		Map<String, Object> map = new HashMap<>();
		Map<String, Object> profile = DWServiceContext.getContext().getProfile();
		Long tenantsid = (Long) profile.get("tenantSid");
		String userid = (String) profile.get("userId");
//		Long tenantsid = new Long(111111);
//		String userid = "gengmr";

		// 获取当前时间
		Date date = new Date();
		SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		SimpleDateFormat sdfd = new SimpleDateFormat("yyyyMMdd");
		SimpleDateFormat sdfm = new SimpleDateFormat("yyyyMM");
		Excel excel = new Excel();
		excel.uploadLocalDocument(UPLOADPATH  +tenantsid + File.separator + userid + File.separator, file);
		String id = excel.getReturnfileId();
		if (id != null) {
			map.put("FileId", id);
		} else {
			map.put("error", "uploaderror1");
		}
		return map;
	}
	// 获取模板文件
	@Override
	public Object getModel(Map<String, Object> info) throws Exception {
//		Excel ex = new Excel();
		BASE64Encoder es = new BASE64Encoder();
		Map<String,String> map = new HashMap<String,String>();

//		String folderid = ex.getFolderId(null, MODELPATH);
//		try {
			// 1.对默认bucket操作
			IDocumentStorageService documentStorageService = DocumentStorageService.instance();

			String fileId = info.get("fileId").toString();
			FileInfo fileInfo = documentStorageService.getDocumentInfo(fileId);

			com.digiwin.dmc.sdk.service.download.IFileService fileService = com.digiwin.dmc.sdk.service.download.FileService
					.fileInstance();
			// 参数：文件id 下载默认存储区文件
			byte[] buffer = fileService.download(fileId);

			map.put("fileName", fileInfo.getFileName());
			map.put("content", es.encode(buffer).replace("\r", "").replace("\n", ""));
//		} catch (Exception e) {
//			throw new Exception("传参错误！");
//		}
		return map;
	}

	@Override
	public byte[] getFile(String fileId) throws Exception {
		Excel ex = new Excel();
		if("".equals(fileId.trim())){
			throw new DWArgumentException("fileId", "fileId is null !");
		}
		IDocumentStorageService documentStorageService = DocumentStorageService.instance();

//		String fileId = info.get("fileId").toString();
		FileInfo fileInfo = documentStorageService.getDocumentInfo(fileId);
		String filename = fileInfo.getFileName();
		com.digiwin.dmc.sdk.service.download.IFileService fileService = com.digiwin.dmc.sdk.service.download.FileService
				.fileInstance();
		// 参数：文件id 下载默认存储区文件
		byte[] audioBytes = fileService.download(fileId);
//		byte[] audioBytes = IOUtils.toByteArray(this.getClass().getResourceAsStream("/test.mp3"));

//	File inputFile = new File(this.getClass()
//	          .getResource("/test.mp3")
//	          .getFile());
//
//        byte[] fileContent = FileUtils.readFileToByteArray(inputFile);
//        byte[] audioBytes = Base64
//          .getDecoder().decode(fileContent);

//		String fileExt = "mp3";
		//音频或附件

		int bytesLength = audioBytes.length;

//		if ("mp3".equals(fileExt) || "wav".equals(fileExt) || "ogg".equals(fileExt)) {
//            String requestRange = request.getHeader("Range");
//            int startRange = 0;
//            if (!StringUtil.isEmpty(requestRange)) {
//                int startRangeIndex = requestRange.indexOf("=");
//                int endRangeIndex = requestRange.indexOf("-");
//                if (startRangeIndex > 0) {
//                    if (endRangeIndex > 0 && endRangeIndex > startRangeIndex) {
//                        startRange = Integer.valueOf(requestRange.substring(startRangeIndex + 1,endRangeIndex).trim());
//                    }
//                }
//            }

			DWServiceContext.getContext().setStandardResult(false);
			Map<String, Object> responseHeader = DWServiceContext.getContext().getResponseHeader();
			responseHeader.put("Content-Type", "" + (bytesLength - 0));
			responseHeader.put("Content-Range", "bytes " + 0 + "-" + (bytesLength - 1) + "/" + bytesLength);

		if (filename.endsWith("mp3") || filename.endsWith("wav") || filename.endsWith("ogg")) {
			responseHeader.put("Content-Type", "audio/mpeg");
		}else{
			responseHeader.put("Content-Type", "image/png");
		}
		responseHeader.put("Content-Disposition", "attachment;fileName="+fileInfo.getFileName());
//			responseHeader.setContentType("multipart/form-data");
//		} else {
//			return new byte[0];
//		}

		return audioBytes;
	}
//	// 获取模板的ids,然后删除该用户之前上传的所有文件
//	@Override
//	public Object getTemplateFileIds(List<Map<String, Object>> info) throws Exception {
//		Excel ex = new Excel();
////		JSONArray ja = new JSONArray(params);
//		Iterator<Map<String, Object>> it = info.iterator();
////		Path/document/model/   用户模板对应存放的路径
//		String modelfolderid = ex.getFolderId(null, MODELPATH);
//		while (it.hasNext()) {
//			Map<String, Object> ob = it.next();
//			String filename = ob.get("fileName").toString().trim();
//			String fileId = ex.getTemplateFileId(modelfolderid, filename);
//			ob.put("fileId", fileId);
//		}
//
//		Calendar cal = Calendar.getInstance();
//		cal.add(Calendar.MONTH, -1);
//		SimpleDateFormat sdfm = new SimpleDateFormat("yyyyMM");
//		// 删除旧文件
////		Map<String, Object> profile = DWServiceContext.getContext().getProfile();
////		Long tenantsid = (Long) profile.get("tenantSid");
////		String userid = (String) profile.get("userId");
//
//		String uploadFolderId = ex.getFolderId(null, UPLOADPATH +sdfm.format(cal.getTime()) + File.separator);
//		ex.deleteFolder(uploadFolderId);
//		return info;
//	}

	// 删除文件
	@Override
	public Object delete(List<Map<String, Object>> info) throws Exception {
		Excel ex = new Excel();
//		JSONArray ja = new JSONArray(params);
		Iterator<Map<String, Object>> it = info.iterator();
//		Path/document/model/   用户模板对应存放的路径
//		String fileid = ex.getFolderId(null, MODELPATH);
		while (it.hasNext()) {
			Map<String,Object> ob = it.next();
			String fileId = ob.get("newFileId").toString().trim();
			if (!fileId.equals("")) {
				ex.deleteDocment(fileId);
			}
		}
		return "Success";
	}

	public static void main(String[] args) {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MONTH, -1);
		SimpleDateFormat sdfm = new SimpleDateFormat("yyyyMM");
		System.out.println(sdfm.format(cal.getTime()));
	}

}
