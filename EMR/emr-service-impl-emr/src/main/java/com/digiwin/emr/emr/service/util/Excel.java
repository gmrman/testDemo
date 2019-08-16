package com.digiwin.emr.emr.service.util;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

import com.digiwin.app.common.DWApplicationConfigUtils;
import com.digiwin.app.common.DWApplicationPropertiesUtils;
import com.digiwin.app.module.DWModuleConfigUtils;
import com.digiwin.app.module.DWModulePropertiesUtils;
//import org.apache.poi.hssf.usermodel.HSSFWorkbook;
//import org.apache.poi.ss.usermodel.Cell;
//import org.apache.poi.ss.usermodel.Row;
//import org.apache.poi.ss.usermodel.Sheet;
//import org.apache.poi.ss.usermodel.Workbook;
//import org.apache.poi.ss.util.CellReference;
//import org.apache.poi.xssf.usermodel.XSSFWorkbook;
//import org.jboss.logging.Logger;

import com.digiwin.app.service.DWFile;
import com.digiwin.dmc.sdk.config.ServerSetting;
import com.digiwin.dmc.sdk.entity.DirInfo;
import com.digiwin.dmc.sdk.entity.FileInfo;
import com.digiwin.dmc.sdk.service.IDocumentStorageService;
import com.digiwin.dmc.sdk.service.download.FileService;
import com.digiwin.dmc.sdk.service.impl.DocumentStorageService;
import com.digiwin.dmc.sdk.service.upload.IGeneralDocumentUploader;
import com.digiwin.dmc.sdk.service.upload.UploadProgressEventArgs;

public class Excel {
	private static final String EXCEL_XLS = "xls";
	private static final String EXCEL_XLS_UP = "XLS";
	private static final String EXCEL_XLSX = "xlsx";
	private static final String EXCEL_XLSX_UP = "XLSX";
	private static final SimpleDateFormat formatterDate = new SimpleDateFormat("yyyy/MM/dd");
	private static final SimpleDateFormat formatterTime = new SimpleDateFormat("HH:mm");
//	private static Logger log = Logger.getLogger(Excel.class);
	private static IDocumentStorageService documentStorageService;
	private String returnfileId = "";

	public String getReturnfileId() {
		return returnfileId;
	}

	public void setReturnfileId(String returnfileId) {
		this.returnfileId = returnfileId;
	}

	public Excel() {
		// 设置文档中心的基础设置
		String url = DWApplicationConfigUtils.getProperty("dmcUrl");
		String adminname = DWModuleConfigUtils.getCurrentModuleProperty("dmcadminName");
		String adminpwd = DWModuleConfigUtils.getCurrentModuleProperty("dmcadminPwd");
		String name = DWModuleConfigUtils.getCurrentModuleProperty("dmcname");
		String pwd = DWModuleConfigUtils.getCurrentModuleProperty("dmcpwd");
		String BucketName = DWModuleConfigUtils.getCurrentModuleProperty("dmcbucketName");
//		String url = DWModuleResourceUtils.getProperties(Excel.class, "DWEhi.properties").getProperty("file.url");
//		String adminname = DWModuleResourceUtils.getProperties(Excel.class, "DWEhi.properties").getProperty("file.adminname");
//		String adminpwd = DWModuleResourceUtils.getProperties(Excel.class, "DWEhi.properties").getProperty("file.adminpwd");
//		String name = DWModuleResourceUtils.getProperties(Excel.class, "DWEhi.properties").getProperty("file.name");
//		String pwd = DWModuleResourceUtils.getProperties(Excel.class, "DWEhi.properties").getProperty("file.pwd");
//		String BucketName = DWModuleResourceUtils.getProperties(Excel.class, "DWEhi.properties").getProperty("file.pwd");
		ServerSetting.setServiceUrl(url);
		ServerSetting.setIdentityAdminName(adminname);
		ServerSetting.setIdentityAdminPwd(adminpwd);
		ServerSetting.setIdentityName(name);
		ServerSetting.setIdentityPwd(pwd);

//		ServerSetting.setServiceUrl("http://47.100.186.97:31725");
////		ServerSetting.setIdentityAdminName("admin");
////		ServerSetting.setIdentityAdminPwd("docadmin");
////		ServerSetting.setIdentityName("OEE");
////		ServerSetting.setIdentityPwd("OEE");
		ServerSetting.setBucketName(BucketName);
		ServerSetting.initialize();
	}

