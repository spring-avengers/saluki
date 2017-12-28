package com.quancheng.saluki.oauth2.common.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.quancheng.saluki.oauth2.common.domain.LogDO;
import com.quancheng.saluki.oauth2.common.domain.PageDO;
import com.quancheng.saluki.oauth2.common.utils.Query;
@Service
public interface LogService {
	PageDO<LogDO> queryList(Query query);
	int remove(Long id);
	int batchRemove(Long[] ids);
}
