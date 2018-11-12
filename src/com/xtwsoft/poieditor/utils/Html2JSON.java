package com.xtwsoft.poieditor.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLDecoder;
import java.util.HashSet;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

/**
 * 
 * 转换html内容为JSON格式
 * 
 * @author NieLei
 *
 */
public class Html2JSON {
	private String m_meta;// used for audio
	private JSONObject m_doc = new JSONObject();
	private File m_storePath = null;
	private String m_storeName = null;
	private static HashSet<String> m_removeSumSet = new HashSet<String>();
	private String m_checkSum = null;

	static {
		m_removeSumSet.add("e8614640bd4741036fc8be9b97c7379d");// 头部 关注
		m_removeSumSet.add("93c597d2f51c98e903b988fbbb17d8c2");// 头部 关注

		m_removeSumSet.add("9cbae971509d714bfd0d9cc12b5ab0ce");// 尾部 二维码
		m_removeSumSet.add("d09c2bdc7d1381e3e3935a8986d6d8f4");// 尾部 二维码
		m_removeSumSet.add("ea3c5525975cf97f42b278f29de15355");// 尾部 二维码
		m_removeSumSet.add("23099586f25a8292115c7f3e98bd9be9");// 尾部 二维码
	}

	public Html2JSON(String strUrl, File path, String name) {
		try {
			m_storePath = path;
			m_storeName = name;
			// 在tomcat中运行，需jdk8之上，并配置SSL，否则会报错"Could not generate DH keypair"
			Document doc = Jsoup.connect(strUrl).get();

			buildTitle(m_doc, doc);

			buildMeta(m_doc, doc.select("div#meta_content"));

			buildContent(m_doc, doc.select("div#js_content"));

			if (m_meta != null) {
				String from = m_meta;
				if (from.endsWith("发布")) {
					from = from.substring(0, from.length() - 2);
				}
				m_doc.put("from", from);
			}

			this.storeJSON();

			m_checkSum = MD5Sum.encode32MD5(m_doc.toJSONString());
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public String getCheckSum() {
		return m_checkSum;
	}

	private void buildTitle(JSONObject jsonDoc, Document doc) {
		String title = doc.title();
		int pos = title.indexOf("||");
		if (pos != -1) {
			title = title.substring(pos + 2);
		}
		jsonDoc.put("title", title);
	}

	private void buildMeta(JSONObject jsonDoc, Elements part) {
		Elements metas = part.select("a#js_name");
		for (Element meta : metas) {
			if (m_meta == null) {
				m_meta = meta.text();
			}
		}
	}

	private void buildContent(JSONObject jsonDoc, Elements part) {
		Elements audios = part.select("mpvoice");
		for (Element item : audios) {
			item.addClass("_workClass");
		}

		Elements sections = part.select("img");
		for (Element item : sections) {
			item.addClass("_workClass");
		}

		Elements videos = part.select("iframe.video_iframe");
		for (Element item : videos) {
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

		JSONArray contentArray = new JSONArray();
		jsonDoc.put("contents", contentArray);

		Elements elements = part.select("._workClass");

		for (Element element : elements) {
			String tagName = element.tagName();
			// System.err.println(tagName);
			if ("p".equals(tagName)) {
				String text = element.text();
				text = text.trim();
				if (text.length() == 0) {
					Elements brs = element.select("br");

					for (Element br : brs) {
						JSONObject obj = new JSONObject();
						obj.put("tag", "br");
						contentArray.add(obj);
						if (element.parent().tagName().equals("section")) {
							obj.put("wrapStyle", element.parent().attr("style"));
						}
					}
				} else {
					JSONObject obj = new JSONObject();
					obj.put("tag", "p");
					contentArray.add(obj);

					String style = element.attr("style");
					obj.put("style", style);
					obj.put("text", text);
					if (element.parent().tagName().equals("section")) {
						obj.put("wrapStyle", element.parent().attr("style"));
					}
				}
			} else if ("iframe".equals(tagName)) {
				String src = element.attr("data-src");
				if (src != null) {
					src = src.trim();
					if (src.length() > 0) {
						int pos = src.indexOf("vid=");
						if (pos > 0) {
							String vid = null;
							int pos1 = src.indexOf("&", pos + 1);
							if (pos1 > pos) {
								vid = src.substring(pos + 4, pos1);
							} else {
								vid = src.substring(pos + 4);
							}
							if (vid != null) {
								String fileName = loadVideo(vid);
								if (fileName != null) {
									JSONObject obj = new JSONObject();
									contentArray.add(obj);
									obj.put("tag", "video");
									obj.put("src", fileName);
								}
							}
						}
					}
				}
			} else if ("img".equals(tagName)) {
				String src = element.attr("data-src");
				if (src != null) {
					src = src.trim();
					if (src.length() > 0) {
						int pos = src.lastIndexOf("=");
						String type = "jpg";// default image type
						int typeLen = src.length() - pos;
						if (typeLen <= 5) {
							type = src.substring(pos + 1);
						}
						String fileName = storeFile(element,
								element.attr("data-src"), this.m_storePath,
								type, true);
						if (fileName != null) {
							JSONObject obj = new JSONObject();
							contentArray.add(obj);
							obj.put("tag", "image");
							obj.put("src", fileName);
							String style = element.attr("style");
							style = style.replaceAll("width: auto;", "");// 解决图片高宽为0问题
							style = style.replaceAll("height: auto;", "");
							obj.put("style", style);
							if (element.parent().tagName().equals("section")) {
								obj.put("wrapStyle",
										element.parent().attr("style"));
							}
						}
					}
				}
			} else if ("mpvoice".equals(tagName)) {
				String audioSrc = element.attr("voice_encode_fileid");
				if (audioSrc != null) {
					if (!audioSrc.startsWith("http")) {
						audioSrc = "https://res.wx.qq.com/voice/getvoice?mediaid="
								+ audioSrc;
					}
					String fileName = storeFile(element, audioSrc,
							this.m_storePath, "mp3", false);
					if (fileName != null) {
						JSONObject obj = new JSONObject();
						contentArray.add(obj);
						obj.put("tag", "audio");
						obj.put("src", fileName);

						if (element.attr("name") != null) {
							String theName = element.attr("name").trim();
							try {
								if (theName.startsWith("%")) {
									theName = URLDecoder.decode(theName,
											"UTF-8");
								}
							} catch (Exception ex) {

							}
							if (theName.length() > 0) {
								obj.put("name", theName);
							}
						}

						if (m_meta != null) {
							obj.put("author", m_meta);
						}
					}
				}
			} else if ("hr".equals(tagName)) {
				JSONObject obj = new JSONObject();
				obj.put("tag", "hr");
				contentArray.add(obj);
			}
		}
	}

	public void storeJSON() throws Exception {
		PrintWriter writer = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(new File(m_storePath, this.m_storeName
						+ ".json")), "UTF-8"));
		writer.write(m_doc.toJSONString());
		writer.flush();
		writer.close();
	}

	public String storeFile(Element element, String src, File path,
			String type, boolean doReduce) {
		String sum = null;
		try {
			URL url = new URL(src);
			BufferedInputStream bis = new BufferedInputStream(url.openStream());

			byte[] buff = new byte[4096];
			// 使用int num = bis.read(buff)方式，num可能不是4096
			int ch = bis.read();
			int index = 0;
			while (ch != -1) {
				buff[index] = (byte) ch;
				index++;
				if (index >= 4096) {
					break;
				}
				ch = bis.read();
			}
			if (index == 4096) {
				sum = MD5Sum.getByteArrayMD5Sum(buff);
				if (m_removeSumSet.contains(sum)) {// 是目标文件
					element.parent().remove();
					try {
						bis.close();
					} catch (Exception ex) {
					}
					return null;
				}
			} else {
				sum = MD5Sum.getByteArrayMD5Sum(buff, index);
			}

			File file = new File(path, sum + "." + type);
			BufferedOutputStream bos = new BufferedOutputStream(
					new FileOutputStream(file));
			bos.write(buff, 0, index);
			bos.flush();
			int num = bis.read(buff);
			while (num > 0) {
				bos.write(buff, 0, num);
				num = bis.read(buff);
			}
			bos.flush();
			bos.close();
			if (doReduce) {
				Utils.reduceImageFile(file, type, 500);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return sum + "." + type;
	}

	public boolean storeVideoFile(String src, File file) {
		try {
			URL url = new URL(src);
			BufferedInputStream bis = new BufferedInputStream(url.openStream());

			byte[] buff = new byte[4096];
			BufferedOutputStream bos = new BufferedOutputStream(
					new FileOutputStream(file));
			int num = bis.read(buff);
			while (num > 0) {
				bos.write(buff, 0, num);
				num = bis.read(buff);
			}
			bos.flush();
			bos.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return true;
	}

	/**
	 * load wx's video by vid
	 * 
	 * @param vid
	 * @return video file name
	 */
	public String loadVideo(String vid) {
		try {
			String strUrl = "https://h5vv.video.qq.com/getinfo?platform=11001&otype=json&guid="
					+ Guid.build24Guid() + "&vids=" + vid;
			// System.err.println(strUrl);

			URL url = new URL(strUrl);
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					url.openStream()));
			String line = reader.readLine();
			reader.close();
			int pos1 = line.indexOf("{");
			int pos2 = line.lastIndexOf("}");
			if (pos1 >= 0 && pos2 > pos1) {
				String strJson = line.substring(pos1, pos2 + 1);
				// System.err.println(strJson);
				JSONObject json = JSON.parseObject(strJson);
				JSONObject vi0 = json.getJSONObject("vl").getJSONArray("vi")
						.getJSONObject(0);
				String fn = vi0.getString("fn");
				String type = ".mp4";
				int pos = fn.lastIndexOf(".");
				if (pos > 0) {
					type = fn.substring(pos);
				}
				String vkey = vi0.getString("fvkey");
				// System.err.println(vkey.length());//must be 240
				String path = vi0.getJSONObject("ul").getJSONArray("ui")
						.getJSONObject(0).getString("url");

				String videoUrl = path + fn + "?vkey=" + vkey;
				String fileName = vid + type;
				this.storeVideoFile(videoUrl, new File(m_storePath, vid + type));
				// System.err.println(videoUrl);
				return fileName;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}

	public static void main(String[] args) {
		File path = new File("./bak/html2json");
		// String src = "https://mp.weixin.qq.com/s/0rzS-xTjzL_9L3Le6D39ow";
		String src = "https://mp.weixin.qq.com/s/Ns8XdHc9TqTpv1hJrovhfw";
		// String src = "https://mp.weixin.qq.com/s/JA7Btpdx1SqtD8aL82z8wQ";
		new Html2JSON(src, path, "zzzz");
	}
}
