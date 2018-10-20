package com.xtwsoft.poieditor.services;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;

import com.alibaba.fastjson.JSON;
import com.xtwsoft.server.ServerConfig;
import com.xtwsoft.server.Service;
import com.xtwsoft.server.ServiceReturn;

/**
 * 保存文件
 * like:    /service?name=filesave&path=/datas/test.json
 * @author NieLei
 *
 */
public class FileSaveService extends Service {
	public FileSaveService() {
		super("filesave");
	}
	
	public void work(ServiceReturn ret, HttpServletRequest request) {
		try {
			String path = request.getParameter("path");
			if(path == null) {
				ret.setError("path is empty!");
				return;
			}
			
			String strContent = getPostContent(request);
			if(path.endsWith(".json")) {
				JSON.parse(strContent);
			}
			File file = new File(ServerConfig.getInstance().getAppPath(),path);
			PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(file),"UTF-8"));
			writer.write(strContent);
			writer.flush();
			writer.close();
			
			ret.setSuccess("save file success!");
		} catch(Exception ex) {
			ex.printStackTrace();
			ret.setError(ex.getMessage());
		}
	}
}
