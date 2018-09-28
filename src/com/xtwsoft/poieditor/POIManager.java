package com.xtwsoft.poieditor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Date;
import java.util.Hashtable;
import java.util.Timer;
import java.util.TimerTask;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.xtwsoft.poieditor.utils.MD5Sum;
import com.xtwsoft.poieditor.utils.Utils;
import com.xtwsoft.server.ServerConfig;
import com.xtwsoft.server.ServiceManager;

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
	private JSONObject m_dataJson = null;
	private JSONArray m_poiJsonArray = null;// json
	private Hashtable<String, POI> m_poiHash = new Hashtable<String, POI>();
	private POISorter m_sorter = new POISorter();

	//用于标志POI是否增删改，如变动，需重新排序，和存盘
	private Boolean m_isChange = false;
	
	public static POIManager getInstance() {
		return m_instance;
	}

	public static void initInstance() {
		if (m_instance == null) {
			m_instance = new POIManager();
		}
	}

	private POIManager() {
		init();
		registerServices();
	}

	private void init() {
		m_dataJsonFile = new File(ServerConfig.getInstance().getDatasPath(),
				"datas.json");
		try {
			if (m_dataJsonFile.exists() && m_dataJsonFile.isFile()) {
				JSON theJson = Utils.loadJSON(m_dataJsonFile);
				if (theJson instanceof JSONObject) {
					m_dataJson = (JSONObject) theJson;
				}

				if (m_dataJson == null) {// 格式不对，重写
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
			if (m_dataJson != null) {
				m_poiJsonArray = m_dataJson.getJSONArray("pois");
				if (m_poiJsonArray == null) {
					m_poiJsonArray = new JSONArray();
					m_dataJson.put("pois", m_poiJsonArray);
				}
				for (int i = 0; i < m_poiJsonArray.size(); i++) {
					JSONObject json = m_poiJsonArray.getJSONObject(i);
					POI poi = new POI(json);
					if (poi.hasKey()) {
						m_poiHash.put(poi.getKey(), poi);
					}
				}
				// 加载数据后先进行一次排序
				m_sorter.sortPois(m_poiJsonArray);

				startTaskTimer();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public POI getPOI(String key) {
		return m_poiHash.get(key);
	}

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
		if (poi.hasNewFlag()) {
			m_poiJsonArray.add(poi.getJson());
			poi.removeNewFlag();
		}
		m_isChange = true;
		return null;
	}

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

	//避免无更新的存盘，保留上次存盘的checkSum
	private String m_storeDataSum = null;
	public void saveDatasToFile() {
		try {
			if (m_dataJson != null) {
				String str = m_dataJson.toJSONString();
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

	public JSONObject getDatas() {
		return m_dataJson;
	}

	public boolean removePOI(String key) {
		POI poi = m_poiHash.remove(key);
		if (poi != null) {
			m_poiJsonArray.remove(poi.getJson());
			m_isChange = true;
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
			m_sorter.sortPois(m_poiJsonArray);
			POIManager.getInstance().saveDatasToFile();
		}
	}

}
