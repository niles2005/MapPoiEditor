package com.xtwsoft.poieditor.services;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.xtwsoft.server.Service;
import com.xtwsoft.server.ServiceReturn;
import com.xtwsoft.server.UsersManager;

/**
 * 根据tomcat日志统计到访次数,此仅为统计工作，并不返回数据。
 * 
 * @author NieLei
 *
 */
public class UserAddService extends Service {
	public UserAddService() {
		super("useradd");
	}

	public void work(ServiceReturn ret, HttpServletRequest request) {
		try {
			String strContent = getPostContent(request);
			JSONObject json = JSON.parseObject(strContent);
			if (json != null) {
				HttpSession session = request.getSession(false);
				if (session == null) {
					ret.setError("加入用户失败 !");
					return;
				}
				String user = json.getString("user").trim();
				String pass = json.getString("pass").trim();
				String addKey = json.getString("key").trim();
				if (user.length() > 0 && pass.length() > 0) {
					String account = (String) session.getAttribute("account");
					boolean result = UsersManager.getInstance()
							.addUserPassword(user, pass, account, addKey);
					if (result) {
						ret.setSuccess("加入用户成功!");
					} else {
						ret.setError("加入用户失败!");
					}
				} else {
					ret.setSuccess("用户名或密码为空!");
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			ret.setError(ex.getMessage());
		}

	}
}
