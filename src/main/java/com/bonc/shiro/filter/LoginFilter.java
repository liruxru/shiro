package com.bonc.shiro.filter;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.apache.shiro.web.filter.AccessControlFilter;
import org.apache.shiro.web.util.WebUtils;

import com.bonc.pojo.UUser;
import com.bonc.shiro.realm.TokenManager;
import com.bonc.utils.LoggerUtils;

/**
 * 判断是否登录  AccessControlFilter 中默认登录路径为/login.jsp
 */
public class LoginFilter  extends AccessControlFilter {
	final static Class<LoginFilter> CLASS = LoginFilter.class;


	@Override
	protected boolean isAccessAllowed(ServletRequest request,
			ServletResponse response, Object mappedValue) throws Exception {
		//(UUser) SecurityUtils.getSubject().getPrincipal(); 这里的token是从subject获取的身份
		UUser token = TokenManager.getToken();
//		判断是不是登录请求,是的话通过过滤器
		if(null != token || isLoginRequest(request, response)){// && isEnabled()
            return Boolean.TRUE;
        }
        //这里说明用户没有登录，如果是ajax请求，那么就返回没有登录的信息
		if (ShiroFilterUtils.isAjax(request)) {// ajax请求
			Map<String,String> resultMap = new HashMap<String, String>();
			LoggerUtils.debug(getClass(), "当前用户没有登录，并且是Ajax请求！");
			resultMap.put("login_status", "300");
			resultMap.put("message", "当前用户没有登录");//当前用户没有登录！
			ShiroFilterUtils.out(response, resultMap);
		}
		return Boolean.FALSE ;
            
	}
/*	表示当访问拒绝时是否已经处理了；如果返回true表示需要继续处理；
	如果返回false表示该拦截器实例已经处理了，将直接返回即可。*/
	@Override
	protected boolean onAccessDenied(ServletRequest request, ServletResponse response)
			throws Exception {
		//保存Request和Response 到登录后的链接，这样登录之后就可以访问之前的页面了
		saveRequest(request);
		// 重定向到登录页面
		WebUtils.issueRedirect(request, response, ShiroFilterUtils.LOGIN_URL);
		return Boolean.FALSE ;
	}

}
