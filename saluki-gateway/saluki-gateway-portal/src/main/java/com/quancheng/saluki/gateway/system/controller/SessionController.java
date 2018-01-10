package com.quancheng.saluki.gateway.system.controller;

import java.util.Collection;
import java.util.List;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.session.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.quancheng.saluki.gateway.common.CommonResponse;
import com.quancheng.saluki.gateway.system.domain.UserOnline;
import com.quancheng.saluki.gateway.system.service.SessionService;

@RequestMapping("/sys/online")
@Controller
public class SessionController {
  @Autowired
  SessionService sessionService;

  @GetMapping()
  @RequiresPermissions("sys:monitor:online")
  public String online() {
    return "system/online/online";
  }

  @ResponseBody
  @RequestMapping("/list")
  @RequiresPermissions("sys:monitor:online")
  public List<UserOnline> list() {
    return sessionService.list();
  }

  @ResponseBody
  @RequestMapping("/sessionList")
  @RequiresPermissions("sys:monitor:online")
  public Collection<Session> sessionList() {
    return sessionService.sessionList();
  }

  @ResponseBody
  @RequestMapping("/forceLogout/{sessionId}")
  @RequiresPermissions("sys:monitor:online")
  public CommonResponse forceLogout(@PathVariable("sessionId") String sessionId,
      RedirectAttributes redirectAttributes) {
    try {
      sessionService.forceLogout(sessionId);
      return CommonResponse.ok();
    } catch (Exception e) {
      e.printStackTrace();
      return CommonResponse.error();
    }

  }


}
