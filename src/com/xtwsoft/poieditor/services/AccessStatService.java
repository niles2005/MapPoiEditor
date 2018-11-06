package com.xtwsoft.poieditor.services;

import javax.servlet.http.HttpServletRequest;

import com.xtwsoft.poieditor.utils.AccessStatManager;
import com.xtwsoft.server.Service;
import com.xtwsoft.server.ServiceReturn;

/**
 * 根据tomcat日志统计到访次数,此仅为统计工作，并不返回数据。
 * 
 * @author NieLei
 *
 */
public class AccessStatService extends Service {
	public AccessStatService() {
		super("accessstat");
	}

	public void work(ServiceReturn ret, HttpServletRequest request) {
		AccessStatManager.getInstance().doStat();
		ret.setSuccess("access stat success!");
	}
}
