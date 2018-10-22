package com.xtwsoft.poieditor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.xtwsoft.poieditor.utils.MD5Sum;
import com.xtwsoft.poieditor.utils.Utils;
import com.xtwsoft.server.ServerConfig;

/**
 * 用户处理逻辑，需实现为单例模式。 在使用中需注意2点：
 * 1.将本对象加入ServiceServlet的init()最后。不能加在ServerConfig或ServiceManager里。
 * 2.需将新加的服务注册到本对象。见registerServices()。
 * 
 * @author NieLei
 *
 */
public class POIManager extends TimerTask {
	private static POIManager m_instance = null;
	private File m_dataJsonFile = null;
	
	private JSONObject m_appJson = null;
	
	private Hashtable<String, POIGroup> m_poiGroupHash = new Hashtable<String, POIGroup>();

	private Hashtable<String, POI> m_poiHash = new Hashtable<String, POI>();

	//创建的详情页面json模板
	private JSONObject m_infoModelJson = null;
	
	//用于标志POI是否增删改，如变动，需重新排序，和存盘
	private Boolean m_isChange = false;
	
	public static POIManager getInstance() {
		return m_instance;
	}

	private POIManager() {
	}

	public static void initInstance() {
		if (m_instance == null) {
			m_instance = new POIManager();
			m_instance.init();
		}
	}

