package com.xtwsoft.poieditor;

import java.util.Iterator;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.xtwsoft.poieditor.utils.Guid;

public class POIGroup {
	private String m_key = null;
	private JSONObject m_groupJson = null;
	private JSONArray m_poisArray = null;
	private POISorter m_sorter = new POISorter();
	
	//临时创建标识，表示是地图上新创建，创建时仅加入m_poiHash，
	//补上名称等属性后加入datas.groups，并删除临时标识
	private boolean m_isNew = false;
	
	public POIGroup(String name) {
		JSONObject json = new JSONObject();
		//为区分POI的key，group加T前缀
		m_key = "T" + Guid.build16Guid();
		json.put("key", m_key);
		json.put("name", name);
		
		//表示新创建，没有加入正式列表
		m_isNew = true;
		
		m_poisArray = new JSONArray();
		json.put("pois", m_poisArray);
		m_groupJson = json;
	}


	public POIGroup(JSONObject groupJson) {
		m_groupJson = groupJson;
		m_key = m_groupJson.getString("key");
		if(m_key == null) {
			//为区分POI的key，Group加T前缀
			m_key = "T" + Guid.build16Guid();
			m_groupJson.put("key", m_key);
		}
		m_poisArray = m_groupJson.getJSONArray("pois");
		if(m_poisArray == null) {
			m_poisArray = new JSONArray();
			m_groupJson.put("pois", m_poisArray);
		} else {
			// 加载数据后先进行一次排序
			m_sorter.sortPois(m_poisArray);
			for (int i = 0; i < m_poisArray.size(); i++) {
				JSONObject json = m_poisArray.getJSONObject(i);
				POI poi = new POI(json,this);
				POIManager.getInstance().storePOI(poi);
			}
		}
	}
	
	public String getKey() {
		return m_key;
	}
	
	public JSONObject getJson() {
		return m_groupJson;
	}
	
	public void update(JSONObject updateGroup) {
		Iterator iters = updateGroup.keySet().iterator();
		while(iters.hasNext()) {
			String str = (String)iters.next();
			if(str.startsWith("_") || str.endsWith("key") || str.endsWith("pois")) {
				
			} else {
				m_groupJson.put(str, updateGroup.get(str));
			}
		}
		Boolean isDelete = updateGroup.getBoolean("_deleted");
		if(isDelete != null && isDelete.booleanValue()) {
			POIManager.getInstance().removePOIGroup(this);
		} else {
			if(this.m_isNew) {
				JSONArray groups = POIManager.getInstance().getDatas().getJSONArray("groups");
				groups.add(this.getJson());
				m_isNew = false;
			}
		}
	}
	
		
	public boolean addNewPOI(POI poi) {
		if(poi.getPOIGroup() == this) {
			m_poisArray.add(poi.getJson());
			return true;
		}
		return false;
	}

	public void removePOI(POI poi) {
		if(poi.getPOIGroup() == this) {
			m_poisArray.remove(poi.getJson());
		}
	}
	
	public void sortPOIs() {
		this.m_sorter.sortPois(m_poisArray);
	}
	
}
