package com.xtwsoft.poieditor;

import javax.servlet.http.HttpServletRequest;

import com.alibaba.fastjson.JSONObject;
import com.xtwsoft.server.Service;
import com.xtwsoft.server.ServiceReturn;

/**
 * 新增POI
 * @author NieLei
 *
 */
public class CreatePOIService extends Service {
	public CreatePOIService() {
		super("createpoi");
	}
	
	public void work(ServiceReturn ret,HttpServletRequest request) {
		JSONObject poi = POIManager.getInstance().createPOI();
		ret.setSuccess(poi);
	}
}
