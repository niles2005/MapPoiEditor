package com.xtwsoft.poieditor;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

import com.alibaba.fastjson.JSONObject;
import com.xtwsoft.poieditor.utils.Guid;
import com.xtwsoft.poieditor.utils.SimplifyHtml;
import com.xtwsoft.server.ServerConfig;

public class POI {
	private JSONObject m_json = null;
	private String m_key = null;
	private File m_detailPath = null;
	private String m_fileSum = null;
	private ArrayList<File> m_imageFileList = null;
	
	public POI() {
		m_key = Guid.build16Guid();
		JSONObject json = new JSONObject();
		json.put("key", m_key);
		
		//临时标识，表示是地图上新创建，创建时仅加入m_poiHash，
		//补上名称等属性后加入m_poiJsonArray，并删除临时标识
		json.put("_new", true);
		m_json = json;
		m_detailPath = new File(ServerConfig.getInstance().getPOISPath(),m_key);
	}
	
	public POI(JSONObject json) {
		m_json = json;
		m_key = json.getString("key");
		m_detailPath = new File(ServerConfig.getInstance().getPOISPath(),m_key);
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
	
	//创建后，只有key，还没有其他属性，还没有加入正式数组
	public boolean hasNewFlag() {
		return m_json.getBooleanValue("_new");
	}
	
	public void removeNewFlag() {
		m_json.remove("_new");
	}
	
	public void update(JSONObject json) {
		Iterator iters = json.keySet().iterator();
		while(iters.hasNext()) {
			String strKey = (String)iters.next();
			m_json.put(strKey, json.get(strKey));
		}
		buildDetail(false);
	}
	
	public void updateDetail(JSONObject json) {
		Iterator iters = json.keySet().iterator();
		while(iters.hasNext()) {
			String strKey = (String)iters.next();
			m_json.put(strKey, json.get(strKey));
		}
		buildDetail(true);
	}
	
	//限于小程序的访问权限，根据detailUrl处理详情，转存为本地html文件，去除js。
	//每次保存后按 处理后内容产生一个md5sum，用以判断是否已经处理。
	//再次处理后可以先比较此 md5sum，如果一致，则不保存，并不进行下载图片，声音等操作。
	//POI编辑页面点 "下载&处理"时，会强制调用此方法。isForce:true
	//POI编辑页面点 "保存 "时，如果没有m_fileSum,会调用一次，后续保存，在m_fileSum存在时不执行此方法。isForce: false
	private void buildDetail(boolean isForce) {
		try {
			if(!m_detailPath.exists()) {
				m_detailPath.mkdir();
			}
			if(m_fileSum == null) {
				String strDetailUrl = m_json.getString("detailUrl");
				if(strDetailUrl != null) {
					SimplifyHtml builder = new SimplifyHtml(strDetailUrl);
					m_json.put("imagesNum", builder.getImagesNum());
					File destFile = new File(m_detailPath,m_key + ".html");
					m_imageFileList = builder.storeImages(m_detailPath,m_key);
					m_fileSum = builder.store(destFile);
				}
			} else {
				if(isForce) {
					String strDetailUrl = m_json.getString("detailUrl");
					if(strDetailUrl != null) {
						SimplifyHtml builder = new SimplifyHtml(strDetailUrl);
						String wxmlSum = builder.buildMD5Sum();
						if(!m_fileSum.equals(wxmlSum)) {//重新执行校验不等，重新下载图片等数据
							m_json.put("imagesNum", builder.getImagesNum());
							File destFile = new File(m_detailPath,m_key + ".html");
							builder.store(destFile);
							m_imageFileList = builder.storeImages(m_detailPath,m_key);
							m_fileSum = wxmlSum;
						}
					}
				}
			}
		} catch(Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public Integer getImagesNum() {
		return m_json.getInteger("imagesNum");
	}
	
}
