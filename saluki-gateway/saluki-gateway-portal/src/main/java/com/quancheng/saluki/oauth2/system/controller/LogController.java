package com.quancheng.saluki.oauth2.system.controller;

import java.util.Map;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.quancheng.saluki.oauth2.common.CommonResponse;
import com.quancheng.saluki.oauth2.system.domain.LogDO;
import com.quancheng.saluki.oauth2.system.domain.PageDO;
import com.quancheng.saluki.oauth2.system.service.LogService;
import com.quancheng.saluki.oauth2.utils.Query;

@RequestMapping("/sys/log")
@Controller
public class LogController {

  private String prefix = "system/log";

  @Autowired
  LogService logService;

  @GetMapping("/run")
  @RequiresPermissions("sys:monitor:run")
  String run() {
    return "redirect:/druid/index.html";
  }

  @GetMapping()
  @RequiresPermissions("sys:monitor:log")
  String log() {
    return prefix + "/log";
  }

  @ResponseBody
  @GetMapping("/list")
  @RequiresPermissions("sys:monitor:log")
  PageDO<LogDO> list(@RequestParam Map<String, Object> params) {
    Query query = new Query(params);
    PageDO<LogDO> page = logService.queryList(query);
    return page;
  }


  @ResponseBody
  @PostMapping("/remove")
  @RequiresPermissions("sys:monitor:log")
  CommonResponse remove(Long id) {
    if (logService.remove(id) > 0) {
      return CommonResponse.ok();
    }
    return CommonResponse.error();
  }

  @ResponseBody
  @PostMapping("/batchRemove")
  @RequiresPermissions("sys:monitor:log")
  CommonResponse batchRemove(@RequestParam("ids[]") Long[] ids) {
    int r = logService.batchRemove(ids);
    if (r > 0) {
      return CommonResponse.ok();
    }
    return CommonResponse.error();
  }
}
