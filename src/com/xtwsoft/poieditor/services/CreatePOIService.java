package com.xtwsoft.poieditor.services;

import javax.servlet.http.HttpServletRequest;

import com.alibaba.fastjson.JSONObject;
import com.xtwsoft.poieditor.POIManager;
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
		String groupKey = request.getParameter("groupkey");
		JSONObject poi = POIManager.getInstance().createPOI(groupKey);
		if(poi != null) {
			ret.setSuccess(poi);
		} else {
			ret.setError("create poi failed!");
		}
	}
}
