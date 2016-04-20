package com.ls.util;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class Installer {

	public void install(){
		//1.创建表
		StringBuilder sb = new StringBuilder();
		sb.append("create table if not exists uploader_file(");
		sb.append("id int primary key auto_increment,");
		sb.append("md5 varchar(32),"                  );
		sb.append("parentMd5 varchar(32),"            );
		sb.append("fileName varchar(200),"            );
		sb.append("newFileName varchar(100),"         );
		sb.append("fileSize long,"                    );
		sb.append("chunk int,"                        );
		sb.append("chunks int,"                       );
		sb.append("isGood bit"                       );
		sb.append(")");
		
		try {
			Connection conn = DBUtil.getConn();
			PreparedStatement ps = conn.prepareStatement(sb.toString());
			ps.executeUpdate();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
}
