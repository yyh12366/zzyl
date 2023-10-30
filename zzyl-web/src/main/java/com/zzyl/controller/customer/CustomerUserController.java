package com.zzyl.controller.customer;

import com.zzyl.base.ResponseResult;
import com.zzyl.dto.UserLoginRequestDto;
import com.zzyl.service.MemberService;
import com.zzyl.utils.AjaxResultBuild;
import com.zzyl.vo.LoginVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

import static com.zzyl.base.ResponseResult.success;

@RestController
@Slf4j
@Api(tags="客户管理")
@RequestMapping("/customer/user")
public class CustomerUserController {

    @Autowired
    private MemberService memberService;
    /**
     * C端用户登录--微信登录
     * @param userLoginRequestDto 用户登录信息
     * @return 登录结果
     */
    @PostMapping("/login")
    @ApiOperation("登录")
    public ResponseResult<LoginVo> login(@RequestBody UserLoginRequestDto userLoginRequestDto) throws IOException {
        LoginVo login = memberService.login(userLoginRequestDto);

        return ResponseResult.success(login);
    }
}
