package com.xtwsoft.poieditor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Iterator;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.xtwsoft.poieditor.utils.Guid;
import com.xtwsoft.poieditor.utils.Html2JSON;
import com.xtwsoft.poieditor.utils.Utils;
import com.xtwsoft.server.ServerConfig;

public class POI {
	private JSONObject m_json = null;
	private String m_key = null;
	private File m_detailPath = null;
	private POIGroup m_poiGroup = null;
//	private ArrayList<File> m_imageFileList = null;
	private boolean m_isNew = false;
	
	public POI(POIGroup poiGroup) {
		m_poiGroup = poiGroup;
		//为区分POI的key，group加G前缀
		m_key = "P" + Guid.build16Guid();
		JSONObject json = new JSONObject();
		json.put("key", m_key);
		
		//临时标识，表示是地图上新创建，创建时仅加入m_poiHash，
		//补上名称等属性后加入m_poiJsonArray，并删除临时标识
		m_isNew = true;
		m_json = json;
		m_detailPath = new File(ServerConfig.getInstance().getPOISPath(),m_key);
	}
	
	public POI(JSONObject json,POIGroup poiGroup) {
		m_json = json;
		m_poiGroup = poiGroup;
		m_key = json.getString("key");
		m_detailPath = new File(ServerConfig.getInstance().getPOISPath(),m_key);
	}
	
	
	public POIGroup getPOIGroup() {
		return m_poiGroup;
	}
	
	public JSONObject getJson() {
		return m_json;
	}
	
	public boolean hasKey() {
		return m_key != null;
	}
	
	public String getKey() {
		return m_key;
	}
	
	public void remove() {
		m_poiGroup.removePOI(this);
		try {
			Utils.deletePath(m_detailPath);
		} catch(Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public void update(JSONObject json) {
		Iterator iters = json.keySet().iterator();
		while(iters.hasNext()) {
			String k = (String)iters.next();
			if(!k.startsWith("_")) {//避开临时属性，比如_new
				m_json.put(k, json.get(k));
			}
		}
		buildDetail(false);
		String thumbnail = m_json.getString("thumbnail");
		if(thumbnail != null && thumbnail.startsWith("thumbnail")) {
			File thumbnailFile = new File(m_detailPath,thumbnail);
			if(!thumbnailFile.exists()) {
				File imageFile = new File(m_detailPath,thumbnail.substring(9));
				if(imageFile.exists()) {
					Utils.doThumbnail(imageFile,thumbnailFile);
				}
			}
		}
		if (m_isNew) {
			if(this.m_poiGroup.addNewPOI(this)) {
				m_isNew = false;
			}
		}
	}
	
	public void updateDetail(JSONObject json) {
		Iterator iters = json.keySet().iterator();
		while(iters.hasNext()) {
			String strKey = (String)iters.next();
			m_json.put(strKey, json.get(strKey));
		}
		buildDetail(true);
	}
	
	public void buildDetailJson(File detailJsonFile) {
		if((m_key + ".json").equals(detailJsonFile.getName())) {
			m_json.put("detailJson", detailJsonFile.getName());
			String theDetailPath = "datas/p/" + m_key + "/";
			m_json.put("detailPath", theDetailPath);
		}
	}
	
	//限于小程序的访问权限，根据detailUrl处理详情，转存为json格式，用于生成小程序的wxml。
	//每次保存后按 处理后内容产生一个md5sum，用以判断是否已经处理。
	//再次处理后可以先比较此 md5sum，如果一致，则不保存，并不进行下载图片，声音等操作。
	//POI编辑页面点 "下载&处理"时，会强制调用此方法。isForce:true
	//POI编辑页面点 "保存 "时，如果没有m_fileSum,会调用一次，后续保存，在m_fileSum存在时不执行此方法。isForce: false
	protected boolean buildDetail(boolean isForce) {
		try {
			if(!m_detailPath.exists()) {
				m_detailPath.mkdir();
			}
			File destFile = new File(m_detailPath,m_key +".json");

			String strDetailUrl = m_json.getString("detailUrl");
			if(strDetailUrl != null) {
				if(strDetailUrl != null) {
					strDetailUrl = strDetailUrl.trim();
				}
				if(strDetailUrl.startsWith("http")) {
					if(!destFile.exists() || isForce) {
						Html2JSON h2J = new Html2JSON(strDetailUrl,m_detailPath,m_key);
						String theDetailPath = "datas/p/" + m_key + "/";
						m_json.put("detailPath", theDetailPath);
						m_json.put("detailJson", destFile.getName());
						return true;
					}
				}
			}
			if(!destFile.exists()) {
				JSONObject modelJson = null;
				File f = new File(ServerConfig.getInstance().getDatasPath(),"model/model.json");
				if(f.exists()) {
					modelJson = (JSONObject)Utils.loadJSON(f);
				}
				if(modelJson == null) {
					JSONObject json = new JSONObject();
					json.put("contents", new JSONArray());
					json.put("title", "");
					json.put("address", "");
					modelJson = json;
				}
				String name = this.m_json.getString("name");
				if(name != null) {
					modelJson.put("title", name);
				}
				String address = this.m_json.getString("address");
				if(address != null) {
					address = address.trim();
					if(address.length() > 0) {
						modelJson.put("address", address);
					}
				}
				PrintWriter writer = new PrintWriter(new OutputStreamWriter(
						new FileOutputStream(destFile), "UTF-8"));
				
				writer.write(modelJson.toJSONString());
				writer.flush();
				writer.close();
				
				String theDetailPath = "datas/p/" + m_key + "/";
				m_json.put("detailPath", theDetailPath);
				m_json.put("detailJson", destFile.getName());
				return true;
			}
		} catch(Exception ex) {
			ex.printStackTrace();
		}
		return false;
	}
	
}
