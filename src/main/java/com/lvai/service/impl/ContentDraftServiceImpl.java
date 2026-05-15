package com.lvai.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lvai.entity.ContentDraft;
import com.lvai.mapper.ContentDraftMapper;
import com.lvai.service.IContentDraftService;
import org.springframework.stereotype.Service;

@Service
public class ContentDraftServiceImpl extends ServiceImpl<ContentDraftMapper, ContentDraft> implements IContentDraftService {
}
