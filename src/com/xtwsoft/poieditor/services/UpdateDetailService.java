package com.xtwsoft.poieditor.services;

import javax.servlet.http.HttpServletRequest;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.xtwsoft.poieditor.POI;
import com.xtwsoft.poieditor.POIManager;
import com.xtwsoft.server.Service;
import com.xtwsoft.server.ServiceReturn;

/**
 * 更新POI的detailUrl，此对象为客户端post数据。
 * 
 * @author NieLei
 *
 */
public class UpdateDetailService extends Service {
	public UpdateDetailService() {
		super("updatedetail");
	}

	public void work(ServiceReturn ret, HttpServletRequest request) {
		try {
			String strContent = getPostContent(request);
			JSONObject json = JSON.parseObject(strContent);
			if (json != null) {
				String err = POIManager.getInstance().updatePoiDetail(json);
				if (err != null) {
					ret.setError(err);
				} else {// success
					POI poi = POIManager.getInstance().getPOI(
							json.getString("key"));
					JSONObject data = new JSONObject();
					data.put("updateVersion",
							poi.getJson().getString("updateVersion"));
					data.put("detailJson", poi.getJson()
							.getString("detailJson"));
					ret.setSuccess(data);
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			ret.setError(ex.getMessage());
		}
	}

}