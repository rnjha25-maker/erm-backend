package com.erm.filter;

import java.io.IOException;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;

public class RequestContextFilter implements Filter {

	private static final ThreadLocal<String> currentIp = new ThreadLocal<>();

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {

		String ipAddress = request.getRemoteAddr();
		System.out.println("IP add " + ipAddress);
		currentIp.set(ipAddress);
		chain.doFilter(request, response);

	}

	@Override
	public void destroy() {
	}

	public static String getCurrentIp() {
		return currentIp.get();
	}

}
