package com.quancheng.saluki.gateway.portal.system.service;

import org.springframework.stereotype.Service;

import com.quancheng.saluki.gateway.portal.system.domain.LogDO;
import com.quancheng.saluki.gateway.portal.system.domain.PageDO;
import com.quancheng.saluki.gateway.portal.utils.Query;
@Service
public interface LogService {
	PageDO<LogDO> queryList(Query query);
	int remove(Long id);
	int batchRemove(Long[] ids);
}
