package com.xtwsoft.poieditor.services;

import javax.servlet.http.HttpServletRequest;

import com.alibaba.fastjson.JSONObject;
import com.xtwsoft.poieditor.POIManager;
import com.xtwsoft.server.Service;
import com.xtwsoft.server.ServiceReturn;

/**
 * 全部POI的detail处理，用于修改程序后的批量更新。
 * 非强制处理，如果已存在数据，则不处理。
 * 如需强制处理，可先删除处理数据。
 * 
 * /service?name=buildalldetails
 * @author NieLei
 *
 */
public class BuildAllDetailsService extends Service {
	public BuildAllDetailsService() {
		super("buildalldetails");
	}
	
	public void work(ServiceReturn ret,HttpServletRequest request) {
		try {
			JSONObject retJson = POIManager.getInstance().buildAllPoiDetails();
			ret.setSuccess(retJson);
		} catch(Exception ex) {
			ex.printStackTrace();
			ret.setError(ex.getMessage());
		}
	}
	
}