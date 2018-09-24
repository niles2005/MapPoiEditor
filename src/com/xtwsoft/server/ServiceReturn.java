package com.xtwsoft.server;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

/**
 * 
 * 返回的服务数据的封装。
 * 只需调用SetError，或SetSuccess。不能不调用，否则无返回码retCode。（-1:错误，>=0:正确）
 * @author NieLei
 *
 */
public class ServiceReturn {
	private JSONObject m_ret = new JSONObject();
	
	public ServiceReturn() {
	}
	
	public String toString() {
		return m_ret.toJSONString();
	}
	
	public void setError(String message) {
		m_ret.put("retCode", -1);
		m_ret.put("message", message);
	}
	
	public void setSuccess(String message) {
		m_ret.put("retCode", 0);
		m_ret.put("message", message);
	}

	public void setSuccess(JSONObject data) {
		m_ret.put("retCode", 0);
		m_ret.put("data", data);
	}
	
	public void setSuccess(JSONArray data) {
		m_ret.put("retCode", 0);
		m_ret.put("data", data);
	}
	
	
}
