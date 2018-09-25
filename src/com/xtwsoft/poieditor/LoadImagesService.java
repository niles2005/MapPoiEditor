package com.xtwsoft.poieditor;

import javax.servlet.http.HttpServletRequest;

import com.xtwsoft.server.Service;
import com.xtwsoft.server.ServiceReturn;

/**
 * 返回POI的图片列表
 * @author NieLei
 *
 */
public class LoadImagesService extends Service {
	public LoadImagesService() {
		super("loadimages");
	}
	
	public void work(ServiceReturn ret,HttpServletRequest request) {
		
		ret.setSuccess(POIManager.getInstance().getDatas());
	}
}
