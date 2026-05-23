package com.lvai.vo;

import com.lvai.entity.TravelInspiration;
import com.lvai.entity.Destination;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "出行灵感详情VO")
public class InspirationVO extends TravelInspiration {
    
    @Schema(description = "关联的目的地信息")
    private Destination destination;
}
