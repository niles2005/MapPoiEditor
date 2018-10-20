package com.xtwsoft.poieditor.services;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;

import com.alibaba.fastjson.JSON;
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
public class DetailJsonBuildService extends Service {
	public DetailJsonBuildService() {
		super("detailjsonbuild");
	}
	
	public void work(ServiceReturn ret,HttpServletRequest request) {
		try {
			String strDetailPath = request.getParameter("path");
			File detailPath = new File(ServerConfig.getInstance().getAppPath(),strDetailPath);
			if(!detailPath.exists()) {
				detailPath.mkdir();
			}
			String fileName = detailPath.getName() + ".json";
			File detailJsonFile = new File(detailPath,fileName);
			if(detailJsonFile.exists() && detailJsonFile.isFile()) {
				ret.setError("file:" + fileName + " is exist!");
				return;
			}
			PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(detailJsonFile),"UTF-8"));
			writer.write("{}");
			writer.flush();
			writer.close();
			JSONObject obj = new JSONObject();
			obj.put("name", fileName);
			obj.put("path", strDetailPath);
			ret.setSuccess(obj);
		} catch(Exception ex) {
			ret.setError(ex.getMessage());
		}

		
	}
}
