package com.bonc.controller;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.shiro.authc.DisabledAccountException;
import org.apache.shiro.web.util.SavedRequest;
import org.apache.shiro.web.util.WebUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import com.bonc.pojo.UUser;
import com.bonc.shiro.realm.TokenManager;
import com.bonc.utils.LoggerUtils;
import com.bonc.utils.StringUtils;

@Controller
public class AuthController {
    @RequestMapping("/u/login")
    public String login(HttpServletRequest request, Model model, String username, String pswd){
        Map<String,Object> resultMap =new HashMap<>();
        try {
            UUser user=new UUser();
            user.setNickname(username);
            user.setPswd(pswd);
            UUser userDo = TokenManager.login(user,false);
            resultMap.put("status", 200);
            resultMap.put("message", "登录成功");
            //获取之前保存的url
            SavedRequest savedRequest = WebUtils.getSavedRequest(request);
            String url = null ;
            if(null != savedRequest){
                url = savedRequest.getRequestUrl();
            }
            /**
             * 我们平常用的获取上一个请求的方式，在Session不一致的情况下是获取不到的
             * String url = (String) request.getAttribute(WebUtils.FORWARD_REQUEST_URI_ATTRIBUTE);
             */
            LoggerUtils.fmtDebug(getClass(), "获取登录之前的URL:[%s]",url);
            //如果登录之前没有地址，那么就跳转到首页。
            if(StringUtils.isBlank(url)){
                url = request.getContextPath() + "/open/index.jsp";
            }
            //跳转地址
            return "redirect:"+url;
            /**
             * 这里其实可以直接catch Exception，然后抛出 message即可，但是最好还是各种明细catch 好点。。
             */
        } catch (DisabledAccountException e) {
            resultMap.put("status", 500);
            resultMap.put("message", "帐号已经禁用。");
        } catch (Exception e) {
            resultMap.put("status", 500);
            resultMap.put("message", "帐号或密码错误");
        }
        model.addAllAttributes(resultMap);
        return "login";
    }
}
