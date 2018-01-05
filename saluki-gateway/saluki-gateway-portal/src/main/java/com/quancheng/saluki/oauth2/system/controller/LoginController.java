package com.quancheng.saluki.oauth2.system.controller;

import java.util.List;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.quancheng.saluki.oauth2.common.Log;
import com.quancheng.saluki.oauth2.common.BaseController;
import com.quancheng.saluki.oauth2.common.CommonResponse;
import com.quancheng.saluki.oauth2.system.domain.MenuDO;
import com.quancheng.saluki.oauth2.system.domain.Tree;
import com.quancheng.saluki.oauth2.system.service.MenuService;
import com.quancheng.saluki.oauth2.utils.MD5Utils;
import com.quancheng.saluki.oauth2.utils.ShiroUtils;

@Controller
public class LoginController extends BaseController {

  @Autowired
  MenuService menuService;

  @GetMapping({"/", ""})
  String welcome(Model model) {
    return "redirect:/index";
  }

  @Log("请求访问主页")
  @GetMapping({"/index"})
  String index(Model model) {
    List<Tree<MenuDO>> menus = menuService.listMenuTree(getUserId());
    model.addAttribute("menus", menus);
    model.addAttribute("name", getUser().getName());
    model.addAttribute("username", getUser().getUsername());
    return "index";
  }

  @GetMapping("/login")
  String login() {
    return "login";
  }

  @Log("登录")
  @PostMapping("/login")
  @ResponseBody
  CommonResponse ajaxLogin(String username, String password) {
    password = MD5Utils.encrypt(username, password);
    UsernamePasswordToken token = new UsernamePasswordToken(username, password);
    Subject subject = SecurityUtils.getSubject();
    try {
      subject.login(token);
      return CommonResponse.ok();
    } catch (AuthenticationException e) {
      return CommonResponse.error("用户或密码错误");
    }
  }

  @GetMapping("/logout")
  String logout() {
    ShiroUtils.logout();
    return "redirect:/login";
  }

  @GetMapping("/main")
  String main() {
    return "main";
  }

  @GetMapping("/403")
  String error403() {
    return "403";
  }

}
