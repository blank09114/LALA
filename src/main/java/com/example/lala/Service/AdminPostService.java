package com.example.lala.Service;

import com.example.lala.DTO.Response.AdminUserActivity;
import com.example.lala.DTO.Response.ReportDetail;
import com.example.lala.Mapper.AdminPostMapper;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class AdminPostService{

    private AdminPostMapper adminPostMapper;

    public Map<String, List<AdminUserActivity>> getGroupedUserActivities(String userId, String filter, String title, String startDate, String endDate, Integer userType) {
        List<AdminUserActivity> list;

        switch (filter) {
            case "reportedByMe":
                list = adminPostMapper.findReportedByMe(userId, title, startDate, endDate);
                break;
            case "reportedToMe":
                list = adminPostMapper.findReportedToMe(userId, title, startDate, endDate);

                for (AdminUserActivity activity : list) {
                    List<ReportDetail> reports = switch (activity.getContentType()) {
                        case "post" -> adminPostMapper.findReportsByPostId(activity.getContentId());
                        case "comment", "reply" -> adminPostMapper.findReportsByCommentId(activity.getContentId());
                        case "review" -> adminPostMapper.findReportsByReviewId(activity.getContentId());
                        default -> List.of();
                    };
                    activity.setReports(reports);
                }
                break;
            case "mine":
            default:
                list = adminPostMapper.findUserActivities(userId, title, startDate, endDate, userType);

                System.out.println("userType = " + userType);
                System.out.println("Total activities found: " + list.size());

        }

        return list.stream()
                .collect(Collectors.groupingBy(AdminUserActivity::getContentType,
                        LinkedHashMap::new, Collectors.toList()));
    }
}
