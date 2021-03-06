package com.timi.timizhuo.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class TimiReply extends BaseEntity<TimiReply> {

    /**内容*/
    private String replyContent;
    /**主回复id*/
    private String parentId;
    /**主贴id*/
    private String forumId;
    /**点赞次数*/
    private Long likeCount;
    /**楼层号*/
    private Integer tierNum;
    /**回复时间*/
    private Date replyTime;
    /**用户id*/
    private String userId;
    /**用户名字*/
    private String userName;
    /**用户图片地址*/
    private String userImageUrl;
    /**回复数*/
    private Integer replyNum ;
    /**回复类型  1 主贴回复  2 楼层回复  3 子回复回复*/
    private Integer replyType ;
    /**回复类型  子回复所需 父名字*/
    private String parentName;
    /** 回复图片 */
    private String imageUrl;
    /** 消息推送状态 1 未推送 2已推送  3失效 */
    private Integer messageState;

    /**
     * 图片回复列表
     */
    @TableField(exist = false)
    private List<String> imagesUrl;

    @TableField(exist = false)
    private List<String> friendIds;

    /**
     * 只看楼主
     */
    @TableField(exist = false)
    private Boolean lookFloorHost;

}
