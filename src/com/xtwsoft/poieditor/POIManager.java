package com.xtwsoft.poieditor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Comparator;
import java.util.Hashtable;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.xtwsoft.poieditor.utils.LoopTask;
import com.xtwsoft.poieditor.utils.Utils;
import com.xtwsoft.server.ServerConfig;
import com.xtwsoft.server.ServiceManager;

/**
 * 用户处理逻辑，需实现为单例模式。
 * 在使用中需注意2点：
 * 1.将本对象加入ServiceServlet的init()最后。不能加在ServerConfig或ServiceManager里。
 * 2.需将新加的服务注册到本对象。见registerServices()。
 * 
 * @author NieLei
 *
 */
public class POIManager {
	private static POIManager m_instance = null;
	private File m_dataJsonFile = null;
	private JSONObject m_dataJson = null;
	private JSONArray m_poiJsonArray = null;//json 
	private Hashtable<String,POI> m_poiHash = new Hashtable<String,POI>();
	private POISorter m_sorter = new POISorter();
	
	public static POIManager getInstance() {
		return m_instance;
	}

	public static void initInstance() {
		if(m_instance == null) {
			m_instance = new POIManager();
		}
	}
	
	
	private POIManager() {
		init();
		registerServices();
	}
	
	private void init() {
		m_dataJsonFile = new File(ServerConfig.getInstance().getDatasPath(),"datas.json");
		try {
			if(m_dataJsonFile.exists() && m_dataJsonFile.isFile()) {
				JSON theJson = Utils.loadJSON(m_dataJsonFile);
				if(theJson instanceof JSONObject) {
					m_dataJson = (JSONObject)theJson;
				}
				if(m_dataJson == null) {//格式不对，重写
					JSONObject json = new JSONObject();
					json.put("pois", new JSONArray());
					Utils.writeJSON(json, m_dataJsonFile);
					m_dataJson = json;
				}
			} else {
				JSONObject json = new JSONObject();
				Utils.writeJSON(json, m_dataJsonFile);
				m_dataJson = json;
			}
			if(m_dataJson != null) {
				m_poiJsonArray = m_dataJson.getJSONArray("pois");
				if(m_poiJsonArray == null) {
					m_poiJsonArray = new JSONArray();
					m_dataJson.put("pois", m_poiJsonArray);
				}
				for(int i=0;i<m_poiJsonArray.size();i++) {
					JSONObject json = m_poiJsonArray.getJSONObject(i);
					POI poi = new POI(json);
					if(poi.hasKey()) {
						m_poiHash.put(poi.getKey(), poi);
					}
				}
			}
			LoopTask.startTimer();			
		} catch(Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public POI getPOI(String key) {
		return m_poiHash.get(key);
	}
	
	public String updatePoi(JSONObject json) {
		String key = json.getString("key");
		if(key == null) {
			return "update poi don't has key property!";
		}
		POI poi = m_poiHash.get(key);
		if(poi == null) {
			return "can't find poi with key :" + key + "!";
		}
		poi.update(json);
		if(poi.hasNewFlag()) {
			m_poiJsonArray.add(poi.getJson());
			poi.removeNewFlag();
		}
		return null;
	}
	
	public String updatePoiDetail(JSONObject json) {
		String key = json.getString("key");
		if(key == null) {
			return "update poi don't has key property!";
		}
		POI poi = m_poiHash.get(key);
		if(poi == null) {
			return "can't find poi with key :" + key + "!";
		}
		poi.updateDetail(json);
		return null;
	}
	

	
	public void saveDatasToFile() {
		try {
			m_sorter.sortPois(m_poiJsonArray);
			if(m_dataJson != null) {
				PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(m_dataJsonFile),"UTF-8"));
				writer.write(m_dataJson.toJSONString());
				writer.flush();
				writer.close();
//				System.err.println("save datas at :" + new Date());
			}
		} catch(Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public JSONObject getDatas() {
		return m_dataJson;
	}
	
	public boolean removePOI(String key) {
		POI poi = m_poiHash.remove(key);
		if(poi != null) {
			m_poiJsonArray.remove(poi.getJson());
		}
		return true;
	}
	
	public JSONObject createPOI() {
		POI poi = new POI();
		m_poiHash.put(poi.getKey(), poi);
		return poi.getJson();
	}
	
	/**
	 * 注册服务。在使用中可以按服务名找到对应的服务。
	 */
	public void registerServices() {
		ServiceManager serviceManager = ServiceManager.getInstance();
		serviceManager.addService(new DatasService());
		serviceManager.addService(new CreatePOIService());
		serviceManager.addService(new RemovePOIService());
		serviceManager.addService(new UpdatePOIService());
		serviceManager.addService(new SaveAllService());
		serviceManager.addService(new UpdateDetailService());
		serviceManager.addService(new LoadImagesService());
	}
}
