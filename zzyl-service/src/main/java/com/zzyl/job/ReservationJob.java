package com.zzyl.job;

import com.zzyl.service.ReservationService;
import com.zzyl.vo.ReservationVo;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author sjqn
 * @date 2023/8/20
 */
@Component
@Slf4j
public class ReservationJob {

    @Autowired
    private ReservationService reservationService;

    @XxlJob("reservationStatusToExpired")
    public void updateReservationStatus() {
        log.info("预约状态-过期修改-begin");
        reservationService.updateReservationStatus(LocalDateTime.now());
        log.info("预约状态-过期修改-end");
    }
}