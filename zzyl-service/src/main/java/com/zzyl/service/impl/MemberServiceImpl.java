package com.zzyl.service.impl;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.google.common.collect.Lists;
import com.zzyl.base.PageResponse;
import com.zzyl.constant.Constants;
import com.zzyl.dto.UserLoginRequestDto;
import com.zzyl.entity.Member;
import com.zzyl.exception.BaseException;
import com.zzyl.mapper.MemberMapper;
import com.zzyl.properties.JwtTokenManagerProperties;
import com.zzyl.service.*;
import com.zzyl.utils.JwtUtil;
import com.zzyl.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 用户管理
 */
@Slf4j
@Service
public class MemberServiceImpl implements MemberService {

    @Resource
    private JwtTokenManagerProperties jwtTokenManagerProperties;

    @Resource
    private WechatService wechatService;

    @Resource
    private MemberMapper memberMapper;


    static ArrayList DEFAULT_NICKNAME_PREFIX = Lists.newArrayList(
         "生活更美好",
                    "大桔大利",
                    "日富一日",
                    "好柿开花",
                    "柿柿如意",
                    "一椰暴富",
                    "大柚所为",
                    "杨梅吐气",
                    "天生荔枝"
                    );
    /**
     * 新增
     * @param member 用户信息
     */
   /* @Override
    public void save(Member member) {
        //判断id是否存在，不存在则新增，否则是更新
        if(ObjectUtil.isEmpty(member.getId())){
            //进入if方法体中的话说明是新增,那么就像正常新增用户之后一样,昵称应该是随机的,之后用户再自己进行更改
            //随机组装昵称，从设定好的词组中随机获取,拼接上手机号后4位(从第8位开始截取到最后)
            String nickName = DEFAULT_NICKNAME_PREFIX.get((int)(Math.random() * DEFAULT_NICKNAME_PREFIX.size())) + StringUtils.substring(member.getPhone(), 7);
            member.setName(nickName);
            memberMapper.save(member);
            return;
        }
        update(member);
    }*/

    /**
     * 根据openid查询用户
     *
     * @param openId 微信ID
     * @return 用户信息
     */
   /* @Override
    public Member getByOpenid(String openId) {
        return memberMapper.getByOpenid(openId);
    }*/


    /**
     * 登录
     *
     * @param userLoginRequestDto 登录code
     * @return 用户信息
     */
    @Override
    public LoginVo login(UserLoginRequestDto userLoginRequestDto) throws IOException {
        // 1 调用微信开放平台小程序的api，根据code获取openid
        String openId = wechatService.getOpenid(userLoginRequestDto.getCode());
        /*
         * 2 获取openid后,根据openid从数据库查询用户
         * 2.1 如果为新用户，此处返回为null
         * 2.2 如果为已经登录过的老用户，此处返回为member对象 （包含openId,phone,unionId等字段）
         */
      // Member member=  getByOpenid(openId);
        //2.根据openId查询用户
        Member member = memberMapper.getByOpenId(openId);
        //3.如果用户为空，则新增
        if (ObjectUtil.isEmpty(member)) {
            member = Member.builder().openId(openId).build();
        }


        /*
         * 4 构造用户数据，设置openId,unionId
         * 4.1 如果member为null，则为新用户，需要构建新的member对象，并设置openId
         * 4.2 如果member不为null，则为老用户，无需设置openId,直接返回即可
         */
       /* member = ObjectUtil.isNotEmpty(member) ? member : Member.builder().openId(openId).build();*/

        // 4. 调用微信开放平台小程序的api获取微信绑定的手机号
        String phone = wechatService.getPhone(userLoginRequestDto.getPhoneCode());


        //5.保存或修改用户
        saveOrUpdate(member, phone);
        /*
         * 6 新用户绑定手机号或者老用户更新手机号,方法会自动作非空校验,不为空再比对手机号是否一致
         * 6.1 如果member.getPhone()为null，则为新用户，需要设置手机号，并保存数据库
         * 6.2 如果member.getPhone()不为null，但是与微信获取到的手机号不一样  则表示用户改了微信绑定的手机号，需要设置手机号，并保存数据库
         * 以上俩种情况，都需要重新设置手机号，并保存数据库
         */
       /* if (ObjectUtil.notEqual(member.getPhone(), phone)) {
            member.setPhone(phone);
            save(member);
        }*/
/*        // 7 将用户ID存入token,键为常量的用户id,值为从member对象中获取的id
        Map<String, Object> claims = MapUtil.<String, Object>builder().put(Constants.JWT_USERID, member.getId()).build();*/
        //6.将用户id存入token,返回
        Map<String, Object> claims = new HashMap<>();
        claims.put(Constants.JWT_USERID, member.getId());
        //给对应的用户存入用户名称
        claims.put(Constants.JWT_USERNAME, member.getName());
        // 8 封装token，参数是JWT签名加密和有效时间
        String token = JwtUtil.createJWT(jwtTokenManagerProperties.getBase64EncodedSecretKey(), jwtTokenManagerProperties.getTtl(), claims);

        // 响应结果
        LoginVo loginVO = new LoginVo();
        loginVO.setToken(token);
        loginVO.setNickName(member.getName());
        return loginVO;
    }


