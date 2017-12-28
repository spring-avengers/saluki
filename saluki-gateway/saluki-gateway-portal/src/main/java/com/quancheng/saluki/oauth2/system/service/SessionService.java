package com.quancheng.saluki.oauth2.system.service;

import java.util.Collection;
import java.util.List;

import org.apache.shiro.session.Session;
import org.springframework.stereotype.Service;

import com.quancheng.saluki.oauth2.system.domain.UserOnline;

@Service
public interface SessionService {
	List<UserOnline> list();

	Collection<Session> sessionList();
	
	boolean forceLogout(String sessionId);
}
