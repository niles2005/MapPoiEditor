package com.xtwsoft.poieditor.services;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Part;

import com.alibaba.fastjson.JSONObject;
import com.xtwsoft.poieditor.utils.Guid;
import com.xtwsoft.server.ServerConfig;
import com.xtwsoft.server.Service;
import com.xtwsoft.server.ServiceReturn;

public class UploadZipService extends Service {
	public UploadZipService() {
		super("intro");
	}

	public void work(ServiceReturn ret, HttpServletRequest request) {
		try {
			File introPath = ServerConfig.getInstance().getIntroPath();
			for (Part part : request.getParts()) {
				String fileName = extractFileName(part);
				if (fileName.length() > 0) {
					if (!fileName.toLowerCase().endsWith(".zip")) {
						ret.setError("上传文件仅支持zip文件！");
						return;
					}

					ZipInputStream zin = new ZipInputStream(
							part.getInputStream());
					ZipEntry en;
					boolean hasIndexHtml = false;
					while ((en = zin.getNextEntry()) != null) {
						if (en.isDirectory()) {
						} else {
							if (en.getName().equals("index.html")) {
								hasIndexHtml = true;
								break;
							}
						}
					}
					if (!hasIndexHtml) {
						ret.setError("zip顶层目录不存在index.html文件！");
						return;
					}

					String guid = Guid.build16Guid();
					int pos = fileName.lastIndexOf("\\");
					if (pos != -1) {
						fileName = fileName.substring(pos + 1);
					}

					File zipFile = new File(introPath, guid + ".zip");
					part.write(zipFile.getAbsolutePath());

					File unZipPath = new File(introPath, guid);
					if (!unZipPath.exists()) {
						unZipPath.mkdir();
					}
					String error = unZipFile(zipFile, unZipPath);
					if (error != null) {
						ret.setError(error);
						return;
					}
					JSONObject retData = new JSONObject();
					retData.put("introPage", introPath.getName() + "/" + guid);
					ret.setSuccess(retData);
					// 仅支持一个zip文件
					break;
				}
			}

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

	private String unZipFile(File zipFile, File unzipPath) {
		try {
			byte[] buffer = new byte[1024];
			ZipInputStream zis = new ZipInputStream(
					new FileInputStream(zipFile));
			ZipEntry zipEntry = zis.getNextEntry();
			while (zipEntry != null) {
				String fileName = zipEntry.getName();
				if (zipEntry.isDirectory()) {
					new File(unzipPath, fileName).mkdir();
					zipEntry = zis.getNextEntry();
				} else {
					File newFile = new File(unzipPath, fileName);
					FileOutputStream fos = new FileOutputStream(newFile);
					int len;
					while ((len = zis.read(buffer)) > 0) {
						fos.write(buffer, 0, len);
					}
					fos.flush();
					fos.close();
				}
				zipEntry = zis.getNextEntry();
			}
			zis.closeEntry();
			zis.close();
			return null;
		} catch (Exception ex) {
			ex.printStackTrace();
			return ex.getMessage();
		}

	}
}