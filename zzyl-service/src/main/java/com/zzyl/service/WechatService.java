package com.zzyl.service;

import java.io.IOException;

public interface WechatService {

    /**
     * 根据临时code获取用户唯一标识openid
     * @param code 临时code
     * @return
     */
    public String getOpenid(String code) throws IOException;

    /**
     * 根据临时phonecode获取用户绑定的手机号
     * @param phoneCode
     * @return
     */
    public String getPhone(String phoneCode) throws IOException;
}
