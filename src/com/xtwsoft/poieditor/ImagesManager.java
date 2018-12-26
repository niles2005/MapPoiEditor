package com.xtwsoft.poieditor;

import java.io.File;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.xtwsoft.server.ServerConfig;

public class ImagesManager {
	private static ImagesManager m_instance = null;

	// 封面
	private JSONObject m_cover = new JSONObject();
	private JSONArray m_coverImages = new JSONArray();
	// 地图图标
	private JSONObject m_marker = new JSONObject();
	private JSONArray m_markerImages = new JSONArray();
	// 列表中景点图
	private JSONObject m_picture = new JSONObject();
	private JSONArray m_pictureImages = new JSONArray();

	// 合并poiGroup配置中的marker和picture为一次请求
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

		resetImageType("cover");
		resetImageType("marker");
		resetImageType("picture");

	}

	private void initImagePath(File path, JSONArray imageArray) {
		if (path.isDirectory() && path.exists()) {
			imageArray.clear();
			File[] files = path.listFiles();
			for (int i = 0; i < files.length; i++) {
				File file = files[i];
				if (file.isFile()) {
					imageArray.add(file.getName());
				}
			}
		}
	}

	private void initMarkerImagePath(File path, JSONArray imageArray) {
		if (path.isDirectory() && path.exists()) {
			File[] files = path.listFiles();
			for (int i = 0; i < files.length; i++) {
				File file = files[i];
				if (file.isFile()) {
					String fileName = file.getName();
					if (fileName.startsWith("focus")) {
						File normalImage = new File(path, fileName.substring(5));
						if (normalImage.exists() && normalImage.isFile()) {
							imageArray.add(normalImage.getName());
						}
					}
				}
			}
		}
	}

	public void resetImageType(String type) {
		File imagesPath = new File(ServerConfig.getInstance().getAppPath(),
				"images");
		if (!imagesPath.exists()) {
			imagesPath.mkdir();
		}
		if ("cover".equals(type)) {
			File coverPath = new File(imagesPath, "cover");
			initImagePath(coverPath, m_coverImages);
		} else if ("picture".equals(type)) {
			File picturePath = new File(imagesPath, "picture");
			initImagePath(picturePath, m_pictureImages);
		} else if ("marker".equals(type)) {
			File markerPath = new File(imagesPath, "marker");
			initMarkerImagePath(markerPath, m_markerImages);
		}
	}

	public JSONObject getImages(String name) {
		if ("marker".equals(name)) {
			return this.m_marker;
		} else if ("picture".equals(name)) {
			return this.m_picture;
		} else if ("cover".equals(name)) {
			return this.m_cover;
		} else if ("group".equals(name)) {// 合并marker和picture两次请求为一次
			return this.m_poiGroup;
		}
		return null;
	}
}
