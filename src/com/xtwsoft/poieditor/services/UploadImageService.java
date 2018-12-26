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

//过期，此接口与UploadDetailFilesService合并
public class UploadImageService extends Service {
	public UploadImageService() {
		super("image");
	}

	public void work(ServiceReturn ret, HttpServletRequest request) {
		try {
			String path = request.getParameter("path");
			File imagePath = new File(ServerConfig.getInstance()
					.getImagesPath(), path);
			if (!imagePath.exists()) {
				ret.setError("上传图片目录不存在·！");
				return;
			}
			JSONArray arr = new JSONArray();
			for (Part part : request.getParts()) {
				String newName = Guid.build16Guid();
				String fileName = extractFileName(part).toLowerCase();
				String fileType = "";
				int pos = fileName.lastIndexOf(".");
				if (pos > 0) {
					fileType = fileName.substring(pos + 1);
					newName += fileName.substring(pos);
				}
				File imageFile = new File(imagePath, newName);
				part.write(imageFile.getAbsolutePath());
				arr.add(newName);
				if ("picture".equals(path)) {
					if ("jpg".equals(fileType) || "jpeg".equals(fileType)
							|| "png".equals(fileType) || "gif".equals(fileType)) {
						Utils.reduceImageFile(imageFile, fileType, 200);
					}
				}
			}
			JSONObject imagesObj = ImagesManager.getInstance().resetImagePath(
					path);
			if (imagesObj == null) {
				imagesObj = new JSONObject();
			}
			if (arr.size() == 1) {
				imagesObj.put("image", arr.getString(0));
			} else if (arr.size() > 1) {
				imagesObj.put("image", arr);
			}
			ret.setSuccess(imagesObj);
		} catch (Exception e) {
			e.printStackTrace();
			ret.setError("上传错误:" + e.getMessage());
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