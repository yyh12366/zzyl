package com.zzyl.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.fastjson.JSONArray;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.zzyl.base.PageResponse;
import com.zzyl.dto.ReservationDto;
import com.zzyl.entity.Reservation;
import com.zzyl.enums.ReservationStatus;
import com.zzyl.exception.BaseException;
import com.zzyl.mapper.ReservationMapper;
import com.zzyl.service.ReservationService;
import com.zzyl.utils.UserThreadLocal;
import com.zzyl.vo.ReservationVo;
import com.zzyl.vo.TimeCountVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class ReservationServiceImpl implements ReservationService {
    @Autowired
    private ReservationMapper reservationMapper;

    @Override
    public void addReservation(ReservationDto reservationDto) {
        //  如果已经预约的次数超过3次，则当天不允许再进行预约
        Integer reservationCount = getCancelledReservationCount(reservationDto.getId());
        if(reservationCount>=3){
            throw new BaseException("取消次数已超过3次");
        }

        // 否则，允许添加预约
        //对数据库进行操作时一般使用dto.vo对应的entity对象,通过数据拷贝BeanUtils
        Reservation reservation = new Reservation();
        BeanUtil.copyProperties(reservationDto, reservation);


        //@JsonValue 可以用在get方法或者属性字段上，一个类只能用一个，当加上@JsonValue注解时，序列化时只返回这一个字段的值，而不是这个类的属性键值对,这个枚举类给序数添加了@JsonValue注解,这样最后只会返回序数而不是枚举名称
        //给新增的预约添加待报道的预约状态,通过getOrdinal方法返回枚举对象的序数,调用这个方法的是枚举名称
        reservation.setStatus(ReservationStatus.PENDING.getOrdinal());
        try {
            reservationMapper.insertReservation(reservation);
        } catch (Exception e) {
            log.info(e + "");
            throw new BaseException("此手机号已预约该时间");
        }
    }






    @Override
    public PageResponse<ReservationVo> queryReservationByPage(int pageNum, int pageSize, Integer status) {
        //PageHelper,指定页码和页面大小
        PageHelper.startPage(pageNum, pageSize);
        //获取当前操作预约的用户id
        Long userId = UserThreadLocal.getUserId();
        //对数据库进行操作时一般使用dto.vo对应的entity对象
        Page<Reservation> reservationPage= reservationMapper.queryReservationByPage(pageNum, pageSize, status,userId);
        // 通过of方法,将泛型为entity的Page转化为泛型为vo的PageResponse,并返回
        return PageResponse.of(reservationPage, ReservationVo.class);
    }

    @Override
    public void cancelReservationByid(long id) {
        Reservation reservationTest = getById(id);
        // 校验预约是否存在
        if(reservationTest==null) {
            throw new BaseException("预约不存在");
        }
        // 校验预约状态,只有状态为0,即待报道状态,才能取消订单
        if(reservationTest.getStatus()!=0){
            throw new BaseException("预约状态错误");
        }
        //执行到这里说明订单可以被取消
        Reservation reservation = new Reservation();
        reservation.setId(reservationTest.getId());

        //给新增的预约添加待报道的预约状态,通过getOrdinal方法返回枚举对象的序数,调用这个方法的是枚举名称
        reservation.setStatus(ReservationStatus.CANCELED.getOrdinal());

        reservationMapper.update(reservation);
    }

    @Override
    public Reservation getById(long id) {
        Reservation reservation = reservationMapper.getById(id);
        return reservation;
    }

    @Override
    public Integer getCancelledReservationCount( Long userId) {
        //获取当天开始时间
        LocalDateTime todayStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);//当天零点
        //获取当天结束时间
        LocalDateTime todayEnd = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);//当天24点

        Map map = new HashMap();
        map.put("begin",todayStart);
        map.put("end",todayEnd);
        //已取消的预约
        map.put("status", ReservationStatus.CANCELED.getOrdinal());
        map.put("createBy",userId);
        Integer cancelledReservationCount = reservationMapper.countByMap(map);
        return cancelledReservationCount;
    }


    @Override
    public List<TimeCountVo> getEachTimeReservationCount(LocalDateTime start) {
        LocalDateTime end = start.plusHours(24);
        return reservationMapper.getEachTimeReservationCount(start, end);
    }
}
