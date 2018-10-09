package com.bonc.shiro.session.dao;

import java.io.Serializable;
import java.util.Collection;

import org.apache.shiro.session.Session;
import org.apache.shiro.session.UnknownSessionException;
import org.apache.shiro.session.mgt.eis.AbstractSessionDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.bonc.shiro.session.listener.CustomSessionListener;
import com.bonc.utils.LoggerUtils;

/**
 * 
 * 开发公司：SOJSON在线工具 <p>
 * 版权所有：© www.sojson.com<p>
 * 博客地址：http://www.sojson.com/blog/  <p>
 * <p>
 * 
 * Session 操作
 * 
 * <p>
 * 
 * 区分　责任人　日期　　　　说明<br/>
 * 创建　周柏成　2016年6月2日 　<br/>
 *
 * @author zhou-baicheng
 * @email  so@sojson.com
 * @version 1.0,2016年6月2日 <br/>
 * 
 */
public class CustomShiroSessionDAO extends AbstractSessionDAO{
	private static final Logger logger = LoggerFactory.getLogger(CustomSessionListener.class);
	/*配置文件注入 不需要注解需要set方法*/
    private ShiroSessionRepository shiroSessionRepository;  
    public void setShiroSessionRepository(ShiroSessionRepository shiroSessionRepository) {
    	logger.info("shiro会话仓库的注入，这里使用redis");
    	this.shiroSessionRepository = shiroSessionRepository;  
    }  
  
    @Override  
    public void update(Session session) throws UnknownSessionException {  
    	shiroSessionRepository.saveSession(session);  
    }

    
    /**
     * 创建session
     * @param session
     * @return
     */
    @Override  
    protected Serializable doCreate(Session session) {  
    	logger.info("创建用户session并且存储用户session");
        Serializable sessionId = this.generateSessionId(session);  
        this.assignSessionId(session, sessionId);  
        shiroSessionRepository.saveSession(session);  
        return sessionId;  
    }

    /**
     * 删除session
     * @param session
     */
    @Override  
    public void delete(Session session) {  
        if (session == null) {  
        	logger.error("删除Session 不能为null");
            return;  
        }  
        Serializable id = session.getId();  
        if (id != null)
        	shiroSessionRepository.deleteSession(id);
    }

    /**
     * 获取全部session
     * @return
     */
    @Override  
    public Collection<Session> getActiveSessions() {  
        return shiroSessionRepository.getAllSessions();  
    }

  
    /**
     * 获取单个session
     * @param sessionId
     * @return
     */
    @Override  
    public Session doReadSession(Serializable sessionId) {  
        return shiroSessionRepository.getSession(sessionId);  
    } }
