package com.ls.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DBUtil {

	private final static String url = "jdbc:mysql://127.0.0.1:3306/test";
	private final static String driver = "com.mysql.jdbc.Driver";
	private final static String username = "root";
	private final static String password = "123456";
	
	public static Connection getConn(){
		Connection conn = null;
		try {
			Class.forName(driver);
			conn = DriverManager.getConnection(url, username, password);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return conn;
	}
	
	public static void close(Connection conn, Statement stmt, ResultSet rs){
			try {
				if(rs != null){
					rs.close();
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}finally{
				try {
					if(stmt != null){
						stmt.close();
					}
				} catch (Exception e2) {
					e2.printStackTrace();
				}finally{
					try {
						if(rs != null){
							rs.close();
						}
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
			}
	}
}
