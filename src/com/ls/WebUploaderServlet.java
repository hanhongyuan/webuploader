package com.ls;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

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
		Part part = request.getPart("file");
		
		Map<String, String[]> map = request.getParameterMap();
		Set<String> keys = map.keySet();
		for (String key : keys) {
			String[] params = map.get(key);
			if(params.length > 1){
				for (String param : params) {
					System.out.println(key+":"+param);
				}
			}else{
				System.out.println(key+":"+params[0]);
			}
		}
		System.out.println("=========================");
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
		System.out.println(host);
		
		String fileName = getFileName(part);
		
		InputStream is = part.getInputStream();
		OutputStream os = new FileOutputStream(new File(uploadPath + fileName));
		IOUtils.copy(is, os);
		
		response.setContentType("text/html;charset=utf-8");
		PrintWriter out = response.getWriter();
		out.println("ok");
	}
	
	private String getFileName(Part part){
		String header = part.getHeader("Content-Disposition");
		String fileName = header.substring(header.indexOf("filename=\"")+10, header.length() - 1);
		return fileName;
	}

}
