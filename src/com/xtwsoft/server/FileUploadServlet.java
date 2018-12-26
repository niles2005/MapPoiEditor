package com.xtwsoft.server;

import java.io.File;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.xtwsoft.poieditor.ImagesManager;
import com.xtwsoft.poieditor.utils.Guid;
import com.xtwsoft.poieditor.utils.Utils;

@WebServlet(name = "FileUploadServlet", urlPatterns = { "/upload" }, loadOnStartup = 0)
@MultipartConfig(fileSizeThreshold = 1024 * 1024 * 2, maxFileSize = 1024 * 1024 * 50, maxRequestSize = 1024 * 1024 * 50)
public class FileUploadServlet extends HttpServlet {
	public FileUploadServlet() {
	}

	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		try {
			request.setCharacterEncoding("utf-8");// 指定输入字符集
			ServletOutputStream sos = response.getOutputStream();
			response.setContentType("text/html; charset=UTF-8");

			ServiceReturn ret = new ServiceReturn();
			try {
				work(ret, request);
			} catch (Exception ex) {
				ret.setError(ex.getMessage());
			}
			sos.write(ret.toString().getBytes("UTF-8"));
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void work(ServiceReturn ret, HttpServletRequest request) {
		try {
			String path = request.getParameter("path");
			File filePath = new File(ServerConfig.getInstance().getAppPath(),
					path);
			if (!filePath.exists()) {
				ret.setError("upload path is not exist！");
				return;
			}
			// reduce image
			boolean isReduceImage = "image".equals(request
					.getParameter("reduce"));
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
				File theFile = new File(filePath, newName);
				part.write(theFile.getAbsolutePath());
				arr.add(newName);

				if (isReduceImage) {
					if ("jpg".equals(fileType) || "jpeg".equals(fileType)
							|| "png".equals(fileType) || "gif".equals(fileType)) {
						Utils.reduceImageFile(theFile, fileType, 500);
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
