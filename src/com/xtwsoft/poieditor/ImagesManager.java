package com.xtwsoft.poieditor;

import java.io.File;

import com.alibaba.fastjson.JSONArray;
import com.xtwsoft.server.ServerConfig;

public class ImagesManager {
	private static ImagesManager m_instance = null;
	//封面
	private JSONArray m_coverImages = new JSONArray();
	//地图图标
	private JSONArray m_markerImages = new JSONArray();
	//列表中景点图
	private JSONArray m_pictureImages = new JSONArray();
	
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
	
	public JSONArray getImages(String name) {
		if("marker".equals(name)) {
			return this.m_markerImages;
		} else if("picture".equals(name)) {
			return this.m_pictureImages;
		} else if("cover".equals(name)) {
			return this.m_coverImages;
		}
		return null;
	}
}
