package com.quancheng.saluki.oauth2.system.service;

import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.quancheng.saluki.oauth2.common.domain.Tree;
import com.quancheng.saluki.oauth2.system.domain.MenuDO;

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
