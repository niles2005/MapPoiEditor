package com.xtwsoft.poieditor.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * 简化html为微信小程序中可加载的页面，基本去掉了css，js的引用 转换延迟加载的image src，并作本地保存和引用。
 * 
 * @author NieLei
 *
 */
public class SimplifyHtml {
	private Document m_doc;
	private ArrayList<Element> m_imageList = new ArrayList<Element>();

	public SimplifyHtml(String strUrl) {
		try {
			// 在tomcat中运行，需jdk8之上，并配置SSL，否则会报错"Could not generate DH keypair"
			m_doc = Jsoup.connect(strUrl).get();

			// 去掉所有js
			Elements scripts = m_doc.getElementsByTag("script");
			scripts.remove();

			Elements images = m_doc.select("img");
			for (Element image : images) {
				String imageSrc = image.attr("data-src");
				if (imageSrc != null && imageSrc.startsWith("http")) {
					m_imageList.add(image);
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	// 产生目标wxml文本的md5sum
	public String buildMD5Sum() {
		return MD5Sum.encode32MD5(m_doc.html());
	}

	public String store(File file) throws Exception {
		PrintWriter writer = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(file), "UTF-8"));
		String strContent = m_doc.html();
		writer.write(strContent);
		writer.flush();
		writer.close();

		return MD5Sum.encode32MD5(strContent);
	}

	public ArrayList<File> storeImages(File path, String name) {
		ArrayList<File> fileList = new ArrayList<File>();
		for (int i = 0; i < m_imageList.size(); i++) {
			File imageFile = new File(path, name + "_" + i);
			storeImageFile(m_imageList.get(i), imageFile);
			fileList.add(imageFile);
		}
		return fileList;
	}

	public void storeImageFile(Element image, File file) {
		try {
			BufferedOutputStream bos = new BufferedOutputStream(
					new FileOutputStream(file));
			String imageSrc = image.attr("data-src");
			URL url = new URL(imageSrc);
			BufferedInputStream bis = new BufferedInputStream(url.openStream());
			int ch = bis.read();
			while (ch != -1) {
				bos.write(ch);
				ch = bis.read();
			}
			bos.flush();
			bos.close();
			image.attr("src", file.getName());
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	public int getImagesNum() {
		return m_imageList.size();
	}

	public static void main(String[] args) {
		SimplifyHtml builder = new SimplifyHtml(
				"https://mp.weixin.qq.com/s/mHShWRwg0sfCyaeMYr2TuQ");
		try {
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

}
