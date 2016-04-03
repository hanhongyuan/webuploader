package com.ls;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class IOUtils {

	public static void copy(InputStream is, OutputStream os) throws IOException{
		byte[] buff = new byte[1024];
		int len = 0;
		while( (len = is.read(buff)) != -1){
			os.write(buff, 0, len);
		}
		is.close();
		os.close();
	}
}
