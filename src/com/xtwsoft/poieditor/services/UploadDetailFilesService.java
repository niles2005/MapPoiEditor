package com.xtwsoft.poieditor.services;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Part;

import com.xtwsoft.poieditor.utils.Guid;
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
			for (Part part : request.getParts()) {
				String newName = Guid.build16Guid();
				String fileName = extractFileName(part).toLowerCase();
				int pos = fileName.lastIndexOf(".");
				String fileType = "";
				if(pos > 0) {
					fileType = fileName.substring(pos + 1);
					newName += fileName.substring(pos);
				}
				File theFile = new File(filePath, newName);
				part.write(theFile.getAbsolutePath());
				
				if("jpg".equals(fileType) || "jpeg".equals(fileType) ||
						"png".equals(fileType) || "gif".equals(fileType)) {
					reduceImageFile(theFile,fileType);
				}
			}
			ret.setSuccess("file upload success!");
		} catch (Exception e) {
			e.printStackTrace();
			ret.setError(e.getMessage());
		}
	}
	
	private void reduceImageFile(File file,String fileType) {
		try {
			//对于大图片，统一缩减为宽为500px的图片
			BufferedImage buffImage = ImageIO.read(file);
			if(buffImage.getWidth() > 500) {
				
				int w = 500;
				double ratio = 1.0 * w / buffImage.getWidth();
				
				int h = (int)(buffImage.getHeight() * ratio + 0.5);
				
				BufferedImage image = new BufferedImage(w, h,
						BufferedImage.TYPE_INT_RGB);
				Graphics2D g = image.createGraphics();
				g.drawImage(buffImage, 0, 0, w, h, null);
				g.dispose();
				
				//统一转成png格式，但还是使用.jpg  jped  .gif等后缀
				ImageIO.write(image, fileType, file);
			}
		} catch(Exception ex) {
			ex.printStackTrace();
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