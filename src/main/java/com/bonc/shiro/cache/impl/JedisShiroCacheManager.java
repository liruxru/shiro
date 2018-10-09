package com.bonc.shiro.cache.impl;

import com.bonc.shiro.cache.JedisShiroCache;
import com.bonc.shiro.cache.ShiroCacheManager;
import com.bonc.shiro.jedis.JedisManager;
import org.apache.shiro.cache.Cache;

/**
 * 使用jedis管理缓存
 */
public class JedisShiroCacheManager  implements ShiroCacheManager {

    private JedisManager jedisManager;
    public JedisManager getJedisManager() {
        return jedisManager;
    }
    public void setJedisManager(JedisManager jedisManager) {
        this.jedisManager = jedisManager;
    }

    @Override
    public <K, V> Cache<K, V> getCache(String name) {
        return new JedisShiroCache<K, V>(name,jedisManager);
    }

    @Override
    public void destroy() {
        //如果和其他系统，或者应用在一起就不能关闭
        //getJedisManager().getJedis().shutdown();
    }


}
