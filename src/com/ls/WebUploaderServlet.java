package com.ls;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.text.ParseException;
import java.util.Date;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import org.apache.tomcat.util.http.fileupload.FileUtils;

/**
 * 测试webuploader上传
 */
public class WebUploaderServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
    
	private String uploadPath;
	
	@Override
	public void init() throws ServletException {
		uploadPath = (String) super.getServletContext().getInitParameter("upload");
		uploadPath = (uploadPath + "\\").replace("\\\\", "\\");
	}
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/html;charset=utf-8");
		PrintWriter out = response.getWriter();
		Long start = System.currentTimeMillis();
		Part part = request.getPart("file");
		
		Map<String, String[]> map = request.getParameterMap();
		Set<String> keys = map.keySet();
		for (String key : keys) {
			String[] params = map.get(key);
			if(params.length > 1){
				for (String param : params) {
					//System.out.println(key+":"+param);
				}
			}else{
				//System.out.println(key+":"+params[0]);
			}
		}
		//System.out.println("=========================");
		//获取参数
		String host = request.getRemoteHost();
		String name = request.getParameter("name");
		String chunks = request.getParameter("chunks");//被分文件的总块数
		String chunk = request.getParameter("chunk");//当前的块号
		String lastModifiedDateStr = request.getParameter("lastModifiedDate");
		lastModifiedDateStr = lastModifiedDateStr.substring(0, lastModifiedDateStr.indexOf("(") - 1);
		Date date = null;
		try {
			date = DateUtil.getDateByTimeZone(lastModifiedDateStr);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		//System.out.println(host);
		
		//构建文件名
		String fileName = getFileName(part);
		File file = new File(uploadPath + fileName + "_" + chunk);
		
		//判断文件是否已经上传过了1460558020047    1460558076206
		//		          1460558023556    1460558082417
		if(file.exists()){
			merge(fileName, Integer.parseInt(chunks));
			System.out.println(System.currentTimeMillis() - start);
			out.println("ok");
			return ;
		}
		
		//保存文件
		InputStream is = part.getInputStream();
		OutputStream os = new FileOutputStream(file);
		IOUtils.copy(is, os);
		System.out.println(System.currentTimeMillis());
		out.println("ok");
	}
	
	private String getFileName(Part part){
		String header = part.getHeader("Content-Disposition");
		String fileName = header.substring(header.indexOf("filename=\"")+10, header.length() - 1);
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
