package com.xtwsoft.server;

import java.util.HashMap;

import javax.servlet.http.HttpSession;

public class SessionManager {
	private static HashMap<String, HttpSession> m_loginAccountMap = new HashMap<String, HttpSession>();  
	public static void addSession(String account,HttpSession accountSession) {
		m_loginAccountMap.put(account, accountSession);
	}

	public static void removeSession(String account) {
		m_loginAccountMap.remove(account);
	}

	public static HttpSession getSession(String account) {
		return m_loginAccountMap.get(account);
	}
	
	
}
