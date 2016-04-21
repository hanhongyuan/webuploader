package com.ls.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.List;

import javax.servlet.http.Part;

import com.ls.UploadFile;
import com.ls.dao.UploadFileDao;

public class FileUtil {

	public static String getFileNameFromPart(Part part){
		String header = part.getHeader("Content-Disposition");
		String fileName = header.substring(header.indexOf("filename=\"")+10, header.length() - 1);
		try {
			fileName = new String(fileName.getBytes(),"utf-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return fileName;
	}
	
	/**
	 * 获取随机文件名
	 * @param fileName
	 * @return
	 */
	public static String getRandomFileName(String fileName){
		String ext = getFileExt(fileName);
		String name = System.nanoTime()+"."+ext;
		return name;
	}
	
	public static String getFileExt(String fileName){
		String ext = "";
		int pos = fileName.lastIndexOf(".");
		if(pos > -1){
			ext = fileName.substring(pos + 1);
		}
		return ext;
	}

	/**
	 * 合并文件
	 * @param files
	 */
	public static void merge(String parentMd5, String uploadPath) {
		OutputStream os = null;
		InputStream is = null;
		try {
			UploadFileDao uploadFileDao = new UploadFileDao();
			//获取文件
			List<UploadFile> files = uploadFileDao.getListByParentMd5(parentMd5);
			
			if(files != null && !files.isEmpty()){
				//1.合并文件
				String newFileName = getRandomFileName(files.get(0).getFileName());//获取新文件名
				os = new FileOutputStream(uploadPath + newFileName);
				for (UploadFile file : files) {
					File tempFile = new File(uploadPath + file.getNewFileName());
					is = new FileInputStream(tempFile);
					IOUtils.copy(is, os);
					IOUtils.close(is, null);
					//删除文件
					if(tempFile.exists()){
						tempFile.delete();
					}
				}
				
				//构建保存对象
				UploadFile uploadFile = new UploadFile();
				uploadFile.setChunk(-1);
				uploadFile.setChunks(files.get(0).getChunks());
				uploadFile.setFileName(files.get(0).getFileName());
				uploadFile.setFileSize(0);
				uploadFile.setGood(true);;
				uploadFile.setMd5(files.get(0).getParentMd5());
				uploadFile.setNewFileName(newFileName);
				uploadFile.setParentMd5("");
				//添加
				uploadFileDao.add(uploadFile);
				//删除临时文件
				uploadFileDao.deleteByParentMd5(parentMd5);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}finally{
			IOUtils.close(is, os);
		}
		return ;
	}
}