	// 读取excel，返回List<Map>(Map中key为excel中的名称框如：A1，B5)
//	public List<Map<String, Object>> readExcel(String fileid) throws Exception {
//
//		try {
//			com.digiwin.dmc.sdk.service.download.IFileService fileService = FileService.fileInstance();
//			// 参数：文件id 下载默认存储区文件
//			byte[] buffer = fileService.download(fileid);
//			// 1.对默认bucket操作
//			IDocumentStorageService documentStorageService = DocumentStorageService.instance();
//			// 参数：bucketName,查询文件id
//			FileInfo fileInfo = documentStorageService.getDocumentInfo(fileid);
//			Workbook wb = getWorkbok(buffer, fileInfo);
//
//			List<Map<String, Object>> outerList = new ArrayList<Map<String, Object>>();
//			Sheet sheet = wb.getSheetAt(0);
//			Map<String, Object> typeMap = new HashMap<String, Object>();
//			String value = "";
//			String columnValue = "";
//			for (int i = 0; i <= sheet.getLastRowNum(); i++) {
////				log.info("当前行数：" + i);
//				Map<String, Object> innerMap = new HashMap<String, Object>();
//				Row row = sheet.getRow(i);
//				if (row != null) {
//					boolean flag = false;
//					for (int j = 0; j < sheet.getRow(0).getLastCellNum(); j++) {
//						Cell cell = row.getCell(j);
//						if (cell == null) {
//							Cell cell1 = sheet.getRow(0).getCell(j);
//							innerMap.put(CellReference.convertNumToColString(cell1.getColumnIndex()), " ");
////							log.info(CellReference.convertNumToColString(cell1.getColumnIndex()) + ":  ");
//							continue;
////							try {
////								innerMap.put(CellReference.convertNumToColString(cell.getColumnIndex()), " ");
////							} catch (Exception e) {
////							} finally {
////								continue;
////							}
//						}
//						// 获取当前单元格的格式
////						log.info("type:" + cell.getCellType() + " ");
//						columnValue = CellReference.convertNumToColString(cell.getColumnIndex());
//						if (cell.getCellType() == 1) {
//							// 单元格为字符串型
//							value = cell.getStringCellValue();
//						} else if (cell.getCellType() == 0
//								&& typeMap.get(columnValue).toString().trim().equals("date")) {
//							// 单元格为数值型并且第二行类型为date
//							value = formatterDate.format(cell.getDateCellValue());
//						} else if (cell.getCellType() == 0
//								&& typeMap.get(columnValue).toString().trim().equals("time")) {
//							// 单元格为数值型并且第二行类型为time
//							value = formatterTime.format(cell.getDateCellValue());
//						} else if (cell.getCellType() == 0
//								&& (typeMap.get(columnValue).toString().trim().equals("int"))) {
//							// 单元格为数值型并且第二行类型为int
//							value = Integer.toString((int) cell.getNumericCellValue());
//						} else if (cell.getCellType() == 0
//								&& typeMap.get(columnValue).toString().trim().equals("double")) {
//							// 单元格为数值型并且第二行类型为double
//							value = Double.toString((double) cell.getNumericCellValue());
//						} else {
//							cell.setCellType(Cell.CELL_TYPE_STRING);
//							value = cell.getStringCellValue();
//						}
//						if (!value.toString().trim().equals("")) {
//							flag = true;// 判断一行是否全部为空
//						}
////						log.info(columnValue + ":" + value + "   ");
//						innerMap.put(columnValue, value);
//					}
//					if (!flag) {
//						innerMap.put("null", "第" + (i + 1) + "行为空行;");
//					}
//				} else {// 空行，提示错误！
//					innerMap.put("null", "第" + (i + 1) + "行为空行;");
//				}
//				// 记录第2行的值，用于得到复杂单元格的string格式
//				if (i == 1) {
//					typeMap = innerMap;
//				}
//				outerList.add(innerMap);
//			}
//			return outerList;
//		} catch (Exception e) {
//			throw new Exception(e.getMessage());
//		}
//	}

