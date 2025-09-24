package com.example.lala.Mapper;

import com.example.lala.DTO.AdminTodayStatisticsDTO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface AdminTodayStatisMapper {

    List<AdminTodayStatisticsDTO> AdminTodayStatis();
}
