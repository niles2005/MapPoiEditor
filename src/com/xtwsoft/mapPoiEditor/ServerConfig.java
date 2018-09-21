package com.xtwsoft.mapPoiEditor;

import java.io.File;

public class ServerConfig {
	private static ServerConfig m_instance = null;
	private String m_appName = null;
	private File m_appPath = null;
	private File m_WEBINFPath = null;
	private File m_datasPath = null;
	
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

			EditorManager.initInstance(m_datasPath);
		} else {
			System.err.println("WEB-INF path not found!");
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


}