	// 1.判断模板和上传的文件之间是否一致，2.并且数据没有类型错误问题
//	public String checkData(List<Map<String, Object>> modelList, List<Map<String, Object>> dataList, String lang,
//			String comp_no) {
//		StringBuffer errorStr = new StringBuffer();
//		// 初始化列数数组，存放列的类型及长度
//		// 当前类型有string，int，date
////		String[] colArry = null;
//		// 用于存放列对应的类型，如：A：int，B：select
//		Map<Object, Object> typeMap = new HashMap<Object, Object>();
//		// 用于存放select类型中的选项,如selectA:{10:XX,20:XX},selectB:{}
//		Map<String, Map<String, Object>> selectMap = new HashMap<String, Map<String, Object>>();
//		formatterDate.setLenient(false);
//
//		// 模板第三行:中文说明
//		Map<String, Object> row3Map = modelList.get(2);
//		// 模板第一行:表字段
//		Map<String, Object> row1Map = modelList.get(0);
//
//		loop: for (int i = 0; i < dataList.size(); i++) {
//			Map<String, Object> dataMap = dataList.get(i);
//			if (dataMap.containsKey("null")) {
////				errorStr.append(dataMap.get("null"));
//				continue;
//			}
//			if (i < 3) {
//				//// 先判断模板前3行标题行与数据是否一致
//				Map<String, Object> titleMap = modelList.get(i);
//				for (Object key : titleMap.keySet()) {
//					String cellvalue = titleMap.get(key).toString().trim();
//					if (!dataMap.containsKey(key) || !cellvalue.equals(dataMap.get(key).toString())) {
//						String err = "";
//						if (lang.equals("zh_CN")) {
//							err = "文档与模板不一致；";
//						} else if (lang.equals("zh_TW")) {
//							err = "文檔與模版不一致；";
//						} else {
//							err = "Document is inconsistent with template";
//						}
//						errorStr.append(err);
//						break loop;
//					}
//					if (i == 1) {// excel第二行
//						if (cellvalue.equals("int") || cellvalue.equals("double") || cellvalue.equals("date")
//								|| cellvalue.equals("time")) {
//							typeMap.put(key, cellvalue);
//						} else if (cellvalue.equals("select")) {
//							// 如果是select选项，则获取模板第三行
//
//							// 根据第2行相同的key，获取标题中的选项
//							String titlevalue = row3Map.get(key).toString();
//
//							Map<String, Object> innerselect = new HashMap<String, Object>();
//							String[] strs = titlevalue.split("\\(");
//							String str = strs[1].replaceFirst("\\)", "");
//							strs = str.split("\\/");
//							for (String s : strs) {
//								String[] strs1 = s.split("\\:");
//								innerselect.put(strs1[0].trim(), strs1[1]);
//							}
//							typeMap.put(key, cellvalue);
//							selectMap.put(cellvalue + key, innerselect);
//						} else {
////							log.info("cellvalue:" + cellvalue);
////							log.info("key:" + key);
//							String[] strs = cellvalue.split("\\(");
//							String s = strs[1].replaceFirst("\\)", "");
//							typeMap.put(key, s.trim());
//						}
//					}
//				}
//			} else {
//				// 循环检查传过来的主要数据，1.是否符合类型；2.是否符合长度
//				String errstr = "";
////				log.info(dataMap.toString());
//				for (Object typeKey : typeMap.keySet()) {
//
//					String type = typeMap.get(typeKey).toString();
//					// 判断是否为空：第一行表字段后面没有#，且带*的之中第3行中文说明没有*
//					boolean flag = false;
////					if(!row1Map.get(typeKey).toString().trim().contains("#")) {
////						if(row1Map.get(typeKey).toString().trim().contains("*") &&
////								   row3Map.get(typeKey).toString().trim().contains("*")) {
////
////						}
////					}
//					if (!dataMap.containsKey(typeKey) || dataMap.get(typeKey).toString().trim().equals("")) {
//						flag = true;
////						if (row1Map.get(typeKey).toString().trim().contains("*")
////								&& row1Map.get(typeKey).toString().trim().contains("#")) {
////							flag = false;
////						} else
//						if (row1Map.get(typeKey).toString().trim().contains("#")) {
//							flag = false;
//						}
//						if (flag) {// 该字段不可为空但是为空，则报错
//							String err = "";
//							if (lang.equals("zh_CN")) {
//								err = "单元格空白;";
//							} else if (lang.equals("zh_TW")) {
//								err = "單元格空白;";
//							} else {
//								err = "Cell blank；";
//							}
////							errorStr.append(typeKey.toString() + (i + 1) + err);
//							errstr = errstr + typeKey.toString() + (i + 1) + err;
//						}
//						continue;// 无论报不报错，只要为空都无需检查数据类型
//					}
//
//					// 判断单元格空白结束
//					Object temps = dataMap.get(typeKey);
//					// 判断是否和所选compno一致
//					if ("A".equals(typeKey.toString()) && !comp_no.equals(temps)) {
//						String err = "";
//						if (lang.equals("zh_CN")) {
//							err = "本行公司与所选公司不一致;";
//						} else if (lang.equals("zh_TW")) {
//							err = "本行公司與所選公司不一致;";
//						} else {
//							err = "The company is inconsistent with the selected company；";
//						}
//						errstr = errstr + typeKey.toString() + (i + 1) + err;
//						continue;
//					}
//					switch (type) {
//					case "int":
//						try {
//							Integer.parseInt(temps.toString().trim());
//						} catch (Exception e) {
//							String err = "";
//							if (lang.equals("zh_CN")) {
//								err = "不是数字类型格式;";
//							} else if (lang.equals("zh_TW")) {
//								err = "不是數字類型格式";
//							} else {
//								err = "Not a numeric type format;";
//							}
////							errorStr.append(typeKey.toString() + (i + 1) + err);
//							errstr = errstr + typeKey.toString() + (i + 1) + err;
//						}
//						break;
//					case "double":
//						try {
//							Double.parseDouble(temps.toString().trim());
//						} catch (Exception e) {
//							String err = "";
//							if (lang.equals("zh_CN")) {
//								err = "不是数字类型格式;";
//							} else if (lang.equals("zh_TW")) {
//								err = "不是數字類型格式";
//							} else {
//								err = "Not a numeric type format;";
//							}
////							errorStr.append(typeKey.toString() + (i + 1) + err);
//							errstr = errstr + typeKey.toString() + (i + 1) + err;
//						}
//						break;
//					case "date":
//						try {
//							formatterDate.parse(temps.toString().trim());
//						} catch (Exception e) {
//							String err = "";
//							if (lang.equals("zh_CN")) {
//								err = "不是时间类型格式;";
//							} else if (lang.equals("zh_TW")) {
//								err = "不是時間類型格式；";
//							} else {
//								err = "Not a time type format;";
//							}
////							errorStr.append(typeKey.toString() + (i + 1) + ":'" + temps + "'" + err);
//							errstr = errstr + typeKey.toString() + (i + 1) + ":'" + temps + "'" + err;
//						}
//						break;
//					case "time":
//						try {
//							formatterTime.parse(temps.toString().trim());
//						} catch (Exception e) {
//							String err = "";
//							if (lang.equals("zh_CN")) {
//								err = "不是时间类型格式;";
//							} else if (lang.equals("zh_TW")) {
//								err = "不是時間類型格式；";
//							} else {
//								err = "Not a time type format;";
//							}
////							errorStr.append(typeKey.toString() + (i + 1) + ":'" + temps + "'"+err);
//							errstr = errstr + typeKey.toString() + (i + 1) + ":'" + temps + "'" + err;
//						}
//						break;
//					case "select":
//						String temps1 = type.trim() + typeKey.toString().trim();
//						if (selectMap.containsKey(temps1)) {
//							Map<String, Object> inner = (Map<String, Object>) selectMap.get(temps1);
//							if (!inner.containsKey(temps.toString().trim())) {
//								String err = "";
//								if (lang.equals("zh_CN")) {
//									err = "不在标题的选项中;";
//								} else if (lang.equals("zh_TW")) {
//									err = "不在標題的選項中;";
//								} else {
//									err = "Not in the title option;";
//								}
////								errorStr.append(typeKey.toString() + (i + 1) + ":'" + temps + "'"+err);
//								errstr = errstr + typeKey.toString() + (i + 1) + ":'" + temps + "'" + err;
//							}
//						} else {
////							log.info("解析错误！");
//						}
//						break;
//
//					default:// string类型直接判断长度
//						String s = temps.toString().trim();
//						String err = "";
//						if (lang.equals("zh_CN")) {
//							err = "超出允许的长度;";
//						} else if (lang.equals("zh_TW")) {
//							err = "超出允許的長度;";
//						} else {
//							err = "Exceeded the allowed length;";
//						}
//						try {
//							if (s.length() > Integer.parseInt(type)) {
////								errorStr.append(typeKey.toString() + (i + 1) + ":'" + temps + "'"+err);
//								errstr = errstr + typeKey.toString() + (i + 1) + ":'" + temps + "'" + err;
//							}
//						} catch (Exception e) {
////							errorStr.append(typeKey.toString() + (i + 1) + ":'" + temps + "'"+err);
//							errstr = errstr + typeKey.toString() + (i + 1) + ":'" + temps + "'" + err;
//						}
//						break;
//					}
//				}
//				if (!errstr.trim().equals("")) {
//					errorStr.append(errstr);
//				}
//			}
//		}
//		if (errorStr.toString().trim().equals("")) {
//			return "success";
//		} else {
//			return errorStr.toString();
//		}
//	}

