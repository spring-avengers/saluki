package com.quancheng.saluki.gateway.portal.system.controller;

import java.util.List;
import java.util.Map;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.quancheng.saluki.gateway.portal.common.BaseController;
import com.quancheng.saluki.gateway.portal.common.CommonResponse;
import com.quancheng.saluki.gateway.portal.common.Log;
import com.quancheng.saluki.gateway.portal.common.Pageable;
import com.quancheng.saluki.gateway.portal.system.domain.DeptDO;
import com.quancheng.saluki.gateway.portal.system.domain.RoleDO;
import com.quancheng.saluki.gateway.portal.system.domain.Tree;
import com.quancheng.saluki.gateway.portal.system.domain.UserDO;
import com.quancheng.saluki.gateway.portal.system.service.RoleService;
import com.quancheng.saluki.gateway.portal.system.service.UserService;
import com.quancheng.saluki.gateway.portal.utils.MD5Utils;
import com.quancheng.saluki.gateway.portal.utils.Query;

@RequestMapping("/sys/user")
@Controller
public class UserController extends BaseController {
  private String prefix = "system/user";
  @Autowired
  UserService userService;
  @Autowired
  RoleService roleService;

  @RequiresPermissions("sys:user:user")
  @GetMapping("")
  String user(Model model) {
    return prefix + "/user";
  }

  @GetMapping("/list")
  @ResponseBody
  Pageable list(@RequestParam Map<String, Object> params) {
    // 查询列表数据
    Query query = new Query(params);
    List<UserDO> sysUserList = userService.list(query);
    int total = userService.count(query);
    Pageable pageUtil = new Pageable(sysUserList, total);
    return pageUtil;
  }

  @RequiresPermissions("sys:user:add")
  @Log("添加用户")
  @GetMapping("/add")
  String add(Model model) {
    List<RoleDO> roles = roleService.list();
    model.addAttribute("roles", roles);
    return prefix + "/add";
  }

  @RequiresPermissions("sys:user:edit")
  @Log("编辑用户")
  @GetMapping("/edit/{id}")
  String edit(Model model, @PathVariable("id") Long id) {
    UserDO userDO = userService.get(id);
    model.addAttribute("user", userDO);
    List<RoleDO> roles = roleService.list(id);
    model.addAttribute("roles", roles);
    return prefix + "/edit";
  }

  @RequiresPermissions("sys:user:add")
  @Log("保存用户")
  @PostMapping("/save")
  @ResponseBody
  CommonResponse save(UserDO user) {
    user.setPassword(MD5Utils.encrypt(user.getUsername(), user.getPassword()));
    if (userService.save(user) > 0) {
      return CommonResponse.ok();
    }
    return CommonResponse.error();
  }

  @RequiresPermissions("sys:user:edit")
  @Log("更新用户")
  @PostMapping("/update")
  @ResponseBody
  CommonResponse update(UserDO user) {

    if (userService.update(user) > 0) {
      return CommonResponse.ok();
    }
    return CommonResponse.error();
  }

  @RequiresPermissions("sys:user:remove")
  @Log("删除用户")
  @PostMapping("/remove")
  @ResponseBody
  CommonResponse remove(Long id) {
    if (userService.remove(id) > 0) {
      return CommonResponse.ok();
    }
    return CommonResponse.error();
  }

  @RequiresPermissions("sys:user:batchRemove")
  @Log("批量删除用户")
  @PostMapping("/batchRemove")
  @ResponseBody
  CommonResponse batchRemove(@RequestParam("ids[]") Long[] userIds) {
    int r = userService.batchremove(userIds);
    if (r > 0) {
      return CommonResponse.ok();
    }
    return CommonResponse.error();
  }

  @PostMapping("/exit")
  @ResponseBody
  boolean exit(@RequestParam Map<String, Object> params) {
    // 存在，不通过，false
    return !userService.exit(params);
  }

  @RequiresPermissions("sys:user:resetPwd")
  @Log("请求更改用户密码")
  @GetMapping("/resetPwd/{id}")
  String resetPwd(@PathVariable("id") Long userId, Model model) {

    UserDO userDO = new UserDO();
    userDO.setUserId(userId);
    model.addAttribute("user", userDO);
    return prefix + "/reset_pwd";
  }

  @Log("提交更改用户密码")
  @PostMapping("/resetPwd")
  @ResponseBody
  CommonResponse resetPwd(UserDO user) {
    user.setPassword(
        MD5Utils.encrypt(userService.get(user.getUserId()).getUsername(), user.getPassword()));
    if (userService.resetPwd(user) > 0) {
      return CommonResponse.ok();
    }
    return CommonResponse.error();
  }

  @GetMapping("/tree")
  @ResponseBody
  public Tree<DeptDO> tree() {
    Tree<DeptDO> tree = new Tree<DeptDO>();
    tree = userService.getTree();
    return tree;
  }

  @GetMapping("/treeView")
  String treeView() {
    return prefix + "/userTree";
  }

  @GetMapping("/personal")
  String personal(Model model) {
    return prefix + "/personal";
  }

}
