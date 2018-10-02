package com.xtwsoft.poieditor.services;

import javax.servlet.http.HttpServletRequest;

import com.alibaba.fastjson.JSONObject;
import com.xtwsoft.poieditor.POIManager;
import com.xtwsoft.server.Service;
import com.xtwsoft.server.ServiceReturn;

/**
 * 新增POIGroup
 * @author NieLei
 *
 */
public class CreatePOIGroupService extends Service {
	public CreatePOIGroupService() {
		super("createpoigroup");
	}
	
	public void work(ServiceReturn ret,HttpServletRequest request) {
		String groupName = request.getParameter("groupname");
		JSONObject poiGroup = POIManager.getInstance().createPOIGroup(groupName);
		if(poiGroup != null) {
			ret.setSuccess(poiGroup);
		} else {
			ret.setError("create poiGroup failed!");
		}
	}
}
