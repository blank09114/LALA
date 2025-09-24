package com.example.lala.Service;

import com.example.lala.DTO.AdminTodayStatisticsDTO;
import com.example.lala.Mapper.AdminTodayStatisMapper;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class AdminTodayStatisService {

    private final AdminTodayStatisMapper adminTodayStatisMapper;

    public List<AdminTodayStatisticsDTO> todayStatis (){
        return adminTodayStatisMapper.AdminTodayStatis();
    }
}
