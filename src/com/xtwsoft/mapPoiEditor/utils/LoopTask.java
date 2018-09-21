package com.xtwsoft.mapPoiEditor.utils;

import java.util.Timer;
import java.util.TimerTask;

import com.xtwsoft.mapPoiEditor.EditorManager;

//避免每次更新都保存，设置为一定时间后保存(5分钟)
public class LoopTask extends TimerTask {
	public LoopTask() {
	}
	
	private static int Index = 0;
	public void run() {
		if(Index > 0) {//程序启动后第一次不做数据保存
			EditorManager.getInstance().saveDatasToFile();
		}
		Index++;
	}

	//5min  60 * 5 * 1000 = 300000
	public static void startTimer() {
		Timer timer = new Timer();
        timer.schedule(new LoopTask(),
               0,        
               60 * 5 * 1000);  
		
	}
}
