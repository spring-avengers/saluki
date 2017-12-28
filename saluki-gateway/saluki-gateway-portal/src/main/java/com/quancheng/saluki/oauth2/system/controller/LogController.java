package com.quancheng.saluki.oauth2.system.controller;

import java.util.Map;

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

@RequestMapping("/common/log")
@Controller
public class LogController {
	@Autowired
	LogService logService;
	String prefix = "common/log";

	@GetMapping()
	String log() {
		return prefix + "/log";
	}

	@ResponseBody
	@GetMapping("/list")
	PageDO<LogDO> list(@RequestParam Map<String, Object> params) {
		Query query = new Query(params);
		PageDO<LogDO> page = logService.queryList(query);
		return page;
	}
	
	@ResponseBody
	@PostMapping("/remove")
	CommonResponse remove(Long id) {
		if (logService.remove(id)>0) {
			return CommonResponse.ok();
		}
		return CommonResponse.error();
	}

	@ResponseBody
	@PostMapping("/batchRemove")
	CommonResponse batchRemove(@RequestParam("ids[]") Long[] ids) {
		int r = logService.batchRemove(ids);
		if (r > 0) {
			return CommonResponse.ok();
		}
		return CommonResponse.error();
	}
}
