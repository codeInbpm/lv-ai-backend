package com.lvai.dto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

@Data
@Schema(description = "打卡/记账请求")
public class CheckInDTO {

    @NotNull(message = "行程ID不能为空")
    @Schema(description = "行程ID")
    private Long planId;

    @Schema(description = "每日行程ID")
    private Long dayId;

    @Schema(description = "行程明细ID")
    private Long itemId;

    @NotNull(message = "类型不能为空")
    @Schema(description = "记录类型: 1打卡 2记账 3日记")
    private Integer type;

    @Schema(description = "内容/备注")
    private String content;

    @Schema(description = "金额(记账时必填)")
    private BigDecimal amount;

    @Schema(description = "费用类型: 1餐饮 2住宿 3交通 4门票 5购物 6其他")
    private Integer costType;

    @Schema(description = "位置名称")
    private String locationName;

    @Schema(description = "经度")
    private BigDecimal lng;

    @Schema(description = "纬度")
    private BigDecimal lat;

    @Schema(description = "图片URLs")
    private List<String> images;

    private BigDecimal cost;
}