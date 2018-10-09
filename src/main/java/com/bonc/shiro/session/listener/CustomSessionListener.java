package com.bonc.shiro.session.listener;


import com.bonc.shiro.session.dao.ShiroSessionRepository;

import org.apache.shiro.session.Session;
import org.apache.shiro.session.SessionListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * 开发公司：SOJSON在线工具 <p>
 * 版权所有：© www.sojson.com<p>
 * 博客地址：http://www.sojson.com/blog/  <p>
 * 会话监听
 *
 * @author zhou-baicheng
 * @email  so@sojson.com
 * @version 1.0,2016年6月2日 <br/>
 * 
 */
public class CustomSessionListener implements SessionListener {
	private static final Logger logger = LoggerFactory.getLogger(CustomSessionListener.class);
    /*相当于session的dao  配置文件中自动注入*/
    private ShiroSessionRepository shiroSessionRepository;

    public void setShiroSessionRepository(ShiroSessionRepository shiroSessionRepository) {
        this.shiroSessionRepository = shiroSessionRepository;
    }

    /**
     * 一个会话的生命周期开始
     */
    @Override
    public void onStart(Session session) {
        System.out.println("on start");
    }
    /**
     * 一个会话的生命周期结束
     */
    @Override
    public void onStop(Session session) {
        System.out.println("on stop");
    }

    /**
     * session超时处理，删除session
     * @param session
     */
    @Override
    public void onExpiration(Session session) {
        shiroSessionRepository.deleteSession(session.getId());

    }



}