	public void uploadLocalDocument(String path, DWFile file) {
		String uuid = UUID.randomUUID().toString();
		String filepath = (System.getProperty("user.dir") + File.separator + "WEB-INF" + File.separator
				+ "implementation" + File.separator + "DWEhi" + File.separator + uuid).replaceAll("\\\\", "/");

		String folderid = getFolderId(null, path);// 获取文件夹的id，如果没有则方法中会创建此文件夹
		// 设置文件信息
		FileInfo fileInfo = new FileInfo();
		// 选择tmp文件夹
		documentStorageService = DocumentStorageService.instance();
//		List<DirInfo> dirInfos = documentStorageService.listContents(null).getDirInfos();
		fileInfo.setDirectoryId(folderid);
		String fileName = file.getFileName();

//		fileInfo.setExtension(fileName.endsWith(EXCEL_XLS) ? "xls" : "xlsx");

		InputStream in = null;
		try {
			in = file.getInputStream();

			// 设置文件名
			fileInfo.setFileName(fileName);
			// 设置文件描述
			fileInfo.setDescription(fileName);
			fileInfo.setDisplayName(fileName);

			byte[] buffer = new byte[in.available()];
			in.read(buffer);
			// 2.文件上传
			final UploadProgressEventArgs up = new UploadProgressEventArgs();
			IGeneralDocumentUploader generalDocumentUploader = documentStorageService.uploadDocument(buffer, fileInfo);
			// 执行上传线程，直到上传完成结束进程
			generalDocumentUploader.upload().onCompleted(eventArgs -> {
				// 判断文件上传是否完成
				if (eventArgs.getPercentage() == 1) {
					up.setPercentage(1);
					// 3.上传成功，返回上传后的文件Id
					setReturnfileId(eventArgs.getFileId());
//					log.info("文件上传完成，" + "Id为：" + getReturnfileId());
				}
			});
			while (up.getPercentage() != 1) {
				Thread.sleep(500);
			}
//			log.info("returnfileId:" + getReturnfileId());

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (in != null) {
					in.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			File f = new File(filepath);
			deleteFile(f);
		}
	}

	// 获取文件夹id 根据目录取得文档中心相应的id,没有相应的目录则创建
	public String getFolderId(String id, String path) {
		if (path.equals("")) {
			return id;
		}
		documentStorageService = DocumentStorageService.instance();
		// id目录下存在的文件夹List
		List<DirInfo> dirInfos = documentStorageService.listContents(id).getDirInfos();
		String firstfolder = path.substring(0, path.indexOf(File.separator));
		Boolean flag = false;

		for (int i = 0; i < dirInfos.size(); i++) {
			if (dirInfos.get(i).getName().equals(firstfolder)) {
				flag = true;

				return getFolderId(dirInfos.get(i).getId(),
						path.substring(path.indexOf(File.separator) + 1, path.length()));
			}
		}
		// 不存在则新增文件夹
		if (!flag) {
			String newid = documentStorageService.createDirectory(ServerSetting.getBucketName(), firstfolder, id);
			return getFolderId(newid, path.substring(path.indexOf(File.separator) + 1, path.length()));
		}
		return null;
	}

	// 获取当前excel文件的后缀，并根据后缀返回excel包中相应的实体类
//	private static Workbook getWorkbok(byte[] buffer, FileInfo fileinfo) throws IOException {
//		Workbook wb = null;
//		ByteArrayInputStream in = new ByteArrayInputStream(buffer);
//		if (fileinfo.getFileName().endsWith(EXCEL_XLS) || fileinfo.getFileName().endsWith(EXCEL_XLS_UP)) { // Excel&nbsp;2003
//			wb = new HSSFWorkbook(in);
//		} else if (fileinfo.getFileName().endsWith(EXCEL_XLSX) || fileinfo.getFileName().endsWith(EXCEL_XLSX_UP)) { // Excel
//																													// 2007/2010
//			wb = new XSSFWorkbook(in);
//		}
//		return wb;
//	}

	// 获取模板id 根据文档名(模板名)获取文档ID
	public String getTemplateFileId(String folderid, String filename) {
		String fileid = "";
		documentStorageService = DocumentStorageService.instance();

		List<FileInfo> fileInfos = documentStorageService.listContents(folderid).getFileInfos();
		for (int m = 0; m < fileInfos.size(); m++) {
			if (filename.equals(fileInfos.get(m).getFileName())) {
				fileid = fileInfos.get(m).getId();
			}
		}
		if (fileid.equals("")) {
			return null;
		} else {
			return fileid;
		}
	}

	// 根据文件id删除文件
	public void deleteDocment(String fileId) {
		documentStorageService = DocumentStorageService.instance();
		// 参数：被删除文件id
//		FileInfo fileInfo = documentStorageService.getDocumentInfo(fileId);
		documentStorageService.deleteDocument(fileId);
	}

	// 根据文件夹id删除文件夹
	public void deleteFolder(String folderId) {
		documentStorageService = DocumentStorageService.instance();
		// 参数分别为：bucketName,要删除文件夹id
		documentStorageService.deleteDirectory(folderId);
	}

	// 删除验证后的文件
	public boolean deleteFile(File dirFile) {
		// 如果dir对应的文件不存在，则退出
		if (!dirFile.exists()) {
			return false;
		}

		if (dirFile.isFile()) {
			return dirFile.delete();
		} else {

			for (File file : dirFile.listFiles()) {
				deleteFile(file);
			}
		}

		return dirFile.delete();
	}

	public static void main(String[] args) {
		Excel excel = new Excel();
		excel.deleteDocment("335200f3-b261-4a04-bb99-87738ac33db2");
//		List list = excel.readExcel("加工行事历_模板.xlsx");
//		List list1 = excel.readExcel("加工行事历_模板01.xlsx");
//		List list = excel.readExcel("停机原因回报_模板.xlsx");
//		List list1 = excel.readExcel("停机原因回报_模板01.xlsx");
//		System.err.println(excel.checkData(list, list1));
//		System.err.println(excel.getTemplateFileId("设备基础数据_模板_zh_CN.xlsx"));

	}

}
