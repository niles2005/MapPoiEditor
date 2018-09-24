package com.xtwsoft.poieditor;

import javax.servlet.http.HttpServletRequest;

import com.xtwsoft.server.Service;
import com.xtwsoft.server.ServiceReturn;

/**
 * 返回所有的POI数据，JSON格式
 * @author NieLei
 *
 */
public class StoreDetailService extends Service {
	public StoreDetailService() {
		super("storedetail");
	}
	
	public void work(ServiceReturn ret,HttpServletRequest request) {
		ret.setSuccess(POIManager.getInstance().getDatas());
	}
}
