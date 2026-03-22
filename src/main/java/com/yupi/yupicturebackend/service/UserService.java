package com.yupi.yupicturebackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yupi.yupicturebackend.model.dto.user.UserQueryRequest;
import com.yupi.yupicturebackend.model.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yupi.yupicturebackend.model.vo.LoginUserVO;
import com.yupi.yupicturebackend.model.vo.UserVO;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author ertstyuqk
 * @description 针对表【user(用户)】的数据库操作Service
 * @createDate 2026-02-14 14:09:03
 */
public interface UserService extends IService<User> {

    //用户注册
    long userRegister(String userAccount, String userPassword, String checkPassword);

    //用户登录（脱敏后）
    LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request);

    //密码加密并获取
    String getEncryptPassword(String userPassword);

    //获得脱敏后的登陆用户信息
    LoginUserVO getLoginUserVO(User user);

    //获得脱敏后的用户信息
    UserVO getUserVO(User user);

    //获得脱敏后的用户信息列表
    List<UserVO> getUserVOList(List<User> userList);

    //获取当前登录用户
    User getLoginUser(HttpServletRequest request);

    //用户注销（退出登录）
    boolean userLogout(HttpServletRequest request);

    //获取查询条件
    QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest);

    //判断是否为管理员
    Boolean isAdmin(User user);
}
