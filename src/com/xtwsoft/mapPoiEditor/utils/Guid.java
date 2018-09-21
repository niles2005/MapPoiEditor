package com.xtwsoft.mapPoiEditor.utils;

import java.util.UUID;

public class Guid {
	//产生的uuid字符串太长，不利于用于文件名等处理，再经过md5 16位处理
	public static String build16Guid() {
        UUID uuid = UUID.randomUUID();
        String randomUUIDString = uuid.toString();
        return MD5Sum.encode16MD5(randomUUIDString);
	}
	
    public static void main(String[] args) {
        UUID uuid = UUID.randomUUID();
        String randomUUIDString = uuid.toString();
 
        System.out.println("Random UUID String = " + randomUUIDString);
        System.out.println("UUID version       = " + uuid.version());
        System.out.println("UUID variant       = " + uuid.variant());
    }
}
