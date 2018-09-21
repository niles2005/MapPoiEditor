package com.xtwsoft.mapPoiEditor.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

public class Utils {
	public static void writeJSON(JSON json,File jsonFile) throws Exception {
		PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(jsonFile),"UTF-8"));
		writer.write(json.toJSONString());
		writer.flush();
		writer.close();
	}
	
	public static JSON loadJSON(File file) throws Exception {
		if(file.exists()) {
			
			StringBuffer strBuff = new StringBuffer();
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file),"utf-8"));
			String line = reader.readLine();
			if (line != null && line.startsWith("\uFEFF")) {//remove utf-8 bom
				line = line.substring(1);
			}
			while(line != null) {
				strBuff.append(line);
				line = reader.readLine();
			}
			reader.close();
			String content = strBuff.toString().trim();
			if(content.startsWith("{")) {
				JSONObject jsonObject = JSON.parseObject(strBuff.toString());
				return jsonObject;
			} else if(content.startsWith("[")) {
				JSONArray jsonArray = JSON.parseArray(strBuff.toString());
				return jsonArray;
			}
		}
		return null;
	}
}
