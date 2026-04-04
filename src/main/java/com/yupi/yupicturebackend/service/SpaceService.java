package com.yupi.yupicturebackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yupi.yupicturebackend.model.dto.space.SpaceAddRequest;
import com.yupi.yupicturebackend.model.dto.space.SpaceQueryRequest;
import com.yupi.yupicturebackend.model.dto.space.analyze.SpaceCategoryAnalyzeRequest;
import com.yupi.yupicturebackend.model.dto.space.analyze.SpaceTagAnalyzeRequest;
import com.yupi.yupicturebackend.model.entity.Space;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yupi.yupicturebackend.model.entity.User;
import com.yupi.yupicturebackend.model.vo.SpaceVO;
import com.yupi.yupicturebackend.model.vo.space.analyze.SpaceCategoryAnalyzeResponse;
import com.yupi.yupicturebackend.model.vo.space.analyze.SpaceTagAnalyzeResponse;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author ertstyuqk
 * @description 针对表【space(空间)】的数据库操作Service
 * @createDate 2026-03-20 09:26:13
 */
public interface SpaceService extends IService<Space> {

    /**
     * 创建空间
     */
    long addSpace(SpaceAddRequest spaceAddRequest, User loginUser);


    /**
     * @param space
     * @param add   是否是创建时校验
     */
    void validSpace(Space space, boolean add);


    Page<SpaceVO> getSpaceVOPage(Page<Space> spacePage, HttpServletRequest request);


    //获取查询对象
    QueryWrapper<Space> getQueryWrapper(SpaceQueryRequest spaceQueryRequest);

    SpaceVO getSpaceVO(Space space, HttpServletRequest request);

    /**
     * 根据空间级别填充空间对象
     * @param space
     */
    void fillSpaceBySpaceLevel(Space space);

    /**
     * 校验空间权限
     */
    void checkSpaceAuth( User loginUser,Space space);


}
