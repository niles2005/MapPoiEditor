package com.xtwsoft.poieditor.services;

import javax.servlet.http.HttpServletRequest;

import com.alibaba.fastjson.JSONObject;
import com.xtwsoft.poieditor.ImagesManager;
import com.xtwsoft.server.Service;
import com.xtwsoft.server.ServiceReturn;

/**
 * 返回图片数组，JSONObject格式，含path，和图名列表
 * @author NieLei
 *
 */
public class ImagesService extends Service {
	public ImagesService() {
		super("images");
	}
	
	public void work(ServiceReturn ret,HttpServletRequest request) {
		String groupName = request.getParameter("type");
		JSONObject imageGroup = ImagesManager.getInstance().getImages(groupName);
		if(imageGroup != null) {
			ret.setSuccess(imageGroup);
		} else {
			ret.setError("images for " + groupName + " is not found!");
		}
	}
}
