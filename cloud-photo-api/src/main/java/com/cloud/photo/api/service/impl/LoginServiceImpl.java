package com.cloud.photo.api.service.impl;

import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.cloud.photo.common.fegin.UserFeignService;
import com.cloud.photo.api.service.LoginService;
import com.cloud.photo.common.bo.UserBo;
import com.cloud.photo.common.common.CommonEnum;
import com.cloud.photo.common.common.ResultBody;
import com.cloud.photo.common.constant.CommonConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

/**
 * @author linzsh
 */
@Service
public class LoginServiceImpl implements LoginService {

    @Autowired
    private UserFeignService userFeignService;

    @Override
    public ResultBody login(UserBo bo) {
        //用户如果当前已经登录
        if (StpUtil.isLogin()) {
            //直接登录即可
            return ResultBody.success();
        }
        //bo为空 返回登录为空的信息
        if (bo == null) {
            return ResultBody.error(CommonEnum.LOGIN_INFO_IS_NULL);
        }
        //1.如果是管理员登录
        if (bo.getRole().equals(CommonConstant.ADMIN)){
            //管理员登陆必须有账密
            if (StrUtil.hasBlank(bo.getUserName(), bo.getPassword())){
                return ResultBody.error(CommonEnum.USERNAME_PASSWORD_ERROR);
            }
            //检查账密是否正确
            ResultBody checkResult = userFeignService.checkAdmin(bo.getUserName(), bo.getPassword());
            if (!checkResult.getCode().equals(CommonEnum.SUCCESS.getResultCode())) {
                //账密不对
                return checkResult;
            }
            bo.setPhone(bo.getUserName());
            //正确就直接执行登陆
            return doLogin(bo, bo.getUserName());
        }

        //2.如果是普通用户登录
        //判断手机号是否为空或者是否为空格
        if (StrUtil.isBlank(bo.getPhone())) {
            if (StrUtil.isBlank(bo.getPhone())) {
                return ResultBody.error(CommonEnum.PHONE_IS_NULL);
            }
        }

        //判断手机号合不合法
        if (!Pattern.matches(CommonConstant.REGEX_MOBILE, bo.getPhone())) {
            return ResultBody.error(CommonEnum.PHONE_IS_NOT_VALID);
        }

        //手机号
        String phone = bo.getPhone();
        //如果没有登录 - 先检查下这个手机号是不是有记录
        ResultBody checkResult = userFeignService.checkPhone(phone);
        if (!checkResult.getCode().equals(CommonEnum.SUCCESS.getResultCode())) {
            //失败 - 表示用户是第一次登录 - 检验一下用户信息是否齐全
            if (bo.getBirth() == null || StrUtil.isBlank(bo.getDepartment()) || StrUtil.isBlank(bo.getUserName())) {
                return ResultBody.error(CommonEnum.USER_INFO_NOT_INPUT);
            }
            //用户信息补全了 - 直接执行登录
        }
        //成功 - 表示有这个用户了 - 直接执行登录
        return doLogin(bo, phone);
    }

    @Override
    public ResultBody logout() {
        if (StpUtil.isLogin()) {
            //用户登录ID
            String loginId = StpUtil.getLoginIdAsString();
            //session
            SaSession session = StpUtil.getSessionByLoginId(loginId, true);
            //清空session
            session.logout();
            //退出登录
            StpUtil.logout();
        }
        return ResultBody.success();
    }

    @Override
    public ResultBody getUserInfo() {
        if (StpUtil.isLogin()) {
            //用户登录ID
            String loginId = StpUtil.getLoginIdAsString();
            //如果session里面有信息优先从里面拿
            SaSession session = StpUtil.getSessionByLoginId(loginId);
            if (session != null) {
                String userInfo = session.getModel(CommonConstant.USER_INFO, String.class);
                if (userInfo != null) {
                    return ResultBody.success(JSON.parseObject(userInfo, UserBo.class));
                }
            }
            //否则访问user服务获取用户信息
            ResultBody userInfo = userFeignService.getUserInfo(loginId);
            if (userInfo.getCode().equals(CommonEnum.SUCCESS.getResultCode())){
                //成功拿到用户信息就存缓存里面
                SaSession saSession = StpUtil.getSessionByLoginId(loginId, true);
                saSession.set(CommonConstant.USER_INFO, JSON.toJSONString(userInfo.getData()));
            }
            return userInfo;
        }
        //未登录
        return ResultBody.error(CommonEnum.NO_LOGIN);
    }

    /**
     * 执行登录
     *
     * @param bo  用户信息体
     * @param phone 手机号
     * @return 返回登录结果
     */
    private ResultBody doLogin(UserBo bo, String phone) {
        ResultBody loginResult = userFeignService.login(bo);
        if (loginResult.getCode().equals(CommonEnum.SUCCESS.getResultCode())) {
            //登陆成功 - 后台也执行登录
            StpUtil.login(phone);
            if (StpUtil.isLogin()) {
                //后台也登录成功了就返回token到前端
                String tokenValue = StpUtil.getTokenInfo().getTokenValue();
                return ResultBody.success(tokenValue);
            }
        } else {

            return loginResult;
        }
        //登陆失败 - 直接返回登录失败信息
        return loginResult;
    }
}
