package com.ls;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import org.apache.tomcat.util.http.fileupload.FileUtils;

import com.ls.dao.UploadFileDao;
import com.ls.util.DateUtil;
import com.ls.util.IOUtils;
import com.ls.util.Installer;

/**
 * 测试webuploader上传
 */
public class WebUploaderServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
    
	private String uploadPath;
	private UploadFileDao uploadFileDao = new UploadFileDao();
	
	@Override
	public void init() throws ServletException {
		uploadPath = (String) super.getServletContext().getInitParameter("upload");
		uploadPath = (uploadPath + "\\").replace("\\\\", "\\");
		
		Installer installer = new Installer();
		installer.install();
	}
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		//1.获取也页面参数
		String type = request.getParameter("type");
		String md5 = request.getParameter("md5");
		String parentMd5 = request.getParameter("parentMd5");
		
		response.setContentType("text/html;charset=utf-8");
		PrintWriter out = response.getWriter();
		//2.
		if("md5".equals(type)){
			int count = uploadFileDao.getCountByMd5(md5);
			out.print( count > 0 ? true : false);
		}else if("parentMd5".equals(type)){
			List<UploadFile> uploadFiles = uploadFileDao.getListByParentMd5(parentMd5);
			StringBuilder sb = new StringBuilder();
			sb.append("[");
			if(uploadFiles != null){
				for (int i = 0; i < uploadFiles.size(); i++) {
					sb.append("\"").append(uploadFiles.get(i).getMd5()).append("\"");
					if(i != uploadFiles.size() - 1){
						sb.append(",");
					}
				}
			}
			sb.append("]");
			out.print( sb.toString());
			System.out.println("----------------");
		}
		
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/html;charset=utf-8");
		PrintWriter out = response.getWriter();
		
		//获取参数
		Part part = request.getPart("file");
		int chunks = getIntParam(request, "chunks", 1);//被分文件的总块数
		int chunk = getIntParam(request, "chunk", 1);//当前的块号
		String parentMd5 = request.getParameter("parentMd5");//当前的块号
		String md5 = request.getParameter("md5");//当前的块号
		
		//构建文件名
		String fileName = getFileName(part);
		String newFileName = fileName + (chunks == 1 ? "" : "_"+chunk);
		File file = new File(uploadPath + newFileName);
		
		if(file.exists()){
			merge(fileName, chunks);
			out.println("ok");
			return ;
		}
		
		//保存文件
		InputStream is = part.getInputStream();
		OutputStream os = new FileOutputStream(file);
		IOUtils.copy(is, os);
		IOUtils.close(is, os);
		
		UploadFile uploadFile = new UploadFile();
		uploadFile.setChunk(chunk);
		uploadFile.setChunks(chunks);
		uploadFile.setFileName(fileName);
		uploadFile.setFileSize(0);
		uploadFile.setGood(chunks == 1 ? true : false);;
		uploadFile.setMd5(md5);
		uploadFile.setNewFileName(newFileName);
		uploadFile.setParentMd5(parentMd5);
		uploadFileDao.add(uploadFile);
		
		out.println("ok");
	}
	
	private int getIntParam(HttpServletRequest request, String name, int defVal){
		String str = request.getParameter(name);
		return getIntParam(str, defVal);
	}
	
	private int getIntParam(String str, int defVal){
		int retVal = 0;
		try {
			retVal = Integer.parseInt(str);
		} catch (Exception e) {
			retVal = defVal;
		}
		return retVal;
	}
	
	private String getFileName(Part part){
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
	 * 判断文件是否上传完整
	 * @return
	 */
	private boolean isUploadComplete(String fileName, int chunks){
		boolean contains = false;
		
		File dir = new File(uploadPath);
		if(dir.isDirectory()){
			String[] files = dir.list();
			if(files != null && files.length > 0){
				
				for(int i = 0; i < chunks; i++){
					
					contains = false;
					String newFileName = fileName + "_" + i;
					for(int j = 0; j < files.length; j++){
						if(newFileName.equals(files[j])){
							contains = true;
							break;
						}
					}
					
					if(!contains){
						break;
					}
				}
			}
		}
		
		return contains;
	}
	
	/**
	 * 将文件合并
	 * @param fileName
	 * @param chunks
	 * @throws IOException
	 */
	private void merge(String fileName, int chunks) throws IOException{
		if(chunks <= 1){
			return ;
		}
		
		if(!isUploadComplete(fileName, chunks)){
			System.out.println("没完整");
			return;
		}
		
		File mergeFile = new File(uploadPath + fileName);
		//如果存在，说明文件正在合并，或者文件合并完成
		if(mergeFile.exists()){
			return ;
		}
		System.out.println("合并中。。。");
		
		//合并文件
		OutputStream os = new FileOutputStream(mergeFile);
		InputStream is = null;
		
		File file = null;
		for(int i = 0;i<chunks;i++){
			file = new File(uploadPath + fileName + "_" + i);
			is = new FileInputStream(file);
			IOUtils.copy(is, os);
			IOUtils.close(is, null);
		}
		IOUtils.close(null, os);
	}
}
