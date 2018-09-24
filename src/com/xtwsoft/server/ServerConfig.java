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
	
	private ServerConfig(String appName,File appPath) {
		if("/".equals(appName)) {
			m_appName = "";
		} else {
			m_appName = appName;
		}
		m_appPath = appPath;

		m_WEBINFPath = new File(appPath, "WEB-INF");
		if (m_WEBINFPath.exists()) {
			m_datasPath = new File(m_WEBINFPath, "datas");
			if(!m_datasPath.exists()) {
				m_datasPath.mkdir();
			}
			m_poisPath = new File(appPath, "p");
			if(!m_poisPath.exists()) {
				m_poisPath.mkdir();
			}
		} else {
			System.err.println("WEB-INF path not found!");
		}
	}
	
	public File getWEBINFPath() {
		return m_WEBINFPath;
	}
	
	public File getPOISPath() {
		return m_poisPath;
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


}
