package com.xtwsoft.poieditor.services;

import javax.servlet.http.HttpServletRequest;

import com.alibaba.fastjson.JSONArray;
import com.xtwsoft.poieditor.ImagesManager;
import com.xtwsoft.poieditor.POIManager;
import com.xtwsoft.server.Service;
import com.xtwsoft.server.ServiceReturn;

/**
 * 返回图片数组，JSONArray格式
 * @author NieLei
 *
 */
public class ImagesService extends Service {
	public ImagesService() {
		super("images");
	}
	
	public void work(ServiceReturn ret,HttpServletRequest request) {
		String groupName = request.getParameter("group");
		JSONArray imageArray = ImagesManager.getInstance().getImages(groupName);
		if(imageArray != null) {
			ret.setSuccess(imageArray);
		} else {
			ret.setError("images for " + groupName + " is not found!");
		}
	}
}
