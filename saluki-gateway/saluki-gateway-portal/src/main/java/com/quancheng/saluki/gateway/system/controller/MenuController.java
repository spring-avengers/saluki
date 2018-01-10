package com.quancheng.saluki.gateway.system.controller;

import java.util.List;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.quancheng.saluki.gateway.common.BaseController;
import com.quancheng.saluki.gateway.common.CommonResponse;
import com.quancheng.saluki.gateway.common.Log;
import com.quancheng.saluki.gateway.system.domain.MenuDO;
import com.quancheng.saluki.gateway.system.domain.Tree;
import com.quancheng.saluki.gateway.system.service.MenuService;

@RequestMapping("/sys/menu")
@Controller
public class MenuController extends BaseController {
  String prefix = "system/menu";
  @Autowired
  MenuService menuService;

  @RequiresPermissions("sys:menu:menu")
  @GetMapping()
  String menu(Model model) {
    return prefix + "/menu";
  }

  @RequiresPermissions("sys:menu:menu")
  @RequestMapping("/list")
  @ResponseBody
  List<MenuDO> list() {
    List<MenuDO> menus = menuService.list();
    return menus;
  }

  @Log("添加菜单")
  @RequiresPermissions("sys:menu:add")
  @GetMapping("/add/{pId}")
  String add(Model model, @PathVariable("pId") Long pId) {
    model.addAttribute("pId", pId);
    if (pId == 0) {
      model.addAttribute("pName", "根目录");
    } else {
      model.addAttribute("pName", menuService.get(pId).getName());
    }
    return prefix + "/add";
  }

  @Log("编辑菜单")
  @RequiresPermissions("sys:menu:edit")
  @GetMapping("/edit/{id}")
  String edit(Model model, @PathVariable("id") Long id) {
    MenuDO mdo = menuService.get(id);
    Long pId = mdo.getParentId();
    model.addAttribute("pId", pId);
    if (pId == 0) {
      model.addAttribute("pName", "根目录");
    } else {
      model.addAttribute("pName", menuService.get(pId).getName());
    }
    model.addAttribute("menu", mdo);
    return prefix + "/edit";
  }

  @Log("保存菜单")
  @RequiresPermissions("sys:menu:add")
  @PostMapping("/save")
  @ResponseBody
  CommonResponse save(MenuDO menu) {
    if (menuService.save(menu) > 0) {
      return CommonResponse.ok();
    } else {
      return CommonResponse.error(1, "保存失败");
    }
  }

  @Log("更新菜单")
  @RequiresPermissions("sys:menu:edit")
  @PostMapping("/update")
  @ResponseBody
  CommonResponse update(MenuDO menu) {
    if (menuService.update(menu) > 0) {
      return CommonResponse.ok();
    } else {
      return CommonResponse.error(1, "更新失败");
    }
  }

  @Log("删除菜单")
  @RequiresPermissions("sys:menu:remove")
  @PostMapping("/remove")
  @ResponseBody
  CommonResponse remove(Long id) {
    if (menuService.remove(id) > 0) {
      return CommonResponse.ok();
    } else {
      return CommonResponse.error(1, "删除失败");
    }
  }

  @GetMapping("/tree")
  @ResponseBody
  Tree<MenuDO> tree() {
    Tree<MenuDO> tree = new Tree<MenuDO>();
    tree = menuService.getTree();
    return tree;
  }

  @GetMapping("/tree/{roleId}")
  @ResponseBody
  Tree<MenuDO> tree(@PathVariable("roleId") Long roleId) {
    Tree<MenuDO> tree = new Tree<MenuDO>();
    tree = menuService.getTree(roleId);
    return tree;
  }
}
