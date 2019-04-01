package com.timi.timizhuo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.util.Date;

@Data
public class TimiVideo extends BaseEntity {

    @TableId(type = IdType.UUID)
    private String id;

    private String videoTitle;

    private String videoCover;

    private String videoContentUrl;

    private String videoTime;

    private Long loveCount;

    private Long showCount;

    private Long collectionCount;

    private String columnNo;

    private String columnName;

    private Date createTime;

    private Date updateTime;

    @TableField(exist = false)
    private Boolean userLove;

    @TableField(exist = false)
    private Boolean userCollection;

    private static final long serialVersionUID = 1L;



    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table timi_video
     *
     * @mbg.generated
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", videoTitle=").append(videoTitle);
        sb.append(", videoCover=").append(videoCover);
        sb.append(", videoContentUrl=").append(videoContentUrl);
        sb.append(", videoTime=").append(videoTime);
        sb.append(", loveCount=").append(loveCount);
        sb.append(", showCount=").append(showCount);
        sb.append(", collectionCount=").append(collectionCount);
        sb.append(", columnNo=").append(columnNo);
        sb.append(", columnName=").append(columnName);
        sb.append(", createTime=").append(createTime);
        sb.append(", updateTime=").append(updateTime);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}