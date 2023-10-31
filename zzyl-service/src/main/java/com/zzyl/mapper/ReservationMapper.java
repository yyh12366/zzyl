
package com.zzyl.mapper;

import com.github.pagehelper.Page;
import com.zzyl.dto.ReservationDto;
import com.zzyl.entity.Member;
import com.zzyl.entity.Reservation;
import com.zzyl.vo.TimeCountVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 会员Mapper接口
 */
@Mapper
public interface ReservationMapper {


    // 插入预约信息,一般插入一条新数据之后会将新数据的id返回,类型为int
    int insertReservation( Reservation reservation);

    // 因为MyBatis在执行SQL时会自动通过对象中的属性来去给SQL中参数进行赋值，它会自动将Java类型转换成数据库的类型。
    // 如果一旦传入的是参数值是null 程序就无法准确判断这个类型应该是什么（是Integer？是VARCHAR?还是别的？）
    // 就有可能将类型转换错误，从而报错,加入jdbcType正是为了解决这样的报错
    // 需要针对这些可能为空的字段，手动指定其转换时用到的类型
    Page<Reservation> queryReservationByPage(int pageNum, int pageSize, Integer status, Long createBy);


    /**
     * 根据id查询订单
     * @param id
     */
    @Select("select * from reservation where id=#{id}")
    Reservation getById(Long id);

    void update(Reservation reservation);


    /**
     * 根据动态条件统计预约数量
     * @param map
     * @return
     */
    Integer countByMap(Map map);



    List<TimeCountVo> getEachTimeReservationCount(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);



    @Update("update reservation set status = 3 where status = 0 and time < #{now}")
    void updateReservationStatus(LocalDateTime now);
}


