package com.xtwsoft.server;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.xtwsoft.poieditor.services.BuildAllDetailsService;
import com.xtwsoft.poieditor.services.CreatePOIGroupService;
import com.xtwsoft.poieditor.services.CreatePOIService;
import com.xtwsoft.poieditor.services.DatasService;
import com.xtwsoft.poieditor.services.DetailFilesService;
import com.xtwsoft.poieditor.services.UploadDetailFilesService;
import com.xtwsoft.poieditor.services.DetailJsonBuildService;
import com.xtwsoft.poieditor.services.FileSaveService;
import com.xtwsoft.poieditor.services.ImagesService;
import com.xtwsoft.poieditor.services.PathFilesService;
import com.xtwsoft.poieditor.services.RemovePOIService;
import com.xtwsoft.poieditor.services.SaveAllService;
import com.xtwsoft.poieditor.services.UpdateAppService;
import com.xtwsoft.poieditor.services.UpdateDetailService;
import com.xtwsoft.poieditor.services.UpdatePOIService;
import com.xtwsoft.poieditor.services.UploadImageService;
import com.xtwsoft.poieditor.services.UploadZipService;

/**
 * 
 *	标准化的后台服务处理，通过注册的方式加入服务，新的项目只要实现需要的服务模块。
 * @author Nielei
 *
 */
public class ServiceManager {
	private Map<String , Service> m_serviceMap = new HashMap<String , Service>();
	private Map<String , Service> m_uploadServiceMap = new HashMap<String , Service>();
	private static ServiceManager m_instance = null;

	public static ServiceManager getInstance() {
		return m_instance;
	}

	public static void initInstance() {
		if(m_instance == null) {
			m_instance = new ServiceManager();
			m_instance.registerServices();
			m_instance.registerUploadServices();
		}
		
	}
	
	private ServiceManager() {
	}
	
	private void addService(Service service) {
		String serviceName = service.getServiceName();
		if(serviceName != null) {
			m_serviceMap.put(serviceName, service);
		}
	}
	
	private void addUploadService(Service service) {
		String serviceName = service.getServiceName();
		if(serviceName != null) {
			m_uploadServiceMap.put(serviceName, service);
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
	
	public void doUploadService(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException{
		String serviceName = request.getParameter("name");
		ServletOutputStream sos = response.getOutputStream();
		ServiceReturn ret = new ServiceReturn();
		
		if(serviceName == null) {
			ret.setError("unknown upload service!");
		} else {
			Service service = m_uploadServiceMap.get(serviceName);
			if(service != null) {
				try {
					service.work(ret,request);
				} catch(Exception ex) {
					ret.setError(ex.getMessage());
				}
			} else {
				ret.setError("can not find upload service:" + serviceName);
			}
		}
		sos.write(ret.toString().getBytes("UTF-8"));
	}
	
	/**
	 * 注册服务。在使用中可以按服务名找到对应的服务。
	 */
	public void registerServices() {
		ServiceManager serviceManager = ServiceManager.getInstance();
		serviceManager.addService(new DatasService());
		serviceManager.addService(new CreatePOIService());
		serviceManager.addService(new CreatePOIGroupService());
		serviceManager.addService(new RemovePOIService());
		serviceManager.addService(new UpdatePOIService());
		serviceManager.addService(new SaveAllService());
		serviceManager.addService(new UpdateDetailService());
		serviceManager.addService(new UpdateAppService());
		serviceManager.addService(new BuildAllDetailsService());
		serviceManager.addService(new DetailFilesService());
		serviceManager.addService(new FileSaveService());
		serviceManager.addService(new DetailJsonBuildService());
		serviceManager.addService(new PathFilesService());
		
		serviceManager.addService(new ImagesService());
	}
	
	public void registerUploadServices() {
		ServiceManager serviceManager = ServiceManager.getInstance();
		serviceManager.addUploadService(new UploadImageService());
		serviceManager.addUploadService(new UploadZipService());
		serviceManager.addUploadService(new UploadDetailFilesService());
	}
	
}
