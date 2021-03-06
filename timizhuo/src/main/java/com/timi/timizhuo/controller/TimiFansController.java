package com.timi.timizhuo.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.timi.timizhuo.annotation.TimiLogin;
import com.timi.timizhuo.common.Constant;
import com.timi.timizhuo.common.ResponseData;
import com.timi.timizhuo.entity.TimiFans;
import com.timi.timizhuo.entity.TimiUser;
import com.timi.timizhuo.service.ITimiFansService;
import com.timi.timizhuo.service.TimiUserService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author timi
 * @since 2019-04-07
 */
@RestController
@RequestMapping("/fans")
public class TimiFansController extends BaseController {
    private static Logger logger = LoggerFactory.getLogger(TimiUserController.class);
    @Autowired
    private ITimiFansService timiFansService;

    @Autowired
    private TimiUserService timiUserService;

    /**
     * 当前登录用户是否关注指定用户
     * @param request
     * @param timiFans
     * @return
     */
    @PostMapping("/isFollow")
    @TimiLogin
    public ResponseData isFollow(HttpServletRequest request, TimiFans timiFans) {
        ResponseData responseData = new ResponseData();
        try {
            if (StringUtils.isEmpty(timiFans.getParentId())) {
                responseData.setFial();
                responseData.setMessage(Constant.PARAMS_NOT_NULL);
                return responseData;
            }
            timiFans.setUserId(getLoginUser(request).getId());
            responseData.setData(timiFansService.getOne(new QueryWrapper<>(timiFans)) != null);
        } catch (Exception e) {
            logger.error("m:isFollow 查询是否关注失败", e);
            responseData.setFial();
            responseData.setMessage(Constant.SYSTEM_ERROR);
        }
        return responseData;
    }

    /**
     * 关注
     *
     * @param timiFans
     * @return
     */
    @PostMapping("/follow")
    @TimiLogin
    public ResponseData follow(HttpServletRequest request, TimiFans timiFans) {
        ResponseData responseData = new ResponseData();
        try {
            if (StringUtils.isEmpty(timiFans.getParentId())) {
                responseData.setFial();
                responseData.setMessage(Constant.PARAMS_NOT_NULL);
                return responseData;
            }
            timiFans.setUserId(getLoginUser(request).getId());
            if (timiFansService.getOne(new QueryWrapper<>(timiFans)) == null) {
                timiFans.setUpdateTime(new Date());
                timiFans.setCreateTime(new Date());
                boolean save = timiFansService.save(timiFans);
                if (save) {
                    responseData.setData(Constant.FOLLOW_USER_OK);
                } else {
                    responseData.setFial();
                    responseData.setMessage("婷迷关注失败");
                }
            }
            responseData.setData(Constant.FOLLOW_USER_OK);
        } catch (Exception e) {
            logger.error("m:updateTimiUser 婷迷关注失败", e);
            responseData.setFial();
            responseData.setMessage(Constant.SYSTEM_ERROR);
        }
        return responseData;
    }

    /**
     * 关注列表传userId/粉丝列表传parentId
     *
     * @param timiFans
     * @return
     */
    @PostMapping("/followList")
    public ResponseData followList(HttpServletRequest request, TimiFans timiFans) {
        ResponseData responseData = new ResponseData();
        try {
            List<TimiFans> result = timiFansService.list(new QueryWrapper<>(timiFans));
            responseData.setSuccess();
            if (CollectionUtils.isEmpty(result)) {
                responseData.setData(Collections.EMPTY_LIST);
            } else {
                Collection<String> userIdList = new ArrayList<>();
                TimiUser loginUser = getLoginUser(request);
                if (StringUtils.isNotEmpty(timiFans.getParentId())) {
                    // 粉丝列表
                    for (TimiFans timiFans1 : result) {
                        userIdList.add(timiFans1.getUserId());
                    }
                } else {
                    // 关注列表
                    for (TimiFans timiFans1 : result) {
                        userIdList.add(timiFans1.getParentId());
                    }
                }
                Collection<TimiUser> timiUserList = timiUserService.listByIds(userIdList);
                if (loginUser != null) {
                    if (StringUtils.isNotEmpty(timiFans.getParentId())) {
                        // 查询粉丝是否被我关注
                        List<TimiFans> timiFansList = timiFansService.list(new QueryWrapper<TimiFans>().eq("user_id", loginUser.getId()).in("parent_id", userIdList));
                        timiUserList.forEach(timiUser -> {
                            timiFansList.forEach(timiFans1 -> {
                                if (timiFans1.getParentId().equals(timiUser.getId())) {
                                    timiUser.setIsFollow(true);
                                }
                            });
                        });
                    } else {
                        // 查询关注列表是否关注我
                        List<TimiFans> timiFansList = timiFansService.list(new QueryWrapper<TimiFans>().eq("parent_id", loginUser.getId()).in("user_id", userIdList));
                        timiUserList.forEach(timiUser -> {
                            timiFansList.forEach(timiFans1 -> {
                                if (timiFans1.getUserId().equals(timiUser.getId())) {
                                    timiUser.setIsFollowMe(true);
                                }
                            });
                        });
                    }
                }
                responseData.setData(timiUserList);
            }
        } catch (Exception e) {
            logger.error("m:updateTimiUser 获取婷迷关注列表/粉丝列表失败", e);
            responseData.setFial();
            responseData.setMessage(Constant.SYSTEM_ERROR);
        }
        return responseData;
    }
}
