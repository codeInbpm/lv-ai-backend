package com.lvai.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lvai.entity.Destination;
import com.lvai.vo.DestinationDetailVO;

public interface IDestinationService extends IService<Destination> {
    Page<Destination> getHotDestinations(int page, int size);
    DestinationDetailVO getDestinationDetail(Long id);
}