    /**
     * 更新用户信息
     *
     * @param member 用户信息
     * @return 更新结果
     */
   /* @Override
    public int update(Member member) {
        return memberMapper.update(member);
    }*/


    /**
     * 保存或修改客户
     *
     * @param member
     * @param phone
     */
    private void saveOrUpdate(Member member, String phone) {

        //1.判断取到的手机号与数据库中保存的手机号不一样
        if(ObjectUtil.notEqual(phone, member.getPhone())){
            //设置手机号
            member.setPhone(phone);
        }
        //2.判断id存在
        if (ObjectUtil.isNotEmpty(member.getId())) {
            memberMapper.update(member);
            return;
        }
        //3.保存新的用户
        //随机组装昵称，词组+手机号后四位
        String nickName = DEFAULT_NICKNAME_PREFIX.get((int) (Math.random() * DEFAULT_NICKNAME_PREFIX.size()))
                + StringUtils.substring(member.getPhone(), 7);

        member.setName(nickName);
        memberMapper.save(member);
    }

    /**
     * 根据id查询用户
     *
     * @param id 用户id
     * @return 用户信息
     */
  /*  @Override
    public Member getById(Long id) {
        return memberMapper.selectById(id);
    }*/



    @Resource
    private ContractService contractService;

    @Resource
    private MemberElderService memberElderService;

    @Resource
    private OrderService orderService;


    /**
     * 分页查询用户列表
     *
     * @param page     当前页码
     * @param pageSize 每页数量
     * @param phone    手机号
     * @param nickname 昵称
     * @return 分页结果
     */
   /* @Override
    public PageResponse<MemberVo> page(Integer page, Integer pageSize, String phone, String nickname) {
        PageHelper.startPage(page, pageSize);
        Page<List<Member>> listPage = memberMapper.page(phone, nickname);

        PageResponse<MemberVo> pageResponse = PageResponse.of(listPage, MemberVo.class);
        List<Long> ids = pageResponse.getRecords().stream().map(MemberVo::getId).distinct().collect(Collectors.toList());

        pageResponse.getRecords().forEach(v -> {
            List<ContractVo> contractVos = contractService.listByMemberPhone(v.getPhone());
            v.setIsSign(contractVos.isEmpty() ? "否" : "是");
            List<OrderVo> orderVos = orderService.listByMemberId(v.getId());
            v.setOrderCount(orderVos.size());
            List<MemberElderVo> memberElderVos = memberElderService.listByMemberId(v.getId());
            List<String> collect = memberElderVos.stream().map(m -> m.getElderVo().getName()).collect(Collectors.toList());
            v.setElderNames(String.join(",", collect));
        });
        return pageResponse;
    }*/
}
