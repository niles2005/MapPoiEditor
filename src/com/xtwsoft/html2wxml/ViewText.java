package com.xtwsoft.html2wxml;

public class ViewText extends View {
	private String m_text = "";
	public ViewText(String text) {
		m_text += text;
	}
	
	public void appendText(String text) {
		m_text += text;
	}

	public void buildWxml(StringBuffer strBuff) {
		strBuff.append("<view");
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
		strBuff.append(">");
		strBuff.append(m_text);
		strBuff.append("</view>\r\n");
	}
}

