package com.lvai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "社区用户统计数据")
public class CommunityStatsDTO {
    @Schema(description = "关注数")
    private Integer followingCount;
    
    @Schema(description = "粉丝数")
    private Integer followersCount;
    
    @Schema(description = "获赞与被收藏总数")
    private Integer likesAndFavsCount;
}
