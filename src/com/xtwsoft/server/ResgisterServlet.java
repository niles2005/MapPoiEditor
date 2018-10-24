package com.xtwsoft.server;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.alibaba.fastjson.JSONObject;
import com.xtwsoft.poieditor.ImagesManager;
import com.xtwsoft.poieditor.POIManager;

@WebServlet(name = "/ResgisterServlet", urlPatterns = { "/register" }, loadOnStartup = 1)
public class ResgisterServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	public void init(ServletConfig config) throws ServletException {
		try {
			ServletContext servletContext = config.getServletContext();
			String contextPath = servletContext.getContextPath();
			String realPath = servletContext.getRealPath("");
			ServerConfig.initInstance(contextPath, realPath);
			POIManager.initInstance();
			ImagesManager.initInstance();
			ServiceManager.initInstance();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		doPage(request, response);
	}

	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		doPage(request, response);
	}

	public void doPage(HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		request.setCharacterEncoding("UTF-8");
		response.setContentType("text/html; charset=UTF-8");
		String action = request.getParameter("action");
		if (action != null) {
			if (action.equals("usersalt")) {
				String strUser = request.getParameter("user");
				String salt = UsersManager.getInstance().getUserSalt(strUser);
				ServletOutputStream sos = response.getOutputStream();
				ServiceReturn ret = new ServiceReturn();
				
				if(salt != null) {
					ret.setSuccessData(salt);
				} else {
					ret.setError("user is not exist!");
				}
				sos.write(ret.toString().getBytes("UTF-8"));
			} else if (action.equals("login")) {
				String account = request.getParameter("account");
				String password = request.getParameter("password");

				ServletOutputStream sos = response.getOutputStream();
				ServiceReturn ret = new ServiceReturn();
				if (UsersManager.getInstance().checkUserPass(account, password)) {
					request.getSession(true);
					ret.setSuccess("login success!");
				} else {
					ret.setError("login failed!");
				}
				sos.write(ret.toString().getBytes("UTF-8"));
			} else if (action.equals("logout")) {
				HttpSession session = request.getSession();
				session.invalidate();
				response.sendRedirect(PageFilter.loginPage);
			}
		}
	}
}
