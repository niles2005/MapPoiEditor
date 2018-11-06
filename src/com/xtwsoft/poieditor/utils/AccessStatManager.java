package com.xtwsoft.poieditor.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

public class AccessStatManager {
	private static final int StartYear = 2018;
	private static final int StartMonth = 9;
	private static AccessStatManager m_instance = null;

	private AccessStatManager() {
	}

	public static AccessStatManager getInstance() {
		return m_instance;
	}

	public static void initInstance(File appPath) {
		if (m_instance == null) {
			m_instance = new AccessStatManager();
			m_instance.init(appPath);
		}
	}

	private File m_logPath = null;
	private File m_statPath = null;

	private void init(File appPath) {
		m_logPath = new File(appPath, "../../logs");
		m_statPath = new File(appPath, "stat");
		if (!m_statPath.isDirectory()) {
			m_statPath.mkdir();
		}
	}

	public void doStat(String yearMonth) {
		if (m_logPath.exists()) {
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM");
			Calendar cal = Calendar.getInstance();
			long currentTimeInMillis = cal.getTimeInMillis();
			String currentYearMonth = df.format(cal.getTime());
			if (yearMonth == null || yearMonth.length() != 7) {
				yearMonth = currentYearMonth;
			}
			int index = 0;
			while (true) {
				cal.set(Calendar.YEAR, StartYear);
				cal.set(Calendar.MONTH, StartMonth + index++);
				cal.set(Calendar.DAY_OF_MONTH, 1);

				if (cal.getTimeInMillis() <= currentTimeInMillis) {
					String strYearMonth = df.format(cal.getTime());
					if (strYearMonth.equals(yearMonth)) {
						statMonth(yearMonth);
						break;
					}
				} else {
					break;
				}
			}
		}
	}

	private void statMonth(String strYearMonth) {
		String strPrevFileName = "localhost_access_log." + strYearMonth;
		File statFile = new File(m_statPath, strYearMonth + ".json");
		JSONArray arr = new JSONArray();
		File[] files = m_logPath.listFiles();
		for (int i = 0; i < files.length; i++) {
			String fileName = files[i].getName();
			if (fileName.startsWith(strPrevFileName)) {
				String strDate = fileName.substring(21, fileName.length() - 4);
				JSONObject dateJson = new JSONObject();
				dateJson.put("date", strDate);
				int day = Integer.parseInt(strDate.substring(8));
				dateJson.put("day", day);
				arr.add(dateJson);
				statFile(files[i], strDate, dateJson);
			}
		}
		storeJsonFile(statFile, arr);
	}

	private void statFile(File file, String date, JSONObject dateJson) {
		try {
			BufferedReader reader = new BufferedReader(new FileReader(file));
			String line = reader.readLine();
			int count = 0;
			while (line != null) {
				if (line.indexOf("GET /yuyuanlu/datas/app.json ") != -1) {
					count++;
				}
				line = reader.readLine();
			}
			reader.close();
			dateJson.put("count", count);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private void storeJsonFile(File file, JSON json) {
		try {
			// System.err.println(file.getName());
			PrintWriter writer = new PrintWriter(new FileWriter(file));
			writer.write(json.toJSONString());
			writer.flush();
			writer.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public static void main(String[] args) {
		File logPath = new File(
				"C:\\mywork\\szjt\\workspace\\yylLogs\\webapps\\yyl");
		AccessStatManager.initInstance(logPath);
		AccessStatManager.getInstance().doStat("2018-11");

	}
}
