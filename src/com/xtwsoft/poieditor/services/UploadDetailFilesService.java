package com.xtwsoft.poieditor.services;

import java.io.File;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Part;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.xtwsoft.poieditor.ImagesManager;
import com.xtwsoft.poieditor.utils.Guid;
import com.xtwsoft.poieditor.utils.Utils;
import com.xtwsoft.server.ServerConfig;
import com.xtwsoft.server.Service;
import com.xtwsoft.server.ServiceReturn;

public class UploadDetailFilesService extends Service {
	public UploadDetailFilesService() {
		super("uploaddetailfiles");
	}

	public void work(ServiceReturn ret, HttpServletRequest request) {
		try {
			String path = request.getParameter("path");
			File filePath = new File(ServerConfig.getInstance().getAppPath(),path);
			if(!filePath.exists()) {
				ret.setError("upload path is not exist！");
				return;
			}
			JSONArray arr = new JSONArray();
			for (Part part : request.getParts()) {
				String newName = Guid.build16Guid();
				String fileName = extractFileName(part).toLowerCase();
				String fileType = "";
				int pos = fileName.lastIndexOf(".");
				if(pos > 0) {
					fileType = fileName.substring(pos + 1);
					newName += fileName.substring(pos);
				}
				File theFile = new File(filePath, newName);
				part.write(theFile.getAbsolutePath());
				arr.add(newName);
				
				if("jpg".equals(fileType) || "jpeg".equals(fileType) ||
						"png".equals(fileType) || "gif".equals(fileType)) {
					Utils.reduceImageFile(theFile,fileType,500);
				}
			}
			JSONObject imagesObj = ImagesManager.getInstance().resetImagePath(path);
			if(imagesObj == null) {
				imagesObj = new JSONObject();
			}
			if(arr.size() == 1) {
				imagesObj.put("image", arr.getString(0));
			} else if(arr.size() > 1) {
				imagesObj.put("image", arr);
			}
			ret.setSuccess(imagesObj);
			
			ret.setSuccess("file upload success!");
		} catch (Exception e) {
			e.printStackTrace();
			ret.setError(e.getMessage());
		}
	}
	
	
	private String extractFileName(Part part) {
		String contentDisp = part.getHeader("content-disposition");
		String[] items = contentDisp.split(";");
		for (String s : items) {
			if (s.trim().startsWith("filename")) {
				String fileName = s.substring(s.indexOf("=") + 2,
						s.length() - 1);
				return fileName;
			}
		}
		return "";
	}
	
}