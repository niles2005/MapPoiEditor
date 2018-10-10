package com.xtwsoft.poieditor.services;

import javax.servlet.http.HttpServletRequest;

import com.xtwsoft.poieditor.POIManager;
import com.xtwsoft.server.Service;
import com.xtwsoft.server.ServiceReturn;

/**
 * 移除特定的POI
 * @author NieLei
 *
 */
public class RemovePOIService extends Service {
	public RemovePOIService() {
		super("removepoi");
	}
	
	public void work(ServiceReturn ret,HttpServletRequest request) {
		String key = request.getParameter("key");
		boolean success = POIManager.getInstance().removePOI(key);
		if(success) {
			ret.setSuccess("remove POI(" + key + ") " + (success? " success": "failed"));
		} else {
			ret.setError("POI(" + key + ") is not exist!");
		}
	}
}
