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
	private POIType m_poiType = null;
	private ArrayList<File> m_imageFileList = null;
	
	public POI(POIType poiType) {
		m_poiType = poiType;
		m_key = Guid.build16Guid();
		JSONObject json = new JSONObject();
		json.put("key", m_key);
		
		//临时标识，表示是地图上新创建，创建时仅加入m_poiHash，
		//补上名称等属性后加入m_poiJsonArray，并删除临时标识
		json.put("_new", true);
		m_json = json;
		m_detailPath = new File(ServerConfig.getInstance().getPOISPath(),m_key);
	}
	
	public POI(JSONObject json,POIType poiType) {
		m_json = json;
		m_poiType = poiType;
		m_key = json.getString("key");
		m_detailPath = new File(ServerConfig.getInstance().getPOISPath(),m_key);
	}
	
	public POIType getPOIType() {
		return m_poiType;
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
	private boolean hasNewFlag() {
		return m_json.getBooleanValue("_new");
	}
	
	//移除新创建的标志
	private void removeNewFlag() {
		m_json.remove("_new");
	}
	
	public void remove() {
		m_poiType.removePOI(this);
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
		if (hasNewFlag()) {
			if(this.m_poiType.addNewPOI(this)) {
				removeNewFlag();
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
	
	//限于小程序的访问权限，根据detailUrl处理详情，转存为本地html文件，去除js。
	//每次保存后按 处理后内容产生一个md5sum，用以判断是否已经处理。
	//再次处理后可以先比较此 md5sum，如果一致，则不保存，并不进行下载图片，声音等操作。
	//POI编辑页面点 "下载&处理"时，会强制调用此方法。isForce:true
	//POI编辑页面点 "保存 "时，如果没有m_fileSum,会调用一次，后续保存，在m_fileSum存在时不执行此方法。isForce: false
	private void buildDetail(boolean isForce) {
		try {
			String strDetailUrl = m_json.getString("detailUrl");
			if(strDetailUrl != null) {
				strDetailUrl = strDetailUrl.trim();
			}
			if(!strDetailUrl.startsWith("http")) {
				m_json.put("imagesNum", 0);
				return;
			}
			if(!m_detailPath.exists()) {
				m_detailPath.mkdir();
			}
			if(m_fileSum == null) {
				removeOldFiles();


				//重置imagesNum
				m_json.put("imagesNum", 0);
				
				//版本+1，防止客户端在更新详情后，因图片缓存无法刷新
				Integer ver = (Integer)m_json.get("updateVersion");
				if(ver == null) {
					ver = 1;
				} else {
					ver++;
				}
				SimplifyHtml builder = new SimplifyHtml(strDetailUrl);
				File destFile = new File(m_detailPath,m_key +".html");
				m_imageFileList = builder.storeImages(m_detailPath,m_key);
				m_fileSum = builder.store(destFile);
				m_json.put("imagesNum", builder.getImagesNum());
				m_json.put("updateVersion", ver);
			} else {
				if(isForce) {
					SimplifyHtml builder = new SimplifyHtml(strDetailUrl);
					String fileSum = builder.buildMD5Sum();
					if(!m_fileSum.equals(fileSum)) {//重新执行校验不等，重新下载图片等数据
						//删除老的文件及图片
						removeOldFiles();

						m_json.put("imagesNum", 0);
						
						//版本+1，防止客户端在更新详情后，因图片缓存无法刷新
						Integer ver = (Integer)m_json.get("updateVersion");
						if(ver == null) {
							ver = 1;
						} else {
							ver++;
						}
						File destFile = new File(m_detailPath,m_key + ".html");
						builder.store(destFile);
						m_imageFileList = builder.storeImages(m_detailPath,m_key);
						m_fileSum = fileSum;
						m_json.put("imagesNum", builder.getImagesNum());
						m_json.put("updateVersion", ver);
					}
				}
			}
		} catch(Exception ex) {
			ex.printStackTrace();
		}
	}
	
	//更新版本前先删除老的html和图片
	private void removeOldFiles() {
		Integer ver = (Integer)m_json.get("updateVersion");
		String verPart = "";
		if(ver != null) {
			verPart = "_" + ver;
		}
		
		File oldFile = new File(m_detailPath,m_key + verPart +".html");
		if(oldFile.exists()) {
			oldFile.delete();
		}
		Integer imagesNum = m_json.getInteger("imagesNum");
		if(imagesNum != null) {
			for(int i=0;i<imagesNum;i++) {
				File imageFile = new File(m_detailPath, m_key + verPart + "_" + i);
				if(imageFile.exists()) {
					imageFile.delete();
				}
			}
		}
	}
	
	public Integer getImagesNum() {
		return m_json.getInteger("imagesNum");
	}
	
	public Integer getUpdateVersion() {
		return m_json.getInteger("updateVersion");
	}
}
