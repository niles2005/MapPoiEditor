package com.xtwsoft.server;

import java.io.BufferedReader;

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
	
	protected String getPostContent(HttpServletRequest request) throws Exception {
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
