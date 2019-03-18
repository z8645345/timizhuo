package com.timi.timizhuo.dao.model;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class TimiForum implements Serializable {
    /**id*/
    private String forumId;
    /**标题*/
    private String forumTitle;
    /**内容*/
    private String forumContent;
    /**图片地址*/
    private String imageUrl;
    /**@好友列表*/
    private String friendIds;
    /**阅读次数*/
    private Long readCount;
    /**点赞次数*/
    private Long likeCount;
    /**回复次数*/
    private Long replyCount;
    /**是否置顶*/
    private Boolean stick;
    /**置顶排序*/
    private Integer stickGroup;
    /**帖子类型*/
    private Integer forumType;
    /**用户id*/
    private String userId;
    /**用户名字*/
    private String userName;
    /**发帖时间*/
    private Date postedTime;

    /**用户图片地址*/
    private String userImageUrl;

    private Date createTime;
    private Date updateTime;
}