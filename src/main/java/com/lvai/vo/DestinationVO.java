package com.lvai.vo;

import com.lvai.entity.Destination;
import com.lvai.entity.DestinationSpot;
import com.lvai.entity.DestinationFood;
import lombok.Data;
import java.util.List;

@Data
public class DestinationVO extends Destination {
    private List<DestinationSpot> spots;
    private List<DestinationFood> foods;
}
