package com.zzyl.service;

import com.zzyl.base.PageResponse;
import com.zzyl.dto.ReservationDto;
import com.zzyl.entity.Reservation;
import com.zzyl.vo.ReservationVo;
import com.zzyl.vo.TimeCountVo;

import java.time.LocalDateTime;
import java.util.List;

public interface ReservationService {


    void addReservation(ReservationDto reservationDto);

    PageResponse<ReservationVo> queryReservationByPage(int pageNum, int pageSize, Integer status);

    void cancelReservationByid(long id);

    /**
     * 根据id查找预约信息
     * @param id 预约信息id
     * @return 预约信息
     */
    Reservation getById(long id);

    Integer getCancelledReservationCount( Long userId);


    List<TimeCountVo> getEachTimeReservationCount(LocalDateTime time);


    /**
     * 过期状态修改
     * @param now
     */
    void updateReservationStatus(LocalDateTime now);
}
