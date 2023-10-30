package com.zzyl.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.zzyl.exception.BaseException;
import com.zzyl.service.WechatService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author zzh
 */
@Service
public class WechatServiceImpl implements WechatService {
    // 获取登录凭证的接口
    private static final String REQUEST_URL = "https://api.weixin.qq.com/sns/jscode2session?grant_type=authorization_code";

    // 获取接口调用凭据token的接口
    private static final String TOKEN_URL = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential";

    // 获取用户绑定手机号的接口
    private static final String PHONE_REQUEST_URL = "https://api.weixin.qq.com/wxa/business/getuserphonenumber?access_token=";

    //从配置文件中读取appid
    @Value("${zzyl.wechat.appId}")
    private String appId;
    //从配置文件中读取appsecret
    @Value("${zzyl.wechat.appSecret}")
    private String secret;

    /**
     * 获取openid
     *
     * @param code 登录时传入的code
     * @return
     * @throws IOException
     */
    @Override
    public String getOpenid(String code) throws IOException {
        // 把从配置文件中读取的appid和appsecret封装到指定泛型的map中并返回,作为发送Url请求的参数
        Map<String, Object> requestUrlParam = getAppConfig();
        // 将传入的code装到map中
        requestUrlParam.put("js_code",code);
        // 发送Url请求,响应回来的是JSON格式的字符串
        String result = HttpUtil.get(REQUEST_URL, requestUrlParam);
        // 通过hutool工具包的JSON转换格式的方法,将JSON字符串转化为JSONObject,JSONObject中其实就是Map
        JSONObject jsonObject = JSONUtil.parseObj(result);
        // 若code不正确，则在map中用errcode的键获取值不为空,说明出现了错误,没有获取到openid，响应回去错误信息
        if (ObjectUtil.isNotEmpty(jsonObject.getInt("errcode"))) {
            throw new BaseException(jsonObject.getStr("errmsg"));
        }
        /// 没有在if中抛出异常说明成功的获取到了openid,将获取的openid返回
        return jsonObject.getStr("openid");
    }
    /**
     * 获取手机号
     *
     * @param phoneCode 手机号凭证
     * @return
     * @throws IOException
     */
    @Override
    public String getPhone(String phoneCode) throws IOException {
        //获取access_token
        String token = getToken();
        //拼接请求路径,在请求时需要带上token凭证,否则会被拦截
        String url = PHONE_REQUEST_URL + token;

        Map<String,Object> param = new HashMap<>();
        // 将传入的phoneCode装到map中
        param.put("code",phoneCode);
        // 发送Url请求,响应回来的是JSON格式的字符串
        String result = HttpUtil.post(url, JSONUtil.toJsonStr(param));
        JSONObject jsonObject = JSONUtil.parseObj(result);
        // 若phoneCode不正确，则在map中用errcode的键获取值不为空,说明出现了错误,没有获取到用户手机号phone，响应回去错误信息
        if (jsonObject.getInt("errcode") != 0) {
            throw new BaseException(jsonObject.getStr("errmsg"));

        }
        /// 没有在if中抛出异常说明成功的获取到了phone,将获取的phone返回
        // getJSONObject方法,如果JSONObjct对象中的value是一个JSONObject对象，即根据key获取对应的JSONObject对象
        // TODO:不知道为什么要用getJSONObject方法重新返回一个新的JSONObject对象,用户信息脱敏?
        return jsonObject.getJSONObject("phone_info").getStr("purePhoneNumber");
    }




    /**
     * 封装公共参数,把从配置文件中读取的appid和appsecret封装到指定泛型的map中并返回,作为发送Url请求的参数
     * @return
     */
    private Map<String, Object> getAppConfig() {

        Map<String, Object> requestUrlParam = new HashMap<>();
        // 封装appid
        requestUrlParam.put("appid",appId);
        // 封装appsecret
        requestUrlParam.put("secret",secret);
        return requestUrlParam;
    }

    /**
     * 获取接口调用凭据token
     * @return
     */
    public String getToken(){
        // 把从配置文件中读取的appid和appsecret封装到指定泛型的map中并返回,作为发送Url请求的参数
        Map<String, Object> requestUrlParam = getAppConfig();
        // 发送Url请求,响应回来的是JSON格式的字符串
        String result = HttpUtil.get(TOKEN_URL, requestUrlParam);
        // 通过hutool工具包的JSON转换格式的方法,将JSON字符串转化为JSONObject,JSONObject中其实就是Map
        JSONObject jsonObject = JSONUtil.parseObj(result);
        // 若code不正确，则在map中根据errcode获取值不为空,说明出现了错误,没有获取到openid，响应回去错误信息
        if(ObjectUtil.isNotEmpty(jsonObject.getInt("errcode"))){
            throw new BaseException(jsonObject.getStr("errmsg"));
        }
        /// 没有在if中抛出异常说明成功的获取到了token,将获取的token返回
        return jsonObject.getStr("access_token");

    }
}
