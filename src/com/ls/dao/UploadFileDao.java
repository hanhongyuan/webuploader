package com.ls.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.ls.UploadFile;
import com.ls.util.DBUtil;


public class UploadFileDao {

	public final String curTable = "uploader_file";
	
	private List<UploadFile> getRS(String sql, Object...params){
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		List<UploadFile> items = null;
		try {
			conn = DBUtil.getConn();
			ps = conn.prepareStatement(sql);
			if(params != null && params.length > 0){
				for (int i = 0; i < params.length; i++) {
					ps.setObject(i+1, params[i]);
				}
			}
			rs = ps.executeQuery();
			
			items = new ArrayList<UploadFile>();
			while(rs.next()){
				UploadFile item = new UploadFile();
				item.setChunk(rs.getInt("chunk"));
				item.setChunks(rs.getInt("chunks"));
				item.setFileName(rs.getString("fileName"));
				item.setFileSize(rs.getInt("fileSize"));
				item.setGood(rs.getBoolean("isGood"));
				item.setMd5(rs.getString("md5"));
				item.setNewFileName(rs.getString("newFileName"));
				item.setParentMd5(rs.getString("parentMd5"));
				items.add(item);
			}
		} catch (SQLException e) {
			System.err.println(sql); 
			e.printStackTrace();
		}finally{
			DBUtil.close(conn, ps, rs);
		}
		return items;
	}
	
	private int getCount(String sql, Object...params){
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		int count = 0;
		try {
			conn = DBUtil.getConn();
			ps = conn.prepareStatement(sql);
			if(params != null && params.length > 0){
				for (int i = 0; i < params.length; i++) {
					ps.setObject(i+1, params[i]);
				}
			}
			rs = ps.executeQuery();
			if(rs.next()){
				count = rs.getInt(1);
			}
		} catch (SQLException e) {
			System.err.println(sql); 
			e.printStackTrace();
		}finally{
			DBUtil.close(conn, ps, rs);
		}
		return count;
	}
	
	private int execute(String sql, Object...params){
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		int count = 0;
		try {
			conn = DBUtil.getConn();
			ps = conn.prepareStatement(sql);
			if(params != null && params.length > 0){
				for (int i = 0; i < params.length; i++) {
					ps.setObject(i+1, params[i]);
				}
			}
			count = ps.executeUpdate();
		} catch (SQLException e) {
			System.err.println(sql); 
			e.printStackTrace();
		}finally{
			DBUtil.close(conn, ps, rs);
		}
		return count;
	}
	
	public List<UploadFile> getListByParentMd5(String parentMd5){
		String sql = "select * from "+curTable+" where parentMd5 = ? order by chunk asc";
		return getRS(sql, parentMd5);
	}
	
	public int getCountByMd5(String md5){
		String sql = "select count(*) from "+curTable+" where md5 = ?";
		return getCount(sql, md5);
	}
	
	public int getCountByParentMd5(String parentMd5){
		String sql = "select count(*) from "+curTable+" where parentMd5 = ?";
		return getCount(sql, parentMd5);
	}
	
	public void add(UploadFile uploadFile){
		String sql = "insert into "+curTable+" (md5, parentMd5, fileName, newFileName, fileSize, chunk, chunks, isGood) values(?,?,?,?,?,?,?,?)";
		
		execute(sql, uploadFile.getMd5(), uploadFile.getParentMd5(), uploadFile.getFileName(), uploadFile.getNewFileName(),
				uploadFile.getFileSize(), uploadFile.getChunk(), uploadFile.getChunks(), uploadFile.isGood());
	}

	public void deleteByParentMd5(String parentMd5) {
		String sql = "delete from "+curTable+" where parentMd5 = ?";
		this.execute(sql, parentMd5);
	}
}
