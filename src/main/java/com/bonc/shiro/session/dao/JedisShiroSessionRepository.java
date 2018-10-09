package com.bonc.shiro.session.dao;

import java.io.Serializable;
import java.util.Collection;

import org.apache.shiro.session.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bonc.shiro.jedis.JedisManager;
import com.bonc.shiro.session.CustomSessionManager;
import com.bonc.shiro.session.SessionStatus;
import com.bonc.utils.SerializeUtil;

/**
 * Session 管理
 * @author sojson.com
 *
 */
@SuppressWarnings("unchecked")
public class JedisShiroSessionRepository implements ShiroSessionRepository {
	private static final Logger logger = LoggerFactory.getLogger(JedisShiroSessionRepository.class);
	
    public static final String REDIS_SHIRO_SESSION = "sojson-shiro-demo-session:";
    //这里有个小BUG，因为Redis使用序列化后，Key反序列化回来发现前面有一段乱码，解决的办法是存储缓存不序列化
    public static final String REDIS_SHIRO_ALL = "sojson-shiro-demo-session:*";
    private static final int DB_INDEX = 1;
    /*配置注入*/
    private JedisManager jedisManager;
    public void setJedisManager(JedisManager jedisManager) {
        this.jedisManager = jedisManager;
    }

    @Override
    public void saveSession(Session session) {
    	logger.info("使用jedis存储用户session");
        if (session == null || session.getId() == null)
            throw new NullPointerException("session is empty");
        try {
        	// 创建resis的键  视通了前缀+sessionId 序列化的方法  sojson-shiro-demo-session:
            byte[] key = SerializeUtil.serialize(buildRedisSessionKey(session.getId()));
            
            // session中的在线状态不存在时才添加。
            if(null == session.getAttribute(CustomSessionManager.SESSION_STATUS)){
            	//新建sessionStatus对象  默认为true;标表示在线状态
            	SessionStatus sessionStatus = new SessionStatus();
            	session.setAttribute(CustomSessionManager.SESSION_STATUS, sessionStatus);
            }
            
            byte[] value = SerializeUtil.serialize(session);


            /**这里是我犯下的一个严重问题，但是也不会是致命，
             * 我计算了下，之前上面不小心给我加了0，也就是 18000 / 3600 = 5 个小时
             * 另外，session设置的是30分钟的话，并且加了一个(5 * 60)，一起算下来，session的失效时间是 5:35 的概念才会失效
             * 我原来是存储session的有效期会比session的有效期会长，而且最终session的有效期是在这里【SESSION_VAL_TIME_SPAN】设置的。
             *
             * 这里没有走【shiro-config.properties】配置文件，需要注意的是【spring-shiro.xml】 也是直接配置的值，没有走【shiro-config.properties】
             *
             * PS  : 注意： 这里我们配置 redis的TTL单位是秒，而【spring-shiro.xml】配置的是需要加3个0（毫秒）。
                long sessionTimeOut = session.getTimeout() / 1000;
                Long expireTime = sessionTimeOut + SESSION_VAL_TIME_SPAN + (5 * 60);
                
                                       直接使用 (int) (session.getTimeout() / 1000) 的话，session失效和redis的TTL 同时生效
             */


            /*
            	直接使用 (int) (session.getTimeout() / 1000) 的话，session失效和redis的TTL 同时生效
             */
            logger.info("获取的session超时时间为{}",session.getTimeout());
            jedisManager.saveValueByKey(DB_INDEX, key, value, (int) (session.getTimeout() / 1000));
        } catch (Exception e) {
        	logger.error("save session error,id:{}",session.getId());
        }
    }

    @Override
    public void deleteSession(Serializable id) {
        if (id == null) {
            throw new NullPointerException("session id is empty");
        }
        try {
        	jedisManager.deleteByKey(DB_INDEX,
                    SerializeUtil.serialize(buildRedisSessionKey(id)));
        } catch (Exception e) {
        	logger.error("删除session出现异常，id:{}",id);
        }
    }

   
	@Override
    public Session getSession(Serializable id) {
        if (id == null)
        	 throw new NullPointerException("session id is empty");
        Session session = null;
        try {
            byte[] value = jedisManager.getValueByKey(DB_INDEX, SerializeUtil
                    .serialize(buildRedisSessionKey(id)));
            session = SerializeUtil.deserialize(value, Session.class);
        } catch (Exception e) {
        	logger.error( "获取session异常，id:{}",id);
        }
        return session;
    }

    @Override
    public Collection<Session> getAllSessions() {
    	Collection<Session> sessions = null;
		try {
			sessions = jedisManager.AllSession(DB_INDEX,REDIS_SHIRO_ALL);
		} catch (Exception e) {
			logger.error("获取全部session异常");
		}
       
        return sessions;
    }

    private String buildRedisSessionKey(Serializable sessionId) {
        return REDIS_SHIRO_SESSION + sessionId;
    }


}
