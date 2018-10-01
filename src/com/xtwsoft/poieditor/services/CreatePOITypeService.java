package com.xtwsoft.poieditor.services;

import javax.servlet.http.HttpServletRequest;

import com.alibaba.fastjson.JSONObject;
import com.xtwsoft.poieditor.POIManager;
import com.xtwsoft.server.Service;
import com.xtwsoft.server.ServiceReturn;

/**
 * 新增POIType
 * @author NieLei
 *
 */
public class CreatePOITypeService extends Service {
	public CreatePOITypeService() {
		super("createpoitype");
	}
	
	public void work(ServiceReturn ret,HttpServletRequest request) {
		String typeName = request.getParameter("typename");
		JSONObject poiType = POIManager.getInstance().createPOIType(typeName);
		if(poiType != null) {
			ret.setSuccess(poiType);
		} else {
			ret.setError("create poiType failed!");
		}
	}
}
