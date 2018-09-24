package com.xtwsoft.server;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 
 *	标准化的后台服务处理，通过注册的方式加入服务，新的项目只要实现需要的服务模块。
 * @author Nielei
 *
 */
public class ServiceManager {
	private Map<String , Service> m_serviceMap = new HashMap<String , Service>();
	private static ServiceManager m_instance = null;

	public static ServiceManager getInstance() {
		return m_instance;
	}

	public static void initInstance() {
		if(m_instance == null) {
			m_instance = new ServiceManager();
		}
	}
	
	private ServiceManager() {
	}
	
	public void addService(Service service) {
		String serviceName = service.getServiceName();
		if(serviceName != null) {
			m_serviceMap.put(serviceName, service);
		}
	}
	
	public void doService(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException{
		String serviceName = request.getParameter("name");
		ServletOutputStream sos = response.getOutputStream();
		ServiceReturn ret = new ServiceReturn();
		
		if(serviceName == null) {
			ret.setError("unknown service!");
		} else {
			Service service = m_serviceMap.get(serviceName);
			if(service != null) {
				try {
					service.work(ret,request);
				} catch(Exception ex) {
					ret.setError(ex.getMessage());
				}
			} else {
				ret.setError("can not find service:" + serviceName);
			}
		}
		sos.write(ret.toString().getBytes("UTF-8"));
	}
}
