package com.xtwsoft.poieditor.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;

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
	private static HashSet<String> m_removeImageSumSet = new HashSet<String>(); 

	static {
		m_removeImageSumSet.add("e8614640bd4741036fc8be9b97c7379d");//头部  关注
		m_removeImageSumSet.add("93c597d2f51c98e903b988fbbb17d8c2");//头部  关注
		
		m_removeImageSumSet.add("9cbae971509d714bfd0d9cc12b5ab0ce");//尾部 二维码
		m_removeImageSumSet.add("d09c2bdc7d1381e3e3935a8986d6d8f4");//尾部 二维码
		m_removeImageSumSet.add("ea3c5525975cf97f42b278f29de15355");//尾部 二维码
		m_removeImageSumSet.add("23099586f25a8292115c7f3e98bd9be9");//尾部 二维码
	}
	
	public SimplifyHtml(String strUrl) {
		try {
			
			// 在tomcat中运行，需jdk8之上，并配置SSL，否则会报错"Could not generate DH keypair"
			m_doc = Jsoup.connect(strUrl).get();
			
			// 去掉所有js
			Elements scripts = m_doc.getElementsByTag("script");
			scripts.remove();
			
			Elements elements = m_doc.select("#js_article");
			if(elements.size() == 1) {
				elements.get(0).siblingElements().remove();
			}
			
			elements = m_doc.select("#page-content");
			if(elements.size() == 1) {
				elements.get(0).siblingElements().remove();
			}
			
			elements = m_doc.select("#img-content");
			if(elements.size() == 1) {
				elements.get(0).siblingElements().remove();
			}
			
			elements = m_doc.select("#img-content>div");
			for(Element element:elements) {
				String theId = element.attr("id");
				if(theId != null && theId.endsWith("content")) {
					
				} else {
					element.remove();
				}
			}
			
			
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
		ArrayList<Element> removeList = new ArrayList<Element>();
		int index = 0;
		for(Element image:m_imageList) {
			File imageFile = new File(path, name + "_" + index);
			if(storeImageFile(image, imageFile)) {
				removeList.add(image);
			} else {
				fileList.add(imageFile);
				index++;
			}
		}
		for(Element image:removeList) {
			m_imageList.remove(image);
		}
		return fileList;
	}

	//return true:remove  false:use
	public boolean storeImageFile(Element image, File file) {
		try {
			String imageSrc = image.attr("data-src");
			URL url = new URL(imageSrc);
			BufferedInputStream bis = new BufferedInputStream(url.openStream());
			
			byte[] buff = new byte[4096];
			//使用int num = bis.read(buff)方式，num可能不是4096
			int ch = bis.read();
			int index = 0;
			while(ch != -1) {
				buff[index] = (byte)ch;
				index++;
				if(index >= 4096) {
					break;
				}
				ch = bis.read();
			}
			if(index == 4096) {
				String sum = MD5Sum.getByteArrayMD5Sum(buff);
				if(m_removeImageSumSet.contains(sum)) {//是目标文件
					image.parent().remove();
					try{
						bis.close();
					} catch(Exception ex) {
					}
					return true;
				}
			}

			BufferedOutputStream bos = new BufferedOutputStream(
					new FileOutputStream(file));
			bos.write(buff,0,index);
			bos.flush();
			int num = bis.read(buff);
			while (num > 0) {
				bos.write(buff,0,num);
				num = bis.read(buff);
			}
			bos.flush();
			bos.close();
			image.attr("src", file.getName());
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return false;
	}

	public int getImagesNum() {
		return m_imageList.size();
	}
	
	public static void main(String[] args) {
		try {
			SimplifyHtml builder = new SimplifyHtml(
					"https://mp.weixin.qq.com/s/vplqc8frSsrOP5nLkn6Zvw");
			String key = "tttttttttt";
			File testPath = new File(key);
			testPath.mkdir();
			builder.storeImages(testPath, key);
			builder.store(new File(testPath, key + ".html"));
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}	
	
}
