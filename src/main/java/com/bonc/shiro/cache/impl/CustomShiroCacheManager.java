package com.bonc.shiro.cache.impl;

import com.bonc.shiro.cache.ShiroCacheManager;
import org.apache.shiro.cache.Cache;
import org.apache.shiro.cache.CacheException;
import org.apache.shiro.cache.CacheManager;
import org.apache.shiro.util.Destroyable;

/**
 * 用户权限缓存的实现，这样每次授权就不用通过数据库了
 */

public class CustomShiroCacheManager implements CacheManager ,Destroyable {
    /*实际上注入的是JedisShiroCacheManager*/
    private ShiroCacheManager shiroCacheManager;
    public ShiroCacheManager getShiroCacheManager() {
        return shiroCacheManager;
    }
    public void setShiroCacheManager(ShiroCacheManager shiroCacheManager) {
        this.shiroCacheManager = shiroCacheManager;
    }
    /*CacheManager的方法  获取缓存*/
    @Override
    public <K, V> Cache<K, V> getCache(String name) throws CacheException {
        return shiroCacheManager.getCache(name);
    }

    @Override
    public void destroy() throws Exception {
        shiroCacheManager.destroy();
    }

}
