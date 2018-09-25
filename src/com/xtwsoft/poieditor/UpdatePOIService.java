package com.xtwsoft.poieditor;

import java.io.BufferedReader;

import javax.servlet.http.HttpServletRequest;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.xtwsoft.server.Service;
import com.xtwsoft.server.ServiceReturn;

/**
 * 更新POI对象，此对象为客户端post数据。
 * @author NieLei
 *
 */
public class UpdatePOIService extends Service {
	public UpdatePOIService() {
		super("updatepoi");
	}
	
	public void work(ServiceReturn ret,HttpServletRequest request) {
		try {
			String strContent = getPostContent(request);
			JSONObject json = JSON.parseObject(strContent);			
			if(json != null) {
				String err = POIManager.getInstance().updatePoi(json);
				if(err != null) {
					ret.setError(err);
				} else {//success
					POI poi = POIManager.getInstance().getPOI(json.getString("key"));
					Integer size = poi.getImagesSize();
					if(size != null) {
						JSONObject data = new JSONObject();
						data.put("imagesSize", size);
						ret.setSuccess(data);
					}
				}
			}
		} catch(Exception ex) {
			ex.printStackTrace();
			ret.setError(ex.getMessage());
		}
	}
	
	
	private String getPostContent(HttpServletRequest request) throws Exception {
		BufferedReader reader = request.getReader();
		String str = reader.readLine();
		if(str == null) {
			return null;
		}
        if (str != null && str.startsWith("\uFEFF")) {//remove utf-8 bom
        	str = str.substring(1);
        }
		StringBuilder strBuff = new StringBuilder();
		while(str != null) {
			strBuff.append(str);
			str = reader.readLine();
		}
		return strBuff.toString();
	}
		
}