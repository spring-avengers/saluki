package com.quancheng.saluki.oauth2.common.controller;

import org.springframework.stereotype.Controller;
import com.quancheng.saluki.oauth2.common.utils.ShiroUtils;
import com.quancheng.saluki.oauth2.system.domain.UserDO;

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