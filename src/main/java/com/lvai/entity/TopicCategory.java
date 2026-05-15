package com.lvai.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

@Data
@TableName("topic_category")
public class TopicCategory implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private Integer sort;
}
