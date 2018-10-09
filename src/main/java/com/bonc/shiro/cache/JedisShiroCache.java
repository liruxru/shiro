package com.bonc.shiro.cache;

import java.util.Collection;
import java.util.Set;

import org.apache.shiro.cache.Cache;
import org.apache.shiro.cache.CacheException;

import com.bonc.shiro.jedis.JedisManager;
import com.bonc.utils.LoggerUtils;
import com.bonc.utils.SerializeUtil;

/**
 * JedisShiroCache 继承了shiro的cache接口  实现这个类的方法之后，就会调用这个catch的方法
 * CustomShiroCacheManager获取缓存时，调用了这个类，new了一个
 * JedisShiroCache 同时为jedisManager赋值了
 * @param <K>
 * @param <V>
 */
public class JedisShiroCache<K, V> implements Cache<K, V> {

    /**
     * 为了不和其他的缓存混淆，采用追加前缀方式以作区分
     */
    private static final String REDIS_SHIRO_CACHE = "shiro-demo-cache:";
    /**
     * Redis 分片(分区)，也可以在配置文件中配置
     */
    private static final int DB_INDEX = 1;

    private JedisManager jedisManager;

    private String name;


    static final Class<JedisShiroCache> SELF = JedisShiroCache.class;

    /**
     * 这个类的构造器，新建类的时候同时为
     *  this.name = name;
     * this.jedisManager = jedisManager;赋值
     *  name默认为  授权/认证的类名加上授权/认证英文名字
     *  用户鉴定时会自动调用JedisShiroCacheManager的getCatch方法
     * @param name
     * @param jedisManager
     */
    public JedisShiroCache(String name, JedisManager jedisManager) {
        this.name = name;
        this.jedisManager = jedisManager;
    }

    /**
     * 自定义relm中的授权/认证的类名加上授权/认证英文名字
     */
    public String getName() {
        if (name == null)
            return "";
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * 添加缓存
     * @param key
     * @param value
     * @return
     * @throws CacheException
     */
    @Override
    public V put(K key, V value) throws CacheException {
        V previos = get(key);
        try {
            //为了不和其他的缓存混淆，采用追加前缀方式以作区分
            jedisManager.saveValueByKey(DB_INDEX, SerializeUtil.serialize(buildCacheKey(key)),
                    SerializeUtil.serialize(value), -1);
        } catch (Exception e) {
            LoggerUtils.error(SELF, "put cache throw exception",e);
        }
        return previos;
    }
    private String buildCacheKey(Object key) {
        return REDIS_SHIRO_CACHE + getName() + ":" + key;
    }

    /**
     * 获取缓存   JedisShiroCacheManager 的getCache(String name)方法自定的name 就是key
     * @param key
     * @return
     * @throws CacheException
     */
    @Override
    public V get(K key) throws CacheException {
        //加上我们自定义的前缀
        byte[] byteKey = SerializeUtil.serialize(buildCacheKey(key));
        byte[] byteValue = new byte[0];
        try {
            //调用jedis的获取方法
            byteValue = jedisManager.getValueByKey(DB_INDEX, byteKey);
        } catch (Exception e) {
            LoggerUtils.error(SELF, "get value by cache throw exception",e);
        }
        //进行反序列化
        return (V) SerializeUtil.deserialize(byteValue);
    }

    /**
     * 移除缓存
     * @param key
     * @return
     * @throws CacheException
     */
    @Override
    public V remove(K key) throws CacheException {
        V previos = get(key);
        try {
            //移除缓存
            jedisManager.deleteByKey(DB_INDEX, SerializeUtil.serialize(buildCacheKey(key)));
        } catch (Exception e) {
            LoggerUtils.error(SELF, "remove cache  throw exception",e);
        }
        //返回移除之前的值
        return previos;
    }

    @Override
    public void clear() throws CacheException {
        //TODO--
    }

    @Override
    public int size() {
        if (keys() == null)
            return 0;
        return keys().size();
    }

    @Override
    public Set<K> keys() {
        //TODO
        return null;
    }

    @Override
    public Collection<V> values() {
        //TODO
        return null;
    }



}
