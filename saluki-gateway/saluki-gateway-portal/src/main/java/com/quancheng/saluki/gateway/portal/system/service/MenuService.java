package com.quancheng.saluki.gateway.portal.system.service;

import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.quancheng.saluki.gateway.portal.system.domain.MenuDO;
import com.quancheng.saluki.gateway.portal.system.domain.Tree;

@Service
public interface MenuService {
	Tree<MenuDO> getSysMenuTree(Long id);

	List<Tree<MenuDO>> listMenuTree(Long id);

	Tree<MenuDO> getTree();

	Tree<MenuDO> getTree(Long id);

	List<MenuDO> list();

	int remove(Long id);

	int save(MenuDO menu);

	int update(MenuDO menu);

	MenuDO get(Long id);

	Set<String> listPerms(Long userId);
}
