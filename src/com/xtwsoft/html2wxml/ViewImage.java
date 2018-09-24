package com.xtwsoft.html2wxml;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;


public class ViewImage extends View {
	private String m_src;
	public ViewImage(String src) {
		m_src = src;
	}
	
	public void buildWxml(StringBuffer strBuff) {
		strBuff.append("<image");
		if(m_strAttr.length() > 0) {
			strBuff.append(" " + m_strAttr);
		}
		if(m_strClass.length() > 0) {
			strBuff.append(" class=\"");
			strBuff.append(m_strClass);
			strBuff.append("\"");
		}
		if(m_strStyle.length() > 0) {
			strBuff.append(" style=\"");
			strBuff.append(m_strStyle);
			strBuff.append("\"");
		}
		if(m_src.length() > 0) {
			strBuff.append(" src=\"");
			strBuff.append(m_src);
			strBuff.append("\"");
		}
		strBuff.append(">");
		strBuff.append("</image>\r\n");
	}
	
	public void store(File file) {
		try {
			BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
			URL url = new URL(m_src);
			BufferedInputStream bis = new BufferedInputStream(url.openStream());
			int ch = bis.read();
			while(ch != -1) {
				bos.write(ch);
				ch = bis.read();
			}
			bos.flush();
			bos.close();
			m_src = file.getName();
		} catch(Exception ex) {
			ex.printStackTrace();
		}
		
	}
}
