package com.xtwsoft.mapPoiEditor;

import java.io.BufferedReader;
import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

@WebServlet(name = "EditorServlet", urlPatterns = { "/service" }, loadOnStartup=1)
public class EditorServlet extends HttpServlet {
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		this.doPage(request, response);
	}
	
	public void init(ServletConfig config) throws ServletException {
        try {
        	ServletContext servletContext = config.getServletContext();
        	String contextPath = servletContext.getContextPath();
        	String realPath = servletContext.getRealPath("");
        	ServerConfig.initInstance(contextPath,realPath);
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
	}
	
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		this.doPage(request, response);
	}
	
	public void doPage(HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		
		request.setCharacterEncoding("UTF-8");
		response.setContentType("text/html; charset=UTF-8");
		
		String retInfo = doWork(request,response);
		
		if(retInfo != null) {
			String callback = request.getParameter("callback");
			if(callback != null) {
				retInfo = callback + "(" + retInfo + ");";
			}
			
			ServletOutputStream sos = response.getOutputStream();
			sos.write(retInfo.getBytes("UTF-8"));
		}
	}
	
	private String doWork(HttpServletRequest request, HttpServletResponse response) {
		try {
			JSONObject retJson = new JSONObject();
			String name = request.getParameter("name");
			try {
				if("update".equals(name)) {
					JSONObject ret = doPoiUpdate(request);
					return ret.toJSONString();
				} else {
					
					JSONObject dataJson = EditorManager.getInstance().doWork(name,request);
					if(dataJson != null) {
						retJson.put("retCode", 0);
						retJson.put("data", dataJson);
					} else {
						retJson.put("retCode", -1);
						retJson.put("message", "Error:return null,unknown error!");
					}
				}
				
			} catch(Exception ex) {
				retJson.put("retCode", -1);
				retJson.put("message", "Error:" + ex.getMessage());
			}
			return retJson.toJSONString();
			
		} catch(Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}
	
	private JSONObject doPoiUpdate(HttpServletRequest request) {
		JSONObject retJson = new JSONObject();
		try {
			String strContent = getPostContent(request);
			JSONObject json = JSON.parseObject(strContent);			
			if(json != null) {
				String err = EditorManager.getInstance().updatePoi(json);
				if(err != null) {
					retJson.put("retCode", -1);
					retJson.put("message", "Error:" + err);
				} else {//success
					retJson.put("retCode", 0);
					retJson.put("message", "update poi success!");
				}
			}
		} catch(Exception ex) {
			ex.printStackTrace();
			retJson.put("retCode", -1);
			retJson.put("message", "Error:" + ex.getMessage());
		}
		return retJson;
	}
	
	
	private String getPostContent(HttpServletRequest request) throws Exception {
		BufferedReader reader = request.getReader();
		String str = reader.readLine();
		if(str == null) {
			return null;
		}
        if (str != null && str.startsWith("\uFEFF")) {//remove utf-8 bom
        	str = str.substring(1);
        }
		StringBuilder strBuff = new StringBuilder();
		while(str != null) {
			strBuff.append(str);
			str = reader.readLine();
		}
		return strBuff.toString();
	}
	


	
//	private String getJson(String pageName) {
//		try {
//			File initJsonFile = new File(ServerConfig.getInstance().getPagesPath(),pageName);
//			if(!initJsonFile.exists()) {
////				System.err.println("-------------------");
////				System.err.println(pageName);
//				return "Json file:" + pageName + " is not exist!";
//			}
//			StringBuffer strBuff = new StringBuffer();
//			try {
//				BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(initJsonFile),"UTF-8"));
//				String line = reader.readLine();
//		        if (line != null && line.startsWith("\uFEFF")) {//remove utf-8 bom
//		        	line = line.substring(1);
//		        }
//				while(line != null) {
//					strBuff.append(line);
//					strBuff.append("\r\n");
//					line = reader.readLine();
//				}
//				reader.close();
//				return strBuff.toString();
//			} catch(Exception ex) {
//				ex.printStackTrace();
//				return ex.getMessage();
//			}
//		} catch(Exception ex) {
//			ex.printStackTrace();
//		}
//		return null;
//	}
//	
//	private String getAllJsons(String strPagePath) {
//		try {
//			File pagePath = new File(ServerConfig.getInstance().getAppPath(),strPagePath);
//			if(pagePath.exists() && pagePath.isDirectory()) {
//				
//			} else {
//				JSONObject json = new JSONObject();
//				json.put("retCode", -1);
//				json.put("message", "path:" + strPagePath + " is not exist!");
//				return json.toJSONString();
//			}
//			
//			JSONArray array = new JSONArray();
//			File[] files = pagePath.listFiles();
//			for(int i=0;i<files.length;i++) {
//				String fileName = files[i].getName();
//				if(files[i].isFile() && fileName.endsWith(".json")) {
//					array.add(fileName);
//				}
//			}
//
//			JSONObject json = new JSONObject();
//			json.put("retCode", 0);
//			json.put("result", array);
//			return json.toJSONString();
//		} catch(Exception ex) {
////			ex.printStackTrace();
//			JSONObject json = new JSONObject();
//			json.put("retCode", -1);
//			json.put("message", "Exception:" + ex.getMessage());
//			return json.toJSONString();
//		}
//	}
//	
//	private String getAllFiles(String strPagePath) {
//		try {
//			File pagePath = new File(ServerConfig.getInstance().getAppPath(),strPagePath);
//			if(pagePath.exists() && pagePath.isDirectory()) {
//				
//			} else {
//				JSONObject json = new JSONObject();
//				json.put("retCode", -1);
//				json.put("message", "path:" + strPagePath + " is not exist!");
//				return json.toJSONString();
//			}
//
//			JSONArray editFiles = ServerConfig.getInstance().getJSONArray("editFiles");
//			HashSet hash = null;
//			if(editFiles != null) {
//				hash = new HashSet();
//				for(int i=0;i<editFiles.size();i++) {
//					hash.add(editFiles.getString(i));
//				}
//			}
//			JSONArray array = new JSONArray();
//			File[] files = pagePath.listFiles();
//			int comp = pagePath.getParentFile().compareTo(ServerConfig.getInstance().getAppPath());
//			if(comp != 0) {//不相等，即为子目录
//				JSONObject fileJson = new JSONObject();
//				fileJson.put("name", "..");
//				fileJson.put("type", "dir");
//				array.add(fileJson);
//			}
//			for(int i=0;i<files.length;i++) {
//				String fileName = files[i].getName();
//				if(files[i].isFile()) {
//					JSONObject fileJson = new JSONObject();
//					fileJson.put("name", fileName);
//					int pos = fileName.lastIndexOf(".");
//					if(pos > 0) {
//						String subType = fileName.substring(pos + 1).toLowerCase();
//						if(hash != null) {
//							if(hash.contains(subType)) {
//								fileJson.put("type", subType);
//								array.add(fileJson);
//							} else {
//								continue;
//							}
//						} else {
//							fileJson.put("type", subType);
//							array.add(fileJson);
//						}
//					}
//				} else if(files[i].isDirectory()) {
//					JSONObject fileJson = new JSONObject();
//					fileJson.put("name", fileName);
//					fileJson.put("type", "dir");
//					array.add(fileJson);
//				}
//			}
//
//			JSONObject json = new JSONObject();
//			json.put("retCode", 0);
//			json.put("result", array);
//			return json.toJSONString();
//		} catch(Exception ex) {
//			JSONObject json = new JSONObject();
//			json.put("retCode", -1);
//			json.put("message", "Exception:" + ex.getMessage());
//			return json.toJSONString();
//		}
//	}
		
	
}
