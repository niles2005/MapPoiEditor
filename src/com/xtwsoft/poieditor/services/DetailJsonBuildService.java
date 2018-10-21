package com.xtwsoft.poieditor.services;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.xtwsoft.poieditor.POI;
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
	private JSONObject m_json = null;
	public DetailJsonBuildService() {
		super("detailjsonbuild");
		m_json = loadModelJson();
		if(m_json == null) {
			JSONObject json = new JSONObject();
			json.put("contents", new JSONArray());
			json.put("title", "");
			m_json = json;
		}
	}
	
	private JSONObject loadModelJson() {
		try {
			File f = new File(ServerConfig.getInstance().getWEBINFPath(),"infoModel.json");
			if(f.exists()) {
				
				StringBuffer strBuff = new StringBuffer();
				BufferedReader reader = new BufferedReader(new FileReader(f));
				String line = reader.readLine();
				if (line != null && line.startsWith("\uFEFF")) {//remove utf-8 bom
					line = line.substring(1);
				}
				while(line != null) {
					strBuff.append(line);
					line = reader.readLine();
				}
				reader.close();
				JSONObject jsonObject = JSON.parseObject(strBuff.toString());
				return jsonObject;
			}
		} catch(Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}

	
	public void work(ServiceReturn ret,HttpServletRequest request) {
		try {
			String strDetailPath = request.getParameter("path");
			File detailPath = new File(ServerConfig.getInstance().getAppPath(),strDetailPath);
			if(!detailPath.exists()) {
				detailPath.mkdir();
			}
			POI poi = POIManager.getInstance().getPOI(detailPath.getName());
			if(poi == null) {
				ret.setError("can not find poi:" + detailPath.getName());
			}
			
			String fileName = detailPath.getName() + ".json";
			File detailJsonFile = new File(detailPath,fileName);
			if(detailJsonFile.exists() && detailJsonFile.isFile()) {
				if(poi.getJson().getString("detailJson") == null) {
					poi.buildDetailJson(detailJsonFile);
					ret.setError("file:" + fileName + " is exist,and bind to POI!");
				} else {
					ret.setError("file:" + fileName + " is exist!");
				}
				return;
			}
			PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(detailJsonFile),"UTF-8"));
			String name = poi.getJson().getString("name");
			if(name == null) {
				name = "";
			}
			JSONObject cloneJson = (JSONObject)this.m_json.clone();
			cloneJson.put("title", name);
			writer.write(cloneJson.toJSONString());
			writer.flush();
			writer.close();
			poi.buildDetailJson(detailJsonFile);
			JSONObject obj = new JSONObject();
			obj.put("name", fileName);
			obj.put("path", strDetailPath);
			ret.setSuccess(obj);
		} catch(Exception ex) {
			ret.setError(ex.getMessage());
		}

		
	}
}
