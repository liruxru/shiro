package com.bonc.shiro.filter;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.filter.AccessControlFilter;
import org.apache.shiro.web.util.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bonc.shiro.cache.VCache;
import com.bonc.shiro.realm.TokenManager;
import com.bonc.shiro.session.dao.ShiroSessionRepository;
import com.bonc.utils.LoggerUtils;

/**
 * 相同帐号登录控制的拦截器
 * isAccessAllowed：即是否允许访问，返回true 表示允许；
 onAccessDenied：表示访问拒绝时是否自己处理，如果返回true 表示自己不处理且继续拦
 截器链执行，返回false表示自己已经处理了（比如重定向到另一个页面）。
 *
 */
@SuppressWarnings({"unchecked","static-access"})
public class KickoutSessionFilter extends AccessControlFilter {
	private Logger logger = LoggerFactory.getLogger(KickoutSessionFilter.class);
	
	//在线用户  KickoutSessionFilter.class.getCanonicalName()  获取包名加类名
	final static String ONLINE_USER = KickoutSessionFilter.class.getCanonicalName()+ "_online_user";
	//踢出状态，true标示踢出
	final static String KICKOUT_STATUS = KickoutSessionFilter.class.getCanonicalName()+ "_kickout_status";
	static VCache cache;
	//session获取
	static ShiroSessionRepository shiroSessionRepository;
	public static void setShiroSessionRepository(
			ShiroSessionRepository shiroSessionRepository) {
		KickoutSessionFilter.shiroSessionRepository = shiroSessionRepository;
	}

	/*表示是否允许访问；mappedValue 就是[urls]配置中拦截器参数部分，如
	果允许访问返回true，否则false；*/
	@Override
	protected boolean isAccessAllowed(ServletRequest request,
			ServletResponse response, Object mappedValue) throws Exception {
		logger.info("用户请求拦截器参数为{}",mappedValue);
		HttpServletRequest httpRequest = ((HttpServletRequest)request);
		logger.info("用户请求url为:{}",mappedValue);
		String url = httpRequest.getRequestURI();
		Subject subject = getSubject(request, response);
		//如果是相关目录 or 如果没有登录 就直接return true   用户登录返回true 用户携带记住me的cookie返回true
		/*这里必须是用户没有登录同时没有记住我功能时返回true  就不用走下边的程序，去下一个拦截器*/
		//url.startsWith("/open/")没有任何意义，因为配置文件配置的可以匿名访问
		if((!subject.isAuthenticated() && !subject.isRemembered())){
			return Boolean.TRUE;
		}
		/*这里表示用户已经登录了  要访问别的链接*/
		Session session = subject.getSession();
		Serializable sessionId = session.getId();
		/**
		 * 判断是否已经踢出
		 * 1.如果是Ajax 访问，那么给予json返回值提示。
		 * 2.如果是普通请求，直接跳转到登录页
		 */
		Boolean marker = (Boolean)session.getAttribute(KICKOUT_STATUS);
		if (null != marker && marker ) {
			Map<String, String> resultMap = new HashMap<String, String>();
			//判断是不是Ajax请求
			if (ShiroFilterUtils.isAjax(request) ) {
				LoggerUtils.debug(getClass(), "当前用户已经在其他地方登录，并且是Ajax请求！");
				resultMap.put("user_status", "300");
				resultMap.put("message", "您已经在其他地方登录，请重新登录！");
				ShiroFilterUtils.out(response, resultMap);
			}
			return  Boolean.FALSE;
		}

		//从缓存获取用户-Session信息 <UserId,SessionId>
		LinkedHashMap<Long, Serializable> infoMap = cache.get(ONLINE_USER, LinkedHashMap.class);
		//如果不存在，创建一个新的
		infoMap = null == infoMap ? new LinkedHashMap<Long, Serializable>() : infoMap;
		//获取tokenId
		Long userId = TokenManager.getUserId();
		//如果已经包含当前Session，并且是同一个用户，跳过。
		if(infoMap.containsKey(userId) && infoMap.containsValue(sessionId)){
			//更新存储到缓存1个小时（这个时间最好和session的有效期一致或者大于session的有效期）
			cache.setex(ONLINE_USER, infoMap, 3600);
			return Boolean.TRUE;
		}
		//如果用户相同，Session不相同，那么就要处理了
		/**
		 * 如果用户Id相同,Session不相同
		 * 1.获取到原来的session，并且标记为踢出。
		 * 2.继续走
		 */
		if(infoMap.containsKey(userId) && !infoMap.containsValue(sessionId)){
			//获取原来登录用户的session,标记他为踢出，当他访问别的页面时，又会来到这个拦截器，然后让他重新登录
			Serializable oldSessionId = infoMap.get(userId);
			Session oldSession = shiroSessionRepository.getSession(oldSessionId);
			if(null != oldSession){
				//标记session已经踢出
				oldSession.setAttribute(KICKOUT_STATUS, Boolean.TRUE);
				shiroSessionRepository.saveSession(oldSession);//更新session
				LoggerUtils.fmtDebug(getClass(), "kickout old session success,oldId[%s]",oldSessionId);
			}else{

				shiroSessionRepository.deleteSession(oldSessionId);
				infoMap.remove(userId);
				/*移除旧的session,添加新的session  这一行是ly添的*/
				infoMap.put(userId, sessionId);
				//存储到缓存1个小时（这个时间最好和session的有效期一致或者大于session的有效期）
				cache.setex(ONLINE_USER, infoMap, 3600);
			}
			return  Boolean.TRUE;
		}
		/*既不包括用户id也不包括这个用户的sessionId 就把这个用户的信息存储起来*/
		if(!infoMap.containsKey(userId) && !infoMap.containsValue(sessionId)){
			infoMap.put(userId, sessionId);
			//存储到缓存1个小时（这个时间最好和session的有效期一致或者大于session的有效期）
			cache.setex(ONLINE_USER, infoMap, 3600);
		}
		return Boolean.TRUE;
	}

	/*表示当访问拒绝时是否已经处理了；如果返回true 表示需要继续处理；
	如果返回false表示该拦截器实例已经处理了，将直接返回即可。*/
	@Override
	protected boolean onAccessDenied(ServletRequest request,
			ServletResponse response) throws Exception {
//		如果上边返回的是false,执行这里的操作
		//先退出
		Subject subject = getSubject(request, response);
		subject.logout();
		//保存请求
		saveRequest(request);//WebUtils.getSavedRequest(request); 获取保存的请求
		//重定向到登录
		WebUtils.issueRedirect(request, response, ShiroFilterUtils.KICKED_OUT);
		return false;
	}



}
