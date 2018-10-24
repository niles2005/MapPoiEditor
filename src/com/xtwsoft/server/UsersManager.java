package com.xtwsoft.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.xtwsoft.poieditor.utils.Guid;
import com.xtwsoft.poieditor.utils.MD5Sum;


public class UsersManager {
	private static UsersManager m_instance = null;
	private JSONObject m_usersJson = null;

	private File m_usersFilePath = null;
	
	public static UsersManager getInstance() {
		return m_instance;
	}

	private UsersManager(File path) {
		m_usersFilePath = path;
	}

	public static void initInstance(File path) {
		if (m_instance == null) {
			m_instance = new UsersManager(path);
			m_instance.init();
		}
	}

	private void init() {
		m_usersJson = loadUsersJson();
		if(m_usersJson == null) {
			m_usersJson = new JSONObject();
		}
	}
	
	protected boolean checkUserPass(String user,String password) {
		if(password == null) {
			return false;
		}
		JSONObject userObj = (JSONObject)m_usersJson.get(user);
		if(userObj != null) {
			return password.equals(userObj.getString("pass"));
		}
		return false;
	}
	
	private JSONObject loadUsersJson() {
		try {
			File f = new File(m_usersFilePath,"users.json");
			if(f.exists()) {
				StringBuffer strBuff = new StringBuffer();
				BufferedReader reader = new BufferedReader(new FileReader(f));
				String line = reader.readLine();
				if (line != null && line.startsWith("\uFEFF")) {//remove utf-8 bom
					line = line.substring(1);
				}
				while(line != null) {
					strBuff.append(line);
					line = reader.readLine();
				}
				reader.close();
				JSONObject jsonObject = JSON.parseObject(strBuff.toString());
				return jsonObject;
			}
		} catch(Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}
	
	private void storeUsers() {
		try {
			File f = new File(m_usersFilePath,"users.json");
			PrintWriter writer = new PrintWriter(new OutputStreamWriter(
					new FileOutputStream(f), "UTF-8"));
			writer.write(m_usersJson.toJSONString());
			writer.flush();
			writer.close();
		} catch(Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public void addUserPassword(String user,String password) {
		String salt = Guid.build16Guid();
		String newPass = MD5Sum.encode32MD5(password + salt);
		JSONObject jsonObj = new JSONObject();
		jsonObj.put("name", user);
		jsonObj.put("pass", newPass);
		jsonObj.put("salt", salt);
		
		m_usersJson.put(user, jsonObj);
		storeUsers();
	}
	
	public String getUserSalt(String user) {
		JSONObject userObj = (JSONObject)m_usersJson.get(user);
		if(userObj != null) {
			return userObj.getString("salt");
		}
		return null;
	}
	
	public static void main(String[] args) {
		UsersManager.initInstance(new File("."));
		UsersManager.getInstance().addUserPassword("gxt", "gxt12345");
		UsersManager.getInstance().addUserPassword("niles", "niles1");
	}
}
