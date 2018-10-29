package com.xtwsoft.poieditor.utils;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URL;

import javax.imageio.ImageIO;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

public class Utils {
	public static void writeJSON(JSON json,File jsonFile) throws Exception {
		PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(jsonFile),"UTF-8"));
		writer.write(json.toJSONString());
		writer.flush();
		writer.close();
	}
	
	public static JSON loadJSON(File file) throws Exception {
		if(file.exists()) {
			
			StringBuffer strBuff = new StringBuffer();
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file),"utf-8"));
			String line = reader.readLine();
			if (line != null && line.startsWith("\uFEFF")) {//remove utf-8 bom
				line = line.substring(1);
			}
			while(line != null) {
				strBuff.append(line);
				line = reader.readLine();
			}
			reader.close();
			String content = strBuff.toString().trim();
			if(content.startsWith("{")) {
				JSONObject jsonObject = JSON.parseObject(strBuff.toString());
				return jsonObject;
			} else if(content.startsWith("[")) {
				JSONArray jsonArray = JSON.parseArray(strBuff.toString());
				return jsonArray;
			}
		}
		return null;
	}
	
	//图片缩减，返回缩减后图片名。
	public static void doThumbnail(File imageFile,File destFile) {
		try {
			String imageName = imageFile.getName();
			//ImageIO 处理不了webp格式，同时webp格式压缩率比较大，不需要再压缩，所以返回原文件。
			if(imageName.toLowerCase().endsWith(".webp")) {
				try {
					BufferedInputStream bis = new BufferedInputStream(new FileInputStream(imageFile));
					
					byte[] buff = new byte[4096];
					BufferedOutputStream bos = new BufferedOutputStream(
							new FileOutputStream(destFile));
					int num = bis.read(buff);
					while (num > 0) {
						bos.write(buff,0,num);
						num = bis.read(buff);
					}
					bos.flush();
					bos.close();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
				return;
			}
			double ratio = 0.0;
			BufferedImage buffImage = ImageIO.read(imageFile);
			
			double ratioHeight = 100.0 / buffImage.getHeight();
			double ratioWhidth = 100.0 / buffImage.getWidth();
			if (ratioHeight > ratioWhidth) {
				ratio = ratioHeight;
			} else {
				ratio = ratioWhidth;
			}
			
			int w = (int)(buffImage.getWidth() * ratio + 0.5);
			int h = (int)(buffImage.getHeight() * ratio + 0.5);
			
			BufferedImage image = new BufferedImage(w, h,
					BufferedImage.TYPE_INT_RGB);
			Graphics2D g = image.createGraphics();
			g.drawImage(buffImage, 0, 0, w, h, null);
			g.dispose();
			ImageIO.write(image, "png", destFile);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public static void deletePath(File path) {
		if (path == null) {
			return;
		}
		if (!path.exists()) {
			return;
		}
		File[] files = path.listFiles();
		for (int i = 0; i < files.length; i++) {
			if (files[i].isDirectory()) {
				deletePath(files[i]);
			}
			files[i].delete();
		}
		path.delete();
	}
	
}
