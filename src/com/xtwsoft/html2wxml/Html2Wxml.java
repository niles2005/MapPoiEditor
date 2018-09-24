package com.xtwsoft.html2wxml;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URLDecoder;
import java.util.ArrayList;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.xtwsoft.poieditor.utils.MD5Sum;

/**
 * 
 * 转换html内容为微信小程序的wxml格式
 * @author NieLei
 *
 */
public class Html2Wxml {
	private View m_rootView = null;
	private String m_meta;//used for audio
	
	private ArrayList<ViewAudio> m_audioImageList = new ArrayList<ViewAudio>();
	private ArrayList<ViewImage> m_viewImageList = new ArrayList<ViewImage>();
	public Html2Wxml(String strUrl) {
		try {
			//在tomcat中运行，需jdk8之上，并配置SSL，否则会报错"Could not generate DH keypair"
			Document doc = Jsoup.connect(strUrl).get();
			
			View rootView = new View();
			rootView.addClass("rich_media_container");
			
			buildTitle(rootView,doc);
			
			buildMeta(rootView,doc.select("div#meta_content"));
			
			buildContent(rootView,doc.select("div#js_content"));
			
			m_rootView = rootView;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public View getRootView() {
		return m_rootView;
	}
	
	//产生目标wxml文本的md5sum
	public String buildMD5Sum() {
		if(m_rootView != null) {
			StringBuffer strBuff = new StringBuffer();
			m_rootView.buildWxml(strBuff);
			return MD5Sum.encode32MD5(strBuff.toString());
		}
		return null;
	}
	
	public ArrayList<ViewImage> getViewImageList() {
		return m_viewImageList;
	}
	
	public ArrayList<ViewAudio> getViewAudioList() {
		return m_audioImageList;
	}
	
	
	private void buildTitle(View wrapView,Document doc) {
		ViewText text = new ViewText(doc.title());
		text.addClass("rich_media_title");
		wrapView.addSubView(text);
	}
	
	private void buildMeta(View wrapView,Elements part) {
		View metaList = new View();
		metaList.addClass("rich_media_meta_list");
		wrapView.addSubView(metaList);

		
		Elements metas = part.select("a#js_name");
		for(Element meta: metas) {
			if(m_meta == null) {
				m_meta = meta.text();
			}
			ViewText text = new ViewText(meta.text());
			text.addClass("rich_media_meta");
			metaList.addSubView(text);
		}
	}
	
	private void buildContent(View wrapView,Elements part) {
		View content = new View();
		content.addClass("rich_media_content");
		wrapView.addSubView(content);

		Elements audios = part.select("mpvoice");
		for(Element item : audios) {
			item.addClass("_workClass");
		}
		
		Elements sections = part.select("img");
		for(Element item : sections) {
			item.addClass("_workClass");
		}
		
		Elements ps = part.select("p");
		for (Element element : ps) {
			element.addClass("_workClass");
		}
		
		Elements hrs = part.select("hr");
		for (Element element : hrs) {
			element.addClass("_workClass");
		}
		

		Elements elements = part.select("._workClass");
		
		for (Element element : elements) {
			String tagName = element.tagName();
//			System.err.println(tagName);
			if("p".equals(tagName)) {
				String text = element.text();
				text = text.trim();
				if (text.length() == 0) {
					Elements brs = element.select("br");
					
					for(Element br: brs) {
						content.addSubView(new ViewBR());
					}
				} else {
					String style = element.attr("style");
					ViewText theText = new ViewText(text);
					theText.addClass("rich_media_content rich_media_item");
					theText.addStyle(style);
					content.addSubView(theText);
				}
			} else if("img".equals(tagName)) {
				String src = element.attr("data-src");
				if (src != null) {
					src = src.trim();
					if(src.length() > 0) {
						ViewImage image = new ViewImage(src);
						image.addClass("rich_media_image");
						image.addAttr("mode=\"widthFix\"");
						content.addSubView(image);
						m_viewImageList.add(image);
					}
				}
			} else if("mpvoice".equals(tagName)) {
//				https://res.wx.qq.com/voice/getvoice?mediaid=MzIwMDA2NjAxNF81MDQ0NzEyOTA=
				String audioSrc = element.attr("voice_encode_fileid");
				if(audioSrc != null) {
					if(!audioSrc.startsWith("http")) {
						audioSrc = "https://res.wx.qq.com/voice/getvoice?mediaid=" + audioSrc;
					}
					ViewAudio audio = new ViewAudio(audioSrc);
					audio.addClass("rich_media_audio");
					content.addSubView(audio);
					m_audioImageList.add(audio);
					
					if(element.attr("name") != null) {
						String theName = element.attr("name").trim();
						try {
							if(theName.startsWith("%")) {
								theName = URLDecoder.decode(theName,"UTF-8");	
							}
						}catch(Exception ex) {
							
						}
						if(theName.length() > 0) {
							audio.addAttr("name=\"" + theName + "\"");
						}
					}
					
					if(m_meta != null) {
						audio.addAttr("author=\"" + m_meta + "\"");
					}
				}
//				<audio poster="{{poster}}" name="{{name}}" author="{{author}}" src="{{src}}" id="myAudio" controls loop></audio>
			} else if("hr".equals(tagName)) {
				content.addSubView(new ViewHR());
			}
		}
	}
	
	public String storeWxml(File file) throws Exception {
		if(m_rootView == null) {
			return null;
		}
		StringBuffer strBuff = new StringBuffer();
		m_rootView.buildWxml(strBuff);
		
		
		PrintWriter writer = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(file), "UTF-8"));
		writer.write(strBuff.toString());
		writer.flush();
		writer.close();
		
		return MD5Sum.encode32MD5(strBuff.toString());
	}
	
	public ArrayList<File> storeImage(File path,String name) {
		int index = 1;
		ArrayList<File> fileList = new ArrayList<File>();
		for(ViewImage image : m_viewImageList) {
			File imageFile = new File(path,name + "_" + index++);
			image.store(imageFile);
			fileList.add(imageFile);
		}
		return fileList;
	}

	public static void main(String[] args) {
		new Html2Wxml("https://mp.weixin.qq.com/s/97Nte7W6JVT-OiRlQPFM7Q");
	}
}
