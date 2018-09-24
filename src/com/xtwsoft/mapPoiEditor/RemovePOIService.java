package com.xtwsoft.mapPoiEditor;

import javax.servlet.http.HttpServletRequest;

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
		boolean success = EditorManager.getInstance().removePOI(key);
		ret.setSuccess("remove POI(" + key + ") " + (success? " success": "failed"));
	}
}
