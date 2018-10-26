package com.xtwsoft.poieditor.services;

import java.io.File;

import javax.servlet.http.HttpServletRequest;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.xtwsoft.poieditor.POIManager;
import com.xtwsoft.server.ServerConfig;
import com.xtwsoft.server.Service;
import com.xtwsoft.server.ServiceReturn;

/**
 * 获取detail(包括intro)目录下特定类型的文件，为安全起见，不允许访问datas目录以外文件
 * @author NieLei
 *
 */
public class PathFilesService extends Service {
	public PathFilesService() {
		super("pathfiles");
	}
	
	public void work(ServiceReturn ret,HttpServletRequest request) {
		String strDetailPath = request.getParameter("path");
		String strType = request.getParameter("type");
		if(strDetailPath == null || strDetailPath.indexOf("..") != -1) {
			ret.setError("path:" + strDetailPath + " is not valid!");
			return;
		}
		if(!strDetailPath.startsWith("datas")) {
			ret.setError("path:" + strDetailPath + " is not valid!");
			return;
		}
		File detailPath = new File(ServerConfig.getInstance().getAppPath(),strDetailPath);
		if(!detailPath.exists() || !detailPath.isDirectory()) {
			ret.setError("path:" + strDetailPath + " is not exist!");
			return;
		}
		
		JSONArray array = new JSONArray();
		File[] files = detailPath.listFiles();
		
		if("image".equals(strType)) {
			for(int i=0;i<files.length;i++) {
				String fileName = files[i].getName();
				if(files[i].isFile()) {
					if(fileName.startsWith("thumbnail")) {
						
					} else if(fileName.endsWith(".png") || fileName.endsWith(".jpg") ||
							fileName.endsWith(".jpeg") || fileName.endsWith(".webp") || fileName.endsWith(".gif")) {
						array.add(fileName);
					}
				}
			}
		} else if("audio".equals(strType)) {
			for(int i=0;i<files.length;i++) {
				String fileName = files[i].getName();
				if(files[i].isFile()) {
					if(fileName.endsWith(".mp3")) {
						array.add(fileName);
					}
				}
			}
		} else if("video".equals(strType)) {
			for(int i=0;i<files.length;i++) {
				String fileName = files[i].getName();
				if(files[i].isFile()) {
					if(fileName.endsWith(".mp4")) {
						array.add(fileName);
					}
				}
			}
		}
		ret.setSuccess(array);
	}
}
