package com.xtwsoft.poieditor;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.xtwsoft.poieditor.utils.Guid;

public class POIType {
	private String m_key = null;
	private JSONObject m_typeJson = null;
	private JSONArray m_poisArray = null;
	private POISorter m_sorter = new POISorter();
	
	public POIType(String name) {
		JSONObject json = new JSONObject();
		m_key = "T" + Guid.build16Guid();
		json.put("key", m_key);
		json.put("name", name);
		json.put("pois", m_poisArray);
		m_typeJson = json;
	}


	public POIType(JSONObject typeJson) {
		m_typeJson = typeJson;
		m_key = m_typeJson.getString("key");
		if(m_key == null) {
			m_key = "T" + Guid.build16Guid();
			m_typeJson.put("key", m_key);
		}
		m_poisArray = m_typeJson.getJSONArray("pois");
		if(m_poisArray == null) {
			m_poisArray = new JSONArray();
			m_typeJson.put("pois", m_poisArray);
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
		return m_typeJson;
	}
	
	public boolean addNewPOI(POI poi) {
		if(poi.getPOIType() == this) {
			m_poisArray.add(poi.getJson());
			return true;
		}
		return false;
	}

	public void removePOI(POI poi) {
		if(poi.getPOIType() == this) {
			m_poisArray.remove(poi.getJson());
		}
	}

//	private void initPoi(JSONObject poiJson,JSONObject poiType) {
//		if (poi.hasKey()) {
//			m_poiHash.put(poi.getKey(), poi);
//		}
//	}

	public void sort() {
		this.m_sorter.sortPois(m_poisArray);
	}
	
}