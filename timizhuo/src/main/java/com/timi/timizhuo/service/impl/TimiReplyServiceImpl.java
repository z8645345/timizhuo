package com.timi.timizhuo.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.timi.timizhuo.dto.response.ReplyFindPageDTO;
import com.timi.timizhuo.entity.TimiForum;
import com.timi.timizhuo.entity.TimiReply;
import com.timi.timizhuo.entity.TimiUserMessage;
import com.timi.timizhuo.enums.ReplyEnum;
import com.timi.timizhuo.enums.UserMessageEnum;
import com.timi.timizhuo.mapper.TimiReplyMapper;
import com.timi.timizhuo.service.TimiForumService;
import com.timi.timizhuo.service.TimiReplyService;
import com.timi.timizhuo.service.TimiUserMessageService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.*;

@Service
@Slf4j
public class TimiReplyServiceImpl implements TimiReplyService {

    @Resource
    private TimiReplyMapper timiReplyMapper;

    @Autowired
    private TimiForumService timiForumService;

    @Autowired
    private TimiUserMessageService timiUserMessageService;

    @Override
    @Transactional
    public boolean addForum(TimiReply timiReply) {
        String forumId = timiReply.getForumId();
        if (StringUtils.isBlank(timiReply.getForumId())) {
            log.warn("数据有误,forumId为空");
            return false;
        }
        //主贴回复 ---  回复id会空
        if (timiReply.getReplyType().equals(ReplyEnum.ReplyTypeEnum.MAIN.getValue())) {
            //根据主贴id查询
            List<TimiReply> timiReplies = this.timiReplyMapper.findByForumId(timiReply);
            boolean flag;
            if (CollectionUtils.isEmpty(timiReplies)) {
                //如果没有回复过则为第一次回复 有楼层标识
                timiReply.setTierNum(1);
                timiReply.setCreateTime(new Date());
                timiReply.setUpdateTime(timiReply.getCreateTime());
                timiReply.setReplyTime(timiReply.getCreateTime());
                flag = this.timiReplyMapper.insert(timiReply) == 1;
            } else {
                //按回复时间顺序查询的数据
                TimiReply timiReplyTmp = timiReplies.get(timiReplies.size()-1);
                Integer tierNum = timiReplyTmp.getTierNum();
                Integer i = ++tierNum;
                timiReply.setTierNum(i);
                timiReply.setCreateTime(new Date());
                timiReply.setUpdateTime(timiReply.getCreateTime());
                timiReply.setReplyTime(timiReply.getCreateTime());
                flag = this.timiReplyMapper.insert(timiReply) == 1;
            }
            if (flag) { // 异步更新回复信息
                timiForumService.asyncUpdateReplyreplyCount(timiReply.getForumId());
            }
            this.userMessage(timiReply,UserMessageEnum.ContentTypeEnum.MAIN_AT, UserMessageEnum.ContentTypeEnum.MAIN);
            return flag;
        } else if (timiReply.getReplyType().equals(ReplyEnum.ReplyTypeEnum.TIER.getValue())) {
            // 楼层回复
            timiReply.setCreateTime(new Date());
            timiReply.setUpdateTime(timiReply.getCreateTime());
            timiReply.setReplyTime(timiReply.getCreateTime());
            this.timiReplyMapper.insert(timiReply);
            this.userMessage(timiReply,UserMessageEnum.ContentTypeEnum.TIER_AT, UserMessageEnum.ContentTypeEnum.TIER);
        } else if (timiReply.getReplyType().equals(ReplyEnum.ReplyTypeEnum.SON.getValue())) {
            //子回复回复
            //1 先查询主回复数据
            TimiReply replyById = this.timiReplyMapper.selectById(timiReply.getParentId());
            if (replyById == null) {
                log.warn("数据有误,根据id查询不到数据");
                return false;
            }
//            //2 修改主回复数
            TimiReply upReply = new TimiReply();
            upReply.setId(timiReply.getParentId());
            upReply.setUpdateTime(new Date());
            Integer replyNum = replyById.getReplyNum();
            if (replyNum == null){
                upReply.setReplyNum(1);
            }else {
                Integer num = ++replyNum;
                upReply.setReplyNum(num);
            }
            this.timiReplyMapper.updateReplyNum(upReply);
            timiReply.setCreateTime(new Date());
            timiReply.setUpdateTime(timiReply.getCreateTime());
            timiReply.setReplyTime(timiReply.getCreateTime());
            timiReply.setParentName(replyById.getUserName());
            this.timiReplyMapper.insert(timiReply);
            this.userMessage(timiReply,UserMessageEnum.ContentTypeEnum.SON_AT, UserMessageEnum.ContentTypeEnum.SON);
        }

        return true;
    }