	private void init() {
		m_dataJsonFile = new File(ServerConfig.getInstance().getDatasPath(),
				"app.json");
		try {
			if (m_dataJsonFile.exists() && m_dataJsonFile.isFile()) {
				JSON theJson = Utils.loadJSON(m_dataJsonFile);
				if (theJson instanceof JSONObject) {
					m_appJson = (JSONObject) theJson;
				}

				if (m_appJson == null) {// 格式不对，重写
					JSONObject json = new JSONObject();
					json.put("groups", new JSONArray());
					Utils.writeJSON(json, m_dataJsonFile);
					m_appJson = json;
				}
			} else {
				JSONObject json = new JSONObject();
				json.put("groups", new JSONArray());
				Utils.writeJSON(json, m_dataJsonFile);
				m_appJson = json;
			}
			if (m_appJson != null) {
				JSONArray groupArray = m_appJson.getJSONArray("groups");
				if(groupArray == null) {
					groupArray = new JSONArray();
					m_appJson.put("groups", groupArray);
				} else {
					for(int i=0;i<groupArray.size();i++) {
						JSONObject groupJson = groupArray.getJSONObject(i);
						POIGroup poiGroup = new POIGroup(groupJson);
						m_poiGroupHash.put(poiGroup.getKey(), poiGroup);
					}
				}
				startTaskTimer();
			}
			
			m_infoModelJson = loadInfoModelJson();
			if(m_infoModelJson == null) {
				JSONObject json = new JSONObject();
				json.put("contents", new JSONArray());
				json.put("title", "");
				m_infoModelJson = json;
			}
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public JSONObject getAppJson() {
		return m_appJson;
	}
	
	public POI getPOI(String key) {
		return m_poiHash.get(key);
	}
	
	public void removePOIGroup(POIGroup poiGroup) {
		if(poiGroup != null) {
			m_appJson.getJSONArray("groups").remove(poiGroup.getJson());
			m_poiGroupHash.remove(poiGroup);
		}
	}

	//客户端更新POI
	public String updateApp(JSONObject json) {
		Iterator iters = json.keySet().iterator();
		while(iters.hasNext()) {
			String k = (String)iters.next();
			if(k.startsWith("_") || k.equals("groups")) {//避开临时属性，比如_new,及groups
			} else {
				m_appJson.put(k, json.get(k));
			}
		}
		
		JSONArray updateGroups = json.getJSONArray("groups");
		for(int i=0;i<updateGroups.size();i++) {
			JSONObject updateGroup = updateGroups.getJSONObject(i);
			if(updateGroup != null) {
				String key = updateGroup.getString("key");
				POIGroup theGroup = m_poiGroupHash.get(key);
				if(theGroup != null) {
					theGroup.update(updateGroup);
				}
			}
		}
		this.m_appJson.getJSONArray("groups").sort(new POIGroupComparator(updateGroups));
		m_isChange = true;
		return null;
	}
	
	//客户端更新POI
	public String updatePoi(JSONObject json) {
		String key = json.getString("key");
		if (key == null) {
			return "update poi don't has key property!";
		}
		POI poi = m_poiHash.get(key);
		if (poi == null) {
			return "can't find poi with key :" + key + "!";
		}
		poi.update(json);
		m_isChange = true;
		return null;
	}

	//客户端下载处理POI详情
	//用户点”下载&处理“时做的详情更新，会强制加载详情数据。
	public String updatePoiDetail(JSONObject json) {
		String key = json.getString("key");
		if (key == null) {
			return "update poi don't has key property!";
		}
		POI poi = m_poiHash.get(key);
		if (poi == null) {
			return "can't find poi with key :" + key + "!";
		}
		poi.updateDetail(json);
		m_isChange = true;
		return null;
	}


	//客户端删除POI
	public boolean removePOI(String key) {
		POI poi = m_poiHash.remove(key);
		if (poi != null) {
			poi.remove();
			m_isChange = true;
			return true;
		}
		return false;
	}

	//客户端创建POI
	public JSONObject createPOI(String groupKey) {
		POIGroup poiGroup = m_poiGroupHash.get(groupKey);
		if(poiGroup != null) {
			POI poi = new POI(poiGroup);
			m_poiHash.put(poi.getKey(), poi);
			JSONObject json = new JSONObject();
			json.put("key", poi.getKey());
			return json;
		}
		return null;
	}
	
	//客户端创建POIGroup,新创建时只加到m_poiGroupHash，设置_new标志，等正式提交时加入datas.groups
	public JSONObject createPOIGroup(String groupname) {
		POIGroup poiGroup = new POIGroup(groupname);
		m_poiGroupHash.put(poiGroup.getKey(), poiGroup);
		return poiGroup.getJson();
	}
	
	//存储POI
	protected void storePOI(POI poi) {
		if(poi != null) {
			m_poiHash.put(poi.getKey(), poi);
		}
	}
	
	public JSONObject buildAllPoiDetails() {
		Iterator iters = this.m_poiHash.values().iterator();
		int successCount = 0;
		int failedCount = 0;
		while(iters.hasNext()) {
			POI poi = (POI)iters.next();
			if(poi.buildDetail(false)) {
				successCount++;
			} else {
				failedCount++;
			}
		}
		JSONObject ret = new JSONObject();
		ret.put("successCount", successCount);
		ret.put("failedCount", failedCount);
		return ret;
	}

	//避免无更新的存盘，保留上次存盘的checkSum
	private String m_storeDataSum = null;
	public void saveDatasToFile() {
		try {
			if (m_appJson != null) {
				String str = m_appJson.toJSONString();
				String sum = MD5Sum.encode32MD5(str);
				//避免每次重复存盘，先做checkSum比较。
				if(!sum.equals(m_storeDataSum)) {
					PrintWriter writer = new PrintWriter(new OutputStreamWriter(
							new FileOutputStream(m_dataJsonFile), "UTF-8"));
					writer.write(str);
					writer.flush();
					writer.close();
					m_storeDataSum = sum;
				}
				// System.err.println("save datas at :" + new Date());
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public JSONObject cloneInfoModelJson() {
		return (JSONObject)this.m_infoModelJson.clone();
	}
	
	private JSONObject loadInfoModelJson() {
		try {
			File f = new File(ServerConfig.getInstance().getWEBINFPath(),"infoModel.json");
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
	
	// 5min 5 * 60 * 1000 = 300000
	private void startTaskTimer() {
		long delay = 5 * 60 * 1000;
		long period = delay;
		new Timer().schedule(this, delay, period);
	}

	// task work
	public void run() {
		if(m_isChange) {
			m_isChange = false;
			
			Iterator iters = m_poiGroupHash.values().iterator();
			while(iters.hasNext()) {
				POIGroup poiGroup = (POIGroup)iters.next();
				poiGroup.sortPOIs();
			}
			POIManager.getInstance().saveDatasToFile();
		}
	}

}
