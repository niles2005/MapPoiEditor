package com.xtwsoft.poieditor;

import java.util.Comparator;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

public class POITypesComparator implements Comparator {
	private JSONArray m_typesKeyArray;
	public POITypesComparator(JSONArray typesKeyArray) {
		m_typesKeyArray = typesKeyArray;
	}
	
   public int compare(Object obj1, Object obj2) {
	   int pos1 = m_typesKeyArray.indexOf(((JSONObject)obj1).getString("key"));
	   if(pos1 < 0) {
		   return 1;
	   }
	   int pos2 = m_typesKeyArray.indexOf(((JSONObject)obj2).getString("key"));
	   if(pos1 < 0) {
		   return 1;
	   }
	   return pos1 - pos2;
   }
	
}
