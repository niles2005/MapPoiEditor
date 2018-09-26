package com.xtwsoft.poieditor;

import java.util.Comparator;
import java.util.Hashtable;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

public class POISorter {
	private Hashtable<String,JSONObject> m_hash = new Hashtable<String,JSONObject>();
	public POISorter() {
	}
	
	public void sortPois(JSONArray poiArray) {
		for(int i=0;i<poiArray.size();i++) {
			JSONObject json = poiArray.getJSONObject(i);
			String address = json.getString("address");
			if(address == null) {
				address = "";
				json.put("address",address);
			}
			JSONObject roadInfo = m_hash.get(address);
			if(roadInfo == null) {
				roadInfo = buildRoadInfo(address);
				m_hash.put(address,roadInfo);
			}
		}
		poiArray.sort(new AddrssComparator());
	}
	
	private JSONObject buildRoadInfo(String address) {
		JSONObject info = new JSONObject();
		info.put("name", "1");
		info.put("num", -1);
		int p1 = address.indexOf("路");
		if(p1 != -1) {
			String roadName = address.substring(0, p1 + 1);
			if(roadName.startsWith("愚园")) {
				roadName = "0" + roadName;//保证愚园路拍在前面
			}
			info.put("name", roadName);
			int p2 = address.indexOf("号",p1 + 1);
			int	p3 = address.indexOf("弄",p1 + 1);
			if(p3 == -1) {
				if(p2 == -1) {
					
				}
			} else if(p2 == -1) {
				p2 = p3;
			} else {
				if(p3 < p2) {
					p2 = p3;
				}
			}
			if(p2 > 0) {
				String num = address.substring(p1 + 1, p2).trim();
				int pos4 = num.indexOf("-");
				if(pos4 != -1) {
					num = num.substring(0, pos4);
				}
				info.put("num", Integer.parseInt(num));
			}
		}
		return info;
	}
	
	class AddrssComparator implements Comparator {
	   public int compare(Object obj1, Object obj2) {
		   String address1 = ((JSONObject) obj1).getString("address");
		   String address2 = ((JSONObject) obj2).getString("address");
	       JSONObject road1 = m_hash.get(address1);
	       JSONObject road2 = m_hash.get(address2);
	       String name1 = road1.getString("name");
	       String name2 = road2.getString("name");
	       int comp = name1.compareTo(name2);
	       if(comp == 0) {
		       Integer num1 = road1.getInteger("num");
		       Integer num2 = road2.getInteger("num");
	    	   comp = num1 - num2;
	    	   if(comp == 0) {
	    		   comp = address1.compareTo(address2);
	    	   }
	       }
		   return comp;
	    }
	}
}
