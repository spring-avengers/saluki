package com.quancheng.saluki.gateway.portal.common;

import org.apache.shiro.authz.AuthorizationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 异常处理器
 * 
 */
@RestControllerAdvice
public class BDExceptionHandler {
	private Logger logger = LoggerFactory.getLogger(getClass());

	/**
	 * 自定义异常
	 */
	@ExceptionHandler(BDException.class)
	public CommonResponse handleBDException(BDException e) {
		CommonResponse r = new CommonResponse();
		r.put("code", e.getCode());
		r.put("msg", e.getMessage());

		return r;
	}

	@ExceptionHandler(DuplicateKeyException.class)
	public CommonResponse handleDuplicateKeyException(DuplicateKeyException e) {
		logger.error(e.getMessage(), e);
		return CommonResponse.error("数据库中已存在该记录");
	}

	@ExceptionHandler(org.springframework.web.servlet.NoHandlerFoundException.class)
	public CommonResponse noHandlerFoundException(org.springframework.web.servlet.NoHandlerFoundException e) {
		logger.error(e.getMessage(), e);
		return CommonResponse.error("没找找到页面");
	}

	@ExceptionHandler(AuthorizationException.class)
	public CommonResponse handleAuthorizationException(AuthorizationException e) {
		logger.error(e.getMessage(), e);
		return CommonResponse.error("未授权");
	}

	@ExceptionHandler(Exception.class)
	public CommonResponse handleException(Exception e) {
		logger.error(e.getMessage(), e);
		return CommonResponse.error("服务器错误，请联系管理员");
	}
}
