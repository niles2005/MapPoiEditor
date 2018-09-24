package com.xtwsoft.html2wxml;

import java.util.ArrayList;


public class View {
	protected String m_strAttr = "";
	protected String m_strClass = "";
	protected String m_strStyle = "";
	private ArrayList<View> m_viewList = new ArrayList<View>();
	public View() {
	}
	
	public void addSubView(View view) {
		m_viewList.add(view);
	}
	
	public void addAttr(String strAttr) {
		if(strAttr.length() > 0) {
			strAttr += " ";
		}
		m_strAttr += strAttr;
	}
	
	
	public void addClass(String strClass) {
		if(m_strClass.length() > 0) {
			m_strClass += " ";
		}
		m_strClass += strClass;
	}
	
	public void addStyle(String strStyle) {
		if(strStyle.length() > 0) {
			strStyle += " ";
		}
		m_strStyle += strStyle;
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
		strBuff.append(">\r\n");
		for(View view:m_viewList) {
			view.buildWxml(strBuff);
		}
		strBuff.append("</view>\r\n");
	}
	
}
