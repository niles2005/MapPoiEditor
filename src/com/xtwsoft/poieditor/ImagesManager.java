package com.xtwsoft.poieditor;

import java.io.File;
import java.util.Hashtable;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.xtwsoft.server.ServerConfig;

public class ImagesManager {
	private static ImagesManager m_instance = null;
	
	private Hashtable<String,JSONObject> m_imageHash = new Hashtable<String,JSONObject>();
	//封面
	private JSONObject m_cover = new JSONObject();
	private JSONArray m_coverImages = new JSONArray();
	//地图图标
	private JSONObject m_marker = new JSONObject();
	private JSONArray m_markerImages = new JSONArray();
	//列表中景点图
	private JSONObject m_picture = new JSONObject();
	private JSONArray m_pictureImages = new JSONArray();
	
	//合并poiGroup配置中的marker和picture为一次请求
	private JSONObject m_poiGroup = new JSONObject();
	
	private ImagesManager() {
	}

	public static ImagesManager getInstance() {
		return m_instance;
	}

	public static void initInstance() {
		if (m_instance == null) {
			m_instance = new ImagesManager();
			m_instance.init();
		}
	}
	
	private void init() {
		m_poiGroup.put("marker", m_marker);
		m_poiGroup.put("picture", m_picture);
		
		m_cover.put("path", "images/cover/");
		m_cover.put("images", m_coverImages);
		
		m_marker.put("path", "images/marker/");
		m_marker.put("images", m_markerImages);

		m_picture.put("path", "images/picture/");
		m_picture.put("images", m_pictureImages);
		File imagesPath = new File(ServerConfig.getInstance().getAppPath(),"images");
		if(!imagesPath.exists()) {
			imagesPath.mkdir();
		}
		File coverPath = new File(imagesPath,"cover");
		initImagePath(coverPath,m_coverImages);

		File markerPath = new File(imagesPath,"marker");
		initMarkerImagePath(markerPath,m_markerImages);

		File picturePath = new File(imagesPath,"picture");
		initImagePath(picturePath,m_pictureImages);
		
		this.initImageObject("introduction",true);
	}
	
	private void initImageObject(String pathName,boolean makePath) {
		File imagesPath = new File(ServerConfig.getInstance().getImagesPath(),pathName);
		if(makePath) {
			if(!imagesPath.exists()) {
				imagesPath.mkdir();
			}
		}
		if(imagesPath.isDirectory() && imagesPath.getParentFile().equals(ServerConfig.getInstance().getImagesPath())) {
			JSONObject imageObj = m_imageHash.get(pathName);
			if(imageObj == null) {
				imageObj = new JSONObject();
				m_imageHash.put(pathName,imageObj);
				imageObj.put("path", "images/" + pathName + "/");
			}
			
			JSONArray images = new JSONArray();
			File[] files = imagesPath.listFiles();
			for(int i=0;i<files.length;i++) {
				File file = files[i];
				if(file.isFile()) {
					images.add(file.getName());
				}
			}
			imageObj.put("images", images);
		}
	}
	
	private void initImagePath(File path,JSONArray imageArray) {
		if(path.isDirectory() && path.exists()) {
			File[] files = path.listFiles();
			for(int i=0;i<files.length;i++) {
				File file = files[i];
				if(file.isFile()) {
					imageArray.add(file.getName());
				}
			}
		}
	}
	
	private void initMarkerImagePath(File path,JSONArray imageArray) {
		if(path.isDirectory() && path.exists()) {
			File[] files = path.listFiles();
			for(int i=0;i<files.length;i++) {
				File file = files[i];
				if(file.isFile()) {
					String fileName = file.getName();
					if(fileName.startsWith("focus")) {
						File normalImage = new File(path,fileName.substring(5));
						if(normalImage.exists() && normalImage.isFile()) {
							imageArray.add(normalImage.getName());
						}
					}
				}
			}
		}
	}
	
	public JSONObject resetImagePath(String pathName) {
		initImageObject(pathName,false);
		return m_imageHash.get(pathName);
	}
	
	public JSONObject getImages(String name) {
		JSONObject imageObj = m_imageHash.get(name);
		if(imageObj != null) {
			return imageObj;
		}
		if("marker".equals(name)) {
			return this.m_marker;
		} else if("picture".equals(name)) {
			return this.m_picture;
		} else if("cover".equals(name)) {
			return this.m_cover;
		} else if("group".equals(name)) {//合并marker和picture两次请求为一次
			return this.m_poiGroup;
		}
		return null;
	}
}
