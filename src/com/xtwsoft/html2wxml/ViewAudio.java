package com.xtwsoft.html2wxml;

public class ViewAudio extends View {
	private String m_src;
	public ViewAudio(String src) {
		m_src = src;
		this.addAttr("controls loop");
	}
	
	public void buildWxml(StringBuffer strBuff) {
		strBuff.append("<audio");
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
		strBuff.append("</audio>\r\n");
	}
}
