package com.quancheng.saluki.oauth2.system.controller;

import org.springframework.stereotype.Controller;

import com.quancheng.saluki.oauth2.system.domain.UserDO;
import com.quancheng.saluki.oauth2.utils.ShiroUtils;

@Controller
public class BaseController {
	public UserDO getUser() {
		return ShiroUtils.getUser();
	}

	public Long getUserId() {
		return getUser().getUserId();
	}

	public String getUsername() {
		return getUser().getUsername();
	}
}