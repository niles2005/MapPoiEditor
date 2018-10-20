package com.xtwsoft.poieditor.services;

import java.io.BufferedReader;

import javax.servlet.http.HttpServletRequest;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.xtwsoft.poieditor.POI;
import com.xtwsoft.poieditor.POIManager;
import com.xtwsoft.server.Service;
import com.xtwsoft.server.ServiceReturn;

/**
 * 更新POI对象，此对象为客户端post数据。
 * @author NieLei
 *
 */
public class UpdatePOIService extends Service {
	public UpdatePOIService() {
		super("updatepoi");
	}
	
	public void work(ServiceReturn ret,HttpServletRequest request) {
		try {
			String strContent = getPostContent(request);
			JSONObject json = JSON.parseObject(strContent);			
			if(json != null) {
				String err = POIManager.getInstance().updatePoi(json);
				if(err != null) {
					ret.setError(err);
				} else {//success
					POI poi = POIManager.getInstance().getPOI(json.getString("key"));
					ret.setSuccess(poi.getJson());
				}
			}
		} catch(Exception ex) {
			ex.printStackTrace();
			ret.setError(ex.getMessage());
		}
	}
}