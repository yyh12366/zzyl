package com.zzyl.intercept;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ObjectUtil;
import com.zzyl.constant.Constants;
import com.zzyl.constant.SecurityConstant;
import com.zzyl.exception.BaseException;
import com.zzyl.properties.JwtTokenManagerProperties;
import com.zzyl.utils.JwtUtil;
import com.zzyl.utils.UserThreadLocal;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * 小程序端token校验
 * 统一对请求的合法性进行校验，需要进行2个方面的校验，
 * 一、请求头中是否携带了authorization
 * 二、请求头中是否存在userId，如果不存在则说明是非法请求，响应401状态码
 * 如果是合法请求，将userId存储到ThreadLocal中
 * @author zzh
 */
@Slf4j
@Component
@EnableConfigurationProperties(JwtTokenManagerProperties.class) //jwt配置文件
public class UserInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtTokenManagerProperties jwtTokenManagerProperties;


    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //如果不是映射到方法就放行，比如跨域验证请求、静态资源等不需要身份校验的请求
        //个人理解:跟具体业务不相关的接口和请求,不需要进行权限验证,就不拦截
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }
        //从请求头中获取携带的token
        String token = request.getHeader(SecurityConstant.USER_TOKEN);

        log.info("开始解析 customer user token {}", token);
        if(ObjectUtil.isEmpty(token)) {
            //token失效
            throw new BaseException("小程序登录","401",null,"没有权限,请登录");
        }
        //加密token所使用的密钥会保存在配置文件中,通过密钥对token进行解析,并返回Claims
        Map<String, Object> claims = JwtUtil.parseJWT(jwtTokenManagerProperties.getBase64EncodedSecretKey(), token);
        if (ObjectUtil.isEmpty(claims)) {
            //token失效
            throw new BaseException("小程序登录","401",null,"没有权限,请登录");
        }
        // 根据userid的key去Map中查key对应的value结果,如果存在才能说明token有效
        Long userId = MapUtil.get(claims, Constants.JWT_USERID, Long.class);
        if (ObjectUtil.isEmpty(userId)) {
            throw new BaseException("小程序登录","401",null,"没有权限,请登录");
        }
        // token有效
        // 把用户信息保存在线程中，用户的每一次请求，就是一个线程，保存了用户信息，方便我们在后续操作获取用户登录信息
        UserThreadLocal.set(userId);
        return true;

    }

    /**
     * preHandle返回true后才会被调用,用于清理资源
     * @param request
     * @param response
     * @param handler
     * @param ex
     * @throws Exception
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        //移除当前线程中的用户信息
        UserThreadLocal.remove();
    }


}
