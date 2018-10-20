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
 * 获取detail(包括intro)目录下所有的文件，为安全起见，不允许访问datas目录以外文件
 * @author NieLei
 *
 */
public class DetailFilesService extends Service {
	public DetailFilesService() {
		super("detailfiles");
	}
	
	public void work(ServiceReturn ret,HttpServletRequest request) {
		String strDetailPath = request.getParameter("path");
		File detailPath = new File(ServerConfig.getInstance().getAppPath(),strDetailPath);
		if(!detailPath.exists()) {
			detailPath.mkdir();
		}
//		if(detailPath.exists() && detailPath.isDirectory()) {
//			
//		} else {
//			ret.setError("path:" + strDetailPath + " is not exist!");
//			return;
//		}
		
//		int comp = detailPath.compareTo(ServerConfig.getInstance().getDatasPath());
//		System.err.println("detail path:" + comp);
//		if(comp <= 0) {//相等，为比较目录   <0,为父目录
//			ret.setError("path:" + strDetailPath + " is not valid!");
//			return;
//		} else {// >0,为子目录
//			
//		}
		
		if(detailPath.getParentFile().getParentFile().compareTo(ServerConfig.getInstance().getDatasPath()) == 0 || 
				detailPath.getParentFile().compareTo(ServerConfig.getInstance().getDatasPath()) == 0 ) {
			
		} else {
			ret.setError("path:" + strDetailPath + " is not valid!");
			return;
		}
		
		JSONArray array = new JSONArray();
		File[] files = detailPath.listFiles();
		
		for(int i=0;i<files.length;i++) {
			String fileName = files[i].getName();
			if(files[i].isFile()) {
				array.add(fileName);
			}
		}
		ret.setSuccess(array);
	}
}