    private boolean userMessage(TimiReply timiReply, UserMessageEnum.ContentTypeEnum atType, UserMessageEnum.ContentTypeEnum type) {
        List<TimiUserMessage> timiUserMessages = new ArrayList<>();
        //不为空 处理好友入消息表
        if (!CollectionUtils.isEmpty(timiReply.getFriendIds())) {
            timiReply.getFriendIds().forEach(id -> {
                TimiUserMessage timiUserMessage = new TimiUserMessage();
                timiUserMessage.setUserId(id);
                timiUserMessage.setForumId(timiReply.getForumId());
                timiUserMessage.setReplyId(timiReply.getId());
                timiUserMessage.setContentType(atType.getValue());
                timiUserMessage.setMessageState(UserMessageEnum.MessageStateEnum.UNREAD.getValue());
                timiUserMessage.setCreateTime(new Date());
                timiUserMessage.setUpdateTime(timiUserMessage.getCreateTime());
                timiUserMessages.add(timiUserMessage);
            });
        }
        TimiUserMessage timiUserMessage = new TimiUserMessage();
        timiUserMessage.setForumId(timiReply.getForumId());
        timiUserMessage.setReplyId(timiReply.getId());
        timiUserMessage.setContentType(type.getValue());
        timiUserMessage.setMessageState(UserMessageEnum.MessageStateEnum.UNREAD.getValue());
        timiUserMessage.setCreateTime(new Date());
        timiUserMessage.setUpdateTime(timiUserMessage.getCreateTime());

        if (timiReply.getReplyType().equals(ReplyEnum.ReplyTypeEnum.MAIN.getValue())) {
            // 主贴消息，提醒主贴发帖人
            TimiForum timiForum = timiForumService.getById(timiReply.getForumId());
            timiUserMessage.setUserId(timiForum.getUserId());
        } else {
            // 楼层回复的，提醒楼层回复人
            TimiReply timiReply1 = timiReplyMapper.selectById(timiReply.getParentId());
            timiUserMessage.setUserId(timiReply1.getUserId());
        }
        timiUserMessages.add(timiUserMessage);
       return this.timiUserMessageService.saveBatch(timiUserMessages);
    }

    @Override
    public List<TimiReply> findForumByStick() {
        return null;
    }

    @Override
    public PageInfo<List<ReplyFindPageDTO>> findPage(TimiReply timiReplyDto) {
        if (timiReplyDto == null || StringUtils.isBlank(timiReplyDto.getForumId())) {
            new PageInfo(Collections.EMPTY_LIST);
        }
        if (BooleanUtils.isTrue(timiReplyDto.getLookFloorHost())) {
            // 只看楼主
            TimiForum timiForum = timiForumService.getById(timiReplyDto.getForumId());
            if (timiForum == null) {
                log.warn("查询数据为空");
                new PageInfo(Collections.EMPTY_LIST);
            }
            timiReplyDto.setUserId(timiForum.getUserId());
        }
        //查询所有主id的所有数据
        PageHelper.startPage(timiReplyDto.getPageNum(), timiReplyDto.getPageSize());
        List<TimiReply> timiReplyList = this.timiReplyMapper.findByForumId(timiReplyDto);
        if (CollectionUtils.isEmpty(timiReplyList)) {
            log.warn("查询数据为空");
            new PageInfo(Collections.EMPTY_LIST);
        }

        List<ReplyFindPageDTO> result = new ArrayList<>();
        for (TimiReply timiReply : timiReplyList) {
            ReplyFindPageDTO replyFindPageDTO = new ReplyFindPageDTO();
            replyFindPageDTO.setTimiReply(timiReply);
            TimiReply queryReply = new TimiReply();
            queryReply.setParentId(timiReply.getId());
            List<TimiReply> byCondition = this.timiReplyMapper.findByParentId(queryReply);
            replyFindPageDTO.setSubTimiReplyList(byCondition);
            result.add(replyFindPageDTO);
        }
        result.forEach(replyFindPageDTO -> {
            if (StringUtils.isNotEmpty(replyFindPageDTO.getTimiReply().getImageUrl())) {
                replyFindPageDTO.getTimiReply().setImagesUrl(Arrays.asList(replyFindPageDTO.getTimiReply().getImageUrl().split(",")));
            } else {
                replyFindPageDTO.getTimiReply().setImagesUrl(Collections.EMPTY_LIST);
            }
            replyFindPageDTO.getSubTimiReplyList().forEach(sub -> {
                if (StringUtils.isNotEmpty(sub.getImageUrl())) {
                    sub.setImagesUrl(Arrays.asList(sub.getImageUrl().split(",")));
                } else {
                    sub.setImagesUrl(Collections.EMPTY_LIST);
                }
            });
        });
        return new PageInfo(result);
    }

    private List<TimiReply> getTreeReplyList(List<TimiReply> resultTmp, String parentId) {
        if (StringUtils.isNotBlank(parentId)) {
            TimiReply queryReply = new TimiReply();
            queryReply.setParentId(parentId);
            List<TimiReply> byCondition = this.timiReplyMapper.findByParentId(queryReply);
            if (!CollectionUtils.isEmpty(byCondition)) {
                byCondition.forEach(timiReply -> {
                    resultTmp.add(timiReply);
                    getTreeReplyList(resultTmp, timiReply.getId());
                });
            }
        }
        return resultTmp;
    }

}