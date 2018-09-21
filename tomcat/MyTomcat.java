import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Iterator;

import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

/**
 * Copyright(c) 2010 XTWSoft, Inc. Date:2014-1-8 上午13:14:01 Author: NieLei
 * E-mail: niles2010@live.cn
 */

public class MyTomcat {
	public MyTomcat() {
		try {

			JSONObject config = loadTomcatJSON();
			if(config == null) {
				return;
			}
			Tomcat tomcat = new Tomcat();
			tomcat.setBaseDir(config.getString("baseDir"));
			tomcat.setPort(config.getInteger("port"));
			tomcat.getConnector().setURIEncoding(config.getString("URIEncoding"));
			
			JSONArray arr = config.getJSONArray("webApps");
			for(int i=0;i<arr.size();i++) {
				JSONObject obj = arr.getJSONObject(i);
				String strPath = obj.getString("path");
				Context context = tomcat.addWebapp(obj.getString("name"),strPath);
				context.setCrossContext(true);
				context.setSessionCookiePath("/");
				Iterator iter = obj.keySet().iterator();
				while(iter.hasNext()) {
					String key = (String)iter.next();
					if(key.equals("name") || key.equals("path")) {
						
					} else {
						String value = (String)obj.getString(key);
						context.addParameter(key, value);
					}
				}
			}
			
			tomcat.start();
			System.out.println("Started tomcat");
			tomcat.getServer().await(); // Keeps Tomcat running until it is shutdown
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}
	
	private JSONObject loadTomcatJSON() {
		try {
			File tomcatJosnFile = new File("tomcat.json");
			if(tomcatJosnFile.exists()) {
				
				StringBuffer strBuff = new StringBuffer();
				BufferedReader reader = new BufferedReader(new FileReader(tomcatJosnFile));
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


	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new MyTomcat();
	}

}
