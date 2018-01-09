package com.quancheng.saluki.oauth2.system.service;

import org.springframework.stereotype.Service;

import com.quancheng.saluki.oauth2.system.domain.LogDO;
import com.quancheng.saluki.oauth2.system.domain.PageDO;
import com.quancheng.saluki.oauth2.utils.Query;
@Service
public interface LogService {
	PageDO<LogDO> queryList(Query query);
	int remove(Long id);
	int batchRemove(Long[] ids);
}
