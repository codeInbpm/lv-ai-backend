package com.lvai.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class CommentVO {
    @Schema(description = "评论ID")
    private Long id;

    @Schema(description = "评论内容")
    private String content;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "用户昵称")
    private String nickname;

    @Schema(description = "用户头像")
    private String avatar;

    @Schema(description = "父评论ID")
    private Long parentId;

    @Schema(description = "被回复的具体评论ID")
    private Long replyToId;

    @Schema(description = "被回复的用户ID")
    private Long replyToUserId;

    @Schema(description = "被回复的用户昵称")
    private String replyToNickname;

    @Schema(description = "点赞数")
    private Integer likeCount;

    @Schema(description = "是否已点赞")
    private Boolean hasLiked;

    @Schema(description = "子评论列表")
    private List<CommentVO> children;
}
