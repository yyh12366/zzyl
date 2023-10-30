package com.zzyl.controller;

import cn.hutool.core.date.LocalDateTimeUtil;
import com.zzyl.base.PageResponse;
import com.zzyl.base.ResponseResult;
import com.zzyl.dto.ReservationDto;
import com.zzyl.service.ReservationService;
import com.zzyl.utils.UserThreadLocal;
import com.zzyl.vo.ReservationVo;
import com.zzyl.vo.TimeCountVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/customer/reservation")
@Api(tags = "客户预约管理")
public class CustomerReservationController extends BaseController {


    @Autowired
    private ReservationService reservationService;



    @GetMapping("/cancelled-count")
    @ApiOperation("查询取消预约数量")
    public ResponseResult<Integer> getCancelledReservationCount() {

        Long userId = UserThreadLocal.getUserId();
        //TODO:后面开发了取消预约后，需要到数据库中去统计取消次数
        Integer count=reservationService.getCancelledReservationCount(userId);
        System.out.println(count);
        return success(count);
    }


    @GetMapping("/countByTime")
    @ApiOperation("查询每个时间段剩余预约次数")
    public ResponseResult getEachTimeReservationCount(@RequestParam(required = false) Long time) {
        // LocalDateTimeUtil.of直接将时间戳转换为LocalDateTime
        List<TimeCountVo> timeWithCountList =reservationService.getEachTimeReservationCount(LocalDateTimeUtil.of(time));
        return ResponseResult.success(timeWithCountList);
    }

    @PostMapping
    @ApiOperation("新增预约")
    public ResponseResult addReservation(@RequestBody ReservationDto reservationDto) {
        reservationService.addReservation(reservationDto);
        return success();
    }


    @GetMapping("/page")
    @ApiOperation("分页查询预约")
    public ResponseResult<PageResponse<ReservationVo>> QueryReservationByPage(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            Integer status) {
        PageResponse<ReservationVo> page=reservationService.queryReservationByPage(pageNum, pageSize,status);
        return success(page);
    }

    @PutMapping("/{id}/cancel")
    @ApiOperation("取消预约")
    public ResponseResult cancelReservationById(@PathVariable long id) {
        reservationService.cancelReservationByid(id);
        return success();
    }
}