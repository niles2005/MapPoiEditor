package com.xtwsoft.poieditor.services;

import javax.servlet.http.HttpServletRequest;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.xtwsoft.poieditor.POIManager;
import com.xtwsoft.server.Service;
import com.xtwsoft.server.ServiceReturn;

/**
 * 更新POI对象，此对象为客户端post数据。
 * 
 * @author NieLei
 *
 */
public class UpdateAppService extends Service {
	public UpdateAppService() {
		super("updateapp");
	}

	public void work(ServiceReturn ret, HttpServletRequest request) {
		try {
			String strContent = getPostContent(request);
			JSONObject json = JSON.parseObject(strContent);
			if (json != null) {
				String err = POIManager.getInstance().updateApp(json);
				if (err != null) {
					ret.setError(err);
				} else {// success
					ret.setSuccess("update app info success!");
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			ret.setError(ex.getMessage());
		}
	}

}