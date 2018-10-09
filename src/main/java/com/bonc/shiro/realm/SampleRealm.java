package com.bonc.shiro.realm;

import com.bonc.pojo.UUser;
import com.bonc.service.PermissionService;
import com.bonc.service.RoleService;
import com.bonc.service.UUserService;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.Set;

public class SampleRealm extends AuthorizingRealm {
    @Autowired
    UUserService userService;
    @Autowired
    PermissionService permissionService;
    @Autowired
    RoleService roleService;

    public SampleRealm() {
        super();
    }
    /**
     *  认证信息，主要针对用户登录，
     */
    protected AuthenticationInfo doGetAuthenticationInfo(
            AuthenticationToken authcToken) throws AuthenticationException {
    	// 强转成为用户自定义token
        ShiroToken token = (ShiroToken) authcToken;
        // 这里的username实际上存储的是email 数据库查找用户
        UUser user = userService.login(token.getUsername(),token.getPswd());
        // 
        if(null == user){
            throw new AccountException("帐号或密码不正确！");
            /**
             * 如果用户的status为禁用0。那么就抛出<code>DisabledAccountException</code>
             */
        }else if(UUser._0.equals(user.getStatus())){
            throw new DisabledAccountException("帐号已经禁止登录！");
        }else{
            // 密码验证通过  更新登录时间 last login time
            user.setLastLoginTime(new Date());
            userService.updateByPrimaryKeySelective(user);
        }
        //用户身份user(principal)存储到principal
        return new SimpleAuthenticationInfo(user,user.getPswd(),getName());
    }

    /**
     * 授权
     */
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        Long userId = TokenManager.getUserId();
        // 创建用户权限信息
        SimpleAuthorizationInfo info =  new SimpleAuthorizationInfo();
        //根据用户ID查询角色（role），放入到Authorization里。
        Set<String> roles = roleService.findRoleByUserId(userId);
        info.setRoles(roles);
        //根据用户ID查询权限（permission），放入到Authorization里。
        Set<String> permissions = permissionService.findPermissionByUserId(userId);
        info.setStringPermissions(permissions);
        return info;
    }


    /**
     * 清空当前用户权限信息，这里是方法的重写，因为父类的方法是protected 到时候会具体调用这里的方法
     */
    public  void clearCachedAuthorizationInfo() {
        PrincipalCollection principalCollection = SecurityUtils.getSubject().getPrincipals();
        SimplePrincipalCollection principals = new SimplePrincipalCollection(
                principalCollection, getName());
        super.clearCachedAuthorizationInfo(principals);
    }
    /**
     * 指定principalCollection 清除
     */
    public void clearCachedAuthorizationInfo(PrincipalCollection principalCollection) {
        SimplePrincipalCollection principals = new SimplePrincipalCollection(
                principalCollection, getName());
        super.clearCachedAuthorizationInfo(principals);
    }
}
