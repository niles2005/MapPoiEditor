package com.xtwsoft.poieditor;

import java.util.ArrayList;
import java.util.Comparator;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

public class POITypesComparator implements Comparator {
	private ArrayList<String> m_updateTypeKeys = new ArrayList<String>();
	public POITypesComparator(JSONArray updateTypes) {
		for(int i=0;i<updateTypes.size();i++) {
			m_updateTypeKeys.add(updateTypes.getJSONObject(i).getString("key"));
		}
	}
	
   public int compare(Object obj1, Object obj2) {
	   int pos1 = m_updateTypeKeys.indexOf(((JSONObject)obj1).getString("key"));
	   if(pos1 < 0) {
		   return 1;
	   }
	   int pos2 = m_updateTypeKeys.indexOf(((JSONObject)obj2).getString("key"));
	   if(pos1 < 0) {
		   return 1;
	   }
	   return pos1 - pos2;
   }
	
}
