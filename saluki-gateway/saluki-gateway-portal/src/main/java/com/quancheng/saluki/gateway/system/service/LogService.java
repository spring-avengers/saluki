package com.quancheng.saluki.gateway.system.service;

import org.springframework.stereotype.Service;

import com.quancheng.saluki.gateway.system.domain.LogDO;
import com.quancheng.saluki.gateway.system.domain.PageDO;
import com.quancheng.saluki.gateway.utils.Query;
@Service
public interface LogService {
	PageDO<LogDO> queryList(Query query);
	int remove(Long id);
	int batchRemove(Long[] ids);
}
