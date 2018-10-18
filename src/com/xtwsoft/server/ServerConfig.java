package com.xtwsoft.server;

import java.io.File;

import com.xtwsoft.poieditor.POIManager;

public class ServerConfig {
	private static ServerConfig m_instance = null;
	private String m_appName = null;
	private File m_appPath = null;
	private File m_WEBINFPath = null;
	private File m_datasPath = null;
	private File m_poisPath = null;
	private File m_introPath = null;
	private File m_imagesPath = null;
	
	private ServerConfig(String appName,File appPath) {
		if("/".equals(appName)) {
			m_appName = "";
		} else {
			m_appName = appName;
		}
		m_appPath = appPath;

		m_WEBINFPath = new File(appPath, "WEB-INF");
		if (m_WEBINFPath.exists()) {
		} else {
			System.err.println("WEB-INF path not found!");
		}
		
		m_datasPath = new File(appPath, "datas");
		if(!m_datasPath.exists()) {
			m_datasPath.mkdir();
		}
		m_poisPath = new File(m_datasPath, "p");
		if(!m_poisPath.exists()) {
			m_poisPath.mkdir();
		}
		m_imagesPath = new File(appPath, "images");
		if(!m_imagesPath.exists()) {
			m_imagesPath.mkdir();
		}
		
		m_introPath = new File(m_datasPath, "intro");
		if(!m_introPath.exists()) {
			m_introPath.mkdir();
		}
	}
	
	public File getWEBINFPath() {
		return m_WEBINFPath;
	}
	
	public static ServerConfig getInstance() {
		return m_instance;
	}
	

	public static void initInstance(String appName,String path) {
		if (m_instance != null) {
			return;
		}
		m_instance = new ServerConfig(appName,new File(path));
	}
	
	public File getDatasPath() {
		return m_datasPath;
	}

	public String getAppName() {
		return this.m_appName;
	}
	
	public File getAppPath() {
		return m_appPath;
	}

	public File getPOISPath() {
		return m_poisPath;
	}
	
	public File getIntroPath() {
		return m_introPath;
	}
	
	public File getImagesPath() {
		return m_imagesPath;
	}
	

}
