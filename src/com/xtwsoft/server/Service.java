package com.xtwsoft.server;

import javax.servlet.http.HttpServletRequest;

public abstract class Service {
	private String m_serviceName = null;

	public Service(String serviceName) {
		m_serviceName = serviceName;
	}

	public String getServiceName() {
		return m_serviceName;
	}

	
	public abstract void work(ServiceReturn ret,HttpServletRequest request);
}
