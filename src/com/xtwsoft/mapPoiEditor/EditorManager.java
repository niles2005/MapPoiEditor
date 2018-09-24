package com.xtwsoft.mapPoiEditor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Hashtable;
import java.util.Iterator;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.xtwsoft.mapPoiEditor.utils.Guid;
import com.xtwsoft.mapPoiEditor.utils.LoopTask;
import com.xtwsoft.mapPoiEditor.utils.Utils;
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
public class EditorManager {
	private File m_datasPath = null;
	private static EditorManager m_instance = null;
	private File m_dataJsonFile = null;
	private JSONObject m_dataJson = null;
	private JSONArray m_poisArray = null;
	private Hashtable<String,JSONObject> m_poiHash = new Hashtable<String,JSONObject>();
	
	public static EditorManager getInstance() {
		return m_instance;
	}

	public static void initInstance() {
		if(m_instance == null) {
			m_instance = new EditorManager();
		}
	}
	
	
	private EditorManager() {
		m_datasPath = ServerConfig.getInstance().getDatasPath();
		init();
		registerServices();
	}
	
	private void init() {
		m_dataJsonFile = new File(m_datasPath,"datas.json");
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
				m_poisArray = m_dataJson.getJSONArray("pois");
				if(m_poisArray == null) {
					m_poisArray = new JSONArray();
					m_dataJson.put("pois", m_poisArray);
				}
				for(int i=0;i<m_poisArray.size();i++) {
					JSONObject poi = m_poisArray.getJSONObject(i);
					if(poi != null) {
						String key = poi.getString("key");
						if(key != null) {
							m_poiHash.put(key, poi);
						}
					}
				}
			}
			
			LoopTask.startTimer();			
		} catch(Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public String updatePoi(JSONObject poi) {
		String key = poi.getString("key");
		if(key == null) {
			return "update poi don't has key property!";
		}
		JSONObject thePoi = m_poiHash.get(key);
		if(thePoi == null) {
			return "can't find poi with key :" + key + "!";
		}
		Iterator iters = poi.keySet().iterator();
		while(iters.hasNext()) {
			String strKey = (String)iters.next();
			thePoi.put(strKey, poi.get(strKey));
		}
		if(thePoi.getBooleanValue("_new")) {
			m_poisArray.add(thePoi);
			thePoi.remove("_new");
		}
		return null;
	}
	
	public void saveDatasToFile() {
		try {
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
		JSONObject poi = m_poiHash.remove(key);
		if(poi != null) {
			m_poisArray.remove(poi);
		}
		return true;
	}
	
	public JSONObject createPOI() {
		String guid = Guid.build16Guid();
		JSONObject poi = new JSONObject();
		poi.put("key", guid);
		poi.put("_new", true);//临时属性，表示是地图上新创建，但还没有加入datas的点，此点还有如名称等属性没有完善。
		m_poiHash.put(guid, poi);
		return poi;
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
	}
}
