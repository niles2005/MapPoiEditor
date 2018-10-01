package com.xtwsoft.server;

import java.io.IOException;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.xtwsoft.poieditor.ImagesManager;
import com.xtwsoft.poieditor.POIManager;

@WebServlet(name = "/ServiceServlet", urlPatterns = { "/service" }, loadOnStartup=1)
public class ServiceServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	/**
	 * @see Servlet#init(ServletConfig)
	 */
	public void init(ServletConfig config) throws ServletException {
        try {
        	ServletContext servletContext = config.getServletContext();
        	String contextPath = servletContext.getContextPath();
        	String realPath = servletContext.getRealPath("");
        	ServerConfig.initInstance(contextPath,realPath);
			POIManager.initInstance();
			ImagesManager.initInstance();
        	ServiceManager.initInstance();
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doPage(request,response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doPage(request,response);
	}
	
	
	public void doPage(HttpServletRequest request, HttpServletResponse response)throws IOException, ServletException {
		request.setCharacterEncoding("UTF-8");
		response.setContentType("text/html; charset=UTF-8");
		
		ServiceManager.getInstance().doService(request, response);
	}

}
