package com.xtwsoft.server;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.xtwsoft.poieditor.ImagesManager;
import com.xtwsoft.poieditor.POIManager;
import com.xtwsoft.poieditor.utils.AccessStatManager;

@WebFilter(urlPatterns = "/*")
public class PageFilter implements Filter {
	public static String loginPage;
	public static String editPage;

	public PageFilter() {
	}

	public void destroy() {
	}

	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		HttpServletRequest httpRequest = (HttpServletRequest) request;

		String url = httpRequest.getServletPath();
		// System.err.println(url);
		if (url.startsWith("/datas/")) {
			chain.doFilter(request, response);
		} else if (url.startsWith("/images/")) {
			chain.doFilter(request, response);
		} else if (url.startsWith("/lib/")) {
			chain.doFilter(request, response);
		} else if (url.startsWith("/css/")) {
			chain.doFilter(request, response);
		} else if (url.equals("/login.html")) {
			chain.doFilter(request, response);
		} else if (url.equals("/register")) {
			chain.doFilter(request, response);
		} else {
			HttpSession session = httpRequest.getSession(false);
			if (session == null) {
				((HttpServletResponse) response).sendRedirect(loginPage);
				return;
			}
			chain.doFilter(request, response);
		}
	}

	public void init(FilterConfig fConfig) throws ServletException {
		try {
			ServletContext servletContext = fConfig.getServletContext();
			String contextPath = servletContext.getContextPath();
			String realPath = servletContext.getRealPath("");
			System.out.println("server init at:" + contextPath);
			ServerConfig.initInstance(contextPath, realPath);
			POIManager.initInstance();
			ImagesManager.initInstance();
			ServiceManager.initInstance();
			AccessStatManager.initInstance(ServerConfig.getInstance()
					.getAppPath());
			UsersManager.initInstance(ServerConfig.getInstance()
					.getWEBINFPath());
			loginPage = contextPath + "/login.html";
			editPage = contextPath + "/edit.html";
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

}
