package com.xtwsoft.mapPoiEditor;

import javax.servlet.http.HttpServletRequest;

import com.xtwsoft.server.Service;
import com.xtwsoft.server.ServiceReturn;

/**
 * 保存所有的数据到文件。
 * @author NieLei
 *
 */
public class SaveAllService extends Service {
	public SaveAllService() {
		super("saveall");
	}
	
	public void work(ServiceReturn ret,HttpServletRequest request) {
		EditorManager.getInstance().saveDatasToFile();
		ret.setSuccess("save all data success!");
	}
}
