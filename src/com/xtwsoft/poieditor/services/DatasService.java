package com.xtwsoft.poieditor.services;

import javax.servlet.http.HttpServletRequest;

import com.xtwsoft.poieditor.POIManager;
import com.xtwsoft.server.Service;
import com.xtwsoft.server.ServiceReturn;

/**
 * 返回所有的POI数据，JSON格式
 * @author NieLei
 *
 */
public class DatasService extends Service {
	public DatasService() {
		super("datas");
	}
	
	public void work(ServiceReturn ret,HttpServletRequest request) {
		ret.setSuccess(POIManager.getInstance().getDatas());
	}
}
