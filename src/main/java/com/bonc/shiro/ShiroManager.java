package com.bonc.shiro;

/**
 * 读取初始自定义权限内容,过滤器链定义的配置文件的类
 */
public interface ShiroManager {

    /**
     * 加载过滤配置信息  加载过滤器链
     * @return
     */
    String loadFilterChainDefinitions();

    /**
     * 重新构建权限过滤器
     * 一般在修改了用户角色、用户等信息时，需要再次调用该方法
     */
   void reCreateFilterChains();
}
