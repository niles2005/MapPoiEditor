package com.xtwsoft.poieditor;

import java.util.ArrayList;
import java.util.Comparator;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

public class POIGroupComparator implements Comparator {
	private ArrayList<String> m_updateGroupKeys = new ArrayList<String>();
	public POIGroupComparator(JSONArray updateGroups) {
		for(int i=0;i<updateGroups.size();i++) {
			m_updateGroupKeys.add(updateGroups.getJSONObject(i).getString("key"));
		}
	}
	
   public int compare(Object obj1, Object obj2) {
	   int pos1 = m_updateGroupKeys.indexOf(((JSONObject)obj1).getString("key"));
	   if(pos1 < 0) {
		   return 1;
	   }
	   int pos2 = m_updateGroupKeys.indexOf(((JSONObject)obj2).getString("key"));
	   if(pos1 < 0) {
		   return 1;
	   }
	   return pos1 - pos2;
   }
	
}
