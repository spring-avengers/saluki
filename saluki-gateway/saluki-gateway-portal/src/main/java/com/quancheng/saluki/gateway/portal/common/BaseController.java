package com.quancheng.saluki.gateway.portal.common;

import org.springframework.stereotype.Controller;

import com.quancheng.saluki.gateway.portal.system.domain.UserDO;
import com.quancheng.saluki.gateway.portal.utils.ShiroUtils;

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
