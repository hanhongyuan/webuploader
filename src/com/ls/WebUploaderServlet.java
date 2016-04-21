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
import com.ls.util.FileUtil;
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
		//2.查询
		//2.1查询文件是否存在
		if("md5".equals(type)){
			int count = uploadFileDao.getCountByMd5(md5);
			out.print( count > 0 ? true : false);
		}
		//2.2查询文件的子文件
		else if("parentMd5".equals(type)){
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
		String fileName = FileUtil.getFileNameFromPart(part);
		String newFileName = FileUtil.getRandomFileName(fileName);
		File file = new File(uploadPath + newFileName);
		
		//上传文件
		InputStream is = part.getInputStream();
		OutputStream os = new FileOutputStream(file);
		IOUtils.copy(is, os);
		IOUtils.close(is, os);
		
		//保存上传文件到数据库
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
		
		//判断文件是否上传完整
		if(chunks > 1){
			int count = uploadFileDao.getCountByParentMd5(parentMd5);
			if(chunks == count){
				FileUtil.merge(parentMd5, uploadPath);
			}
		}
		
		out.println("ok");
	}
	
	private int getIntParam(HttpServletRequest request, String name, int defVal){
		int retVal = 0;
		try {
			String param = request.getParameter(name);
			retVal = Integer.parseInt(param);
		} catch (Exception e) {
			retVal = defVal;
		}
		return retVal;
	}
	
}
