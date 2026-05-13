package com.lvai.vo;

import com.lvai.entity.Destination;
import com.lvai.entity.DestinationFood;
import com.lvai.entity.DestinationSpot;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.util.List;

@Data
@Schema(description = "目的地详情")
public class DestinationDetailVO {
    private Destination destination;
    private List<DestinationSpot> spots;
    private List<DestinationFood> foods;
}
