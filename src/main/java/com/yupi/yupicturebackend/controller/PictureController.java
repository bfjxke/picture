package com.yupi.yupicturebackend.controller;


import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.yupi.yupicturebackend.annotation.AuthCheck;
import com.yupi.yupicturebackend.api.aliyunai.AliYunAiApi;
import com.yupi.yupicturebackend.api.aliyunai.model.CreateOutPaintingTaskResponse;
import com.yupi.yupicturebackend.api.aliyunai.model.GetOutPaintingTaskResponse;
import com.yupi.yupicturebackend.api.imagesearch.ImageSearchApiFacade;
import com.yupi.yupicturebackend.api.imagesearch.model.ImageSearchResult;
import com.yupi.yupicturebackend.common.BaseResponse;
import com.yupi.yupicturebackend.common.DeleteRequest;
import com.yupi.yupicturebackend.common.ResultUtils;
import com.yupi.yupicturebackend.constant.UserConstant;
import com.yupi.yupicturebackend.exception.BusinessException;
import com.yupi.yupicturebackend.exception.ErrorCode;
import com.yupi.yupicturebackend.exception.ThrowUtils;
import com.yupi.yupicturebackend.model.dto.picture.*;
import com.yupi.yupicturebackend.model.dto.space.SpaceLevel;
import com.yupi.yupicturebackend.model.entity.Picture;
import com.yupi.yupicturebackend.model.entity.Space;
import com.yupi.yupicturebackend.model.entity.User;
import com.yupi.yupicturebackend.model.enums.PictureReviewStatusEnum;
import com.yupi.yupicturebackend.model.enums.SpaceLevelEnum;
import com.yupi.yupicturebackend.model.vo.PictureTagCategory;
import com.yupi.yupicturebackend.model.vo.PictureVO;
import com.yupi.yupicturebackend.service.PictureService;
import com.yupi.yupicturebackend.service.SpaceService;
import com.yupi.yupicturebackend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.cli.Digest;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.time.Duration;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/picture")
public class PictureController {

    @Resource
    private UserService userService;
    @Resource
    private PictureService pictureService;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private SpaceService spaceService;
    @Resource
    private AliYunAiApi aliYunAiApi;


    private final LoadingCache<String, String> LOCAL_CACHE = Caffeine.newBuilder()
            .initialCapacity(1024)
            .maximumSize(10_000L)
            .expireAfterWrite(Duration.ofMinutes(5))
            .refreshAfterWrite(Duration.ofMinutes(1))
            .build(key -> {
                // 从Redis加载数据
                ValueOperations<String, String> opsForValue = stringRedisTemplate.opsForValue();
                return opsForValue.get(key);
            });


    /**
     * 上传图片（可重新上传）
     * @param multipartFile
     * @param pictureUploadRequest
     * @param request
     * @return
     */
    @PostMapping("/upload")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<PictureVO> uploadPicture(@RequestPart("file") MultipartFile multipartFile
            , PictureUploadRequest pictureUploadRequest, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        PictureVO picturevO = pictureService.uploadPicture(multipartFile, pictureUploadRequest, loginUser);
        return ResultUtils.success(picturevO);

    }


    /**
     * 通过url上传图片（可重新上传）
     *
     * @return
     */
    @PostMapping("/upload/url")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<PictureVO> uploadPictureByUrl(@RequestBody PictureUploadRequest pictureUploadRequest, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        String fileUrl = pictureUploadRequest.getFileUrl();
        PictureVO picturevO = pictureService.uploadPicture(fileUrl, pictureUploadRequest, loginUser);
        return ResultUtils.success(picturevO);

    }


    /**
     * 删除图片
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deletePicture(@RequestBody DeleteRequest deleteRequest
            , HttpServletRequest request) {
        ThrowUtils.throwIf(deleteRequest==null||deleteRequest.getId()<=0,ErrorCode.NOT_FOUND_ERROR);
        User loginUser = userService.getLoginUser(request);
        pictureService.deletePicture(deleteRequest.getId(),loginUser);
        return ResultUtils.success(true);
    }


    /**
     * 更新图片(管理员)
     * @param pictureUpdateRequest
     * @return
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updatePicture(@RequestBody PictureUpdateRequest pictureUpdateRequest, HttpServletRequest request) {
        if (pictureUpdateRequest == null || pictureUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 将实体类和 DTO进行转换
        Picture picture = new Picture();
        BeanUtils.copyProperties(pictureUpdateRequest, picture);
        //注意将list 转为 string
        picture.setTags(JSONUtil.toJsonStr(pictureUpdateRequest.getTags()));
        //数据校验
        pictureService.validPicture(picture);
        //判断是否存在
        long id = pictureUpdateRequest.getId();

        Picture oldPicture = pictureService.getById(id);
        ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR);

        User loginUser = userService.getLoginUser(request);
        //补充审核参数
        pictureService.fillReviewParams(oldPicture, loginUser);

        // 操作数据库
        boolean result = pictureService.updateById(picture);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }


    /**
     * 根据id获取图片（管理员）
     * @param id
     * @param request
     * @return
     */
    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Picture> getPictureById(long id, HttpServletRequest request) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        //查询数据库
        Picture picture = pictureService.getById(id);
        ThrowUtils.throwIf(picture == null, ErrorCode.NOT_FOUND_ERROR);
        //获取封装类
        return ResultUtils.success(picture);
    }


    /**
     * 根据id获取图片（封装类）
     * @param id
     * @param request
     * @return
     */
    @GetMapping("/get/vo")
    public BaseResponse<PictureVO> getPicturevoById(long id, HttpServletRequest request) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        //查询数据库
        Picture picture = pictureService.getById(id);
        ThrowUtils.throwIf(picture == null, ErrorCode.NOT_FOUND_ERROR);
        //空间权限校验
        Long spaceId = picture.getSpaceId();
        if(spaceId!=null){
            User loginUser = userService.getLoginUser(request);
            pictureService.checkPictureAuth(loginUser ,picture);
        }
        //获取封装类
        return ResultUtils.success(pictureService.getPictureVO(picture, request));
    }



    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<Picture>> listPictureByPage (@RequestBody PictureQueryRequest pictureQueryRequest) {
        long current = pictureQueryRequest.getCurrent();
        long size = pictureQueryRequest.getPageSize();
        //查询数据库
        Page<Picture> picturePage = pictureService.page(new Page<>(current, size),
                pictureService.getQueryWrapper(pictureQueryRequest));
        return ResultUtils.success(picturePage);
    }



    /**
    *分页获取图片列表(封装类)
    */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<PictureVO>> listPictureVOByPage(@RequestBody PictureQueryRequest pictureQueryRequest
         ,HttpServletRequest request) {

        long current = pictureQueryRequest.getCurrent();
        long size = pictureQueryRequest.getPageSize();
        //限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Long spaceId = pictureQueryRequest.getSpaceId();
        if(spaceId==null){
            //公开图库
            //普通用户只能看见审核通过的
            pictureQueryRequest.setReviewStatus(PictureReviewStatusEnum.PASS.getValue());
             pictureQueryRequest.setNullSpaceId(true);
        }else{
            //私有空间
            User loginUser=userService.getLoginUser(request);
            Space space = spaceService.getById(spaceId);
            ThrowUtils. throwIf( space == null, ErrorCode.NOT_FOUND_ERROR,"空间不存在");
            if (!loginUser.getId().equals(space.getUserId())) {
                 throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "没有空间权限");
            }
        }
        //用户默认只能看到审核通过的数据
        pictureQueryRequest.setReviewStatus(PictureReviewStatusEnum.PASS.getValue());
        //查询数据库
        Page<Picture> picturePage = pictureService.page(new Page<>(current, size)
                 , pictureService.getQueryWrapper(pictureQueryRequest));
        //获取封装类
        return ResultUtils.success(pictureService.getPictureVOPage(picturePage, request));
    }


    /**
     * 分页获取图片列表(封装类，有缓存)
     */
    @Deprecated
    @PostMapping("/list/page/vo/cache")
    public BaseResponse<Page<PictureVO>> listPictureVOByPageWithCache(@RequestBody PictureQueryRequest pictureQueryRequest
            , HttpServletRequest request) {


        long current = pictureQueryRequest.getCurrent();
        long size = pictureQueryRequest.getPageSize();
        //限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        //用户默认只能看到审核通过的数据
        pictureQueryRequest.setReviewStatus(PictureReviewStatusEnum.PASS.getValue());
        //查询缓存 缓存中没有再查询数据库
        //构建缓存的key
        String queryCondition = JSONUtil.toJsonStr(pictureQueryRequest);
        String hashKey = DigestUtils.md5DigestAsHex(queryCondition.getBytes());
        String cacheKey = String.format("yupicture:listPictureVOByPageWithCache:%s", hashKey);
        //1.先从本地缓存查询数据
        String cachedValue = LOCAL_CACHE.get(cacheKey);
        if (cachedValue != null) {
            //缓存命中，直接返回
            Page<PictureVO> cachedPage = JSONUtil.toBean(cachedValue, Page.class);
            return ResultUtils.success(cachedPage);
        }

        //2.操作redis 查询缓存
        ValueOperations<String, String> opsForValue = stringRedisTemplate.opsForValue();
        cachedValue = opsForValue.get(cacheKey);
        if (cachedValue != null) {
            //若缓存命中 更新本地缓存 并返回结果
            LOCAL_CACHE.put(cacheKey, cachedValue);
            Page<PictureVO> cachedPage = JSONUtil.toBean(cachedValue, Page.class);
            return ResultUtils.success(cachedPage);
        }

        //3.查询数据库
        Page<Picture> picturePage = pictureService.page(new Page<>(current, size)
                , pictureService.getQueryWrapper(pictureQueryRequest));
        Page<PictureVO> pictureVOPage = pictureService.getPictureVOPage(picturePage, request);
        //存入redis缓存
        String cacheValue = JSONUtil.toJsonStr(pictureVOPage);
        //设置过期时间，防止缓存雪崩
        int cacheExpireTime = 300 + RandomUtil.randomInt(0, 300);
        opsForValue.set(cacheKey, cacheValue, cacheExpireTime);
        //写入本地缓存
        LOCAL_CACHE.put(cacheKey, cacheValue);
        //获取封装类
        return ResultUtils.success(pictureVOPage);
    }

    /**
     * 编辑图片（用户）
     *
     * @param pictureEditRequest
     * @param request
     * @return
     */
    @PostMapping("/edit")
    public BaseResponse<Boolean> editPicture(@RequestBody PictureEditRequest pictureEditRequest, HttpServletRequest request) {
        if (pictureEditRequest == null || pictureEditRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        pictureService.editPicture(pictureEditRequest,loginUser);
        return ResultUtils.success(true);

    }

    /**
     * 前端列表视图
     *
     * @return
     */
    @GetMapping("/tag_category")
    public BaseResponse<PictureTagCategory> listPictureTagCategory() {
        PictureTagCategory pictureTagCategory = new PictureTagCategory();
        List<String> tagList = Arrays.asList("热门", "搞笑", "生活", "高清", "艺术", "校园", "背景", "简历", "创意");
        List<String> categoryList = Arrays.asList("模板", "电商", "表情包", "素材", "海报");
        pictureTagCategory.setTagList(tagList);
        pictureTagCategory.setCategoryList(categoryList);
        return ResultUtils.success(pictureTagCategory);
    }


    /**
     * 审核图片（管理员）
     *
     * @param request
     * @return
     */
    @PostMapping("/review")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> doPictureReview(@RequestBody PictureReviewRequest pictureReviewRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(pictureReviewRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        pictureService.doPictureReview(pictureReviewRequest, loginUser);
        return ResultUtils.success(true);
    }


    /**
     * 批量抓取并创建
     *
     * @param request
     * @return
     */
    @PostMapping("/upload/batch")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Integer> uploadPictureByBatch(@RequestBody PictureUploadByBatchRequest pictureUploadByBatchRequest
            , HttpServletRequest request) {
        ThrowUtils.throwIf(pictureUploadByBatchRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        int uploadCount = pictureService.uploadPictureByBatch(pictureUploadByBatchRequest, loginUser);
        return ResultUtils.success(uploadCount);
    }

    /**
     * 获取空间级别列表，便于前端展示
     * @return
     */
    @GetMapping("/list/level")
    public BaseResponse<List<SpaceLevel>>listSpaceLevel(){
        List<SpaceLevel> spaceLevelList = Arrays.stream(SpaceLevelEnum.values())
                .map(spaceLevelEnum -> new SpaceLevel(
                        spaceLevelEnum.getValue(),
                        spaceLevelEnum.getText(),
                        spaceLevelEnum.getMaxCount(),
                        spaceLevelEnum.getMaxSize()
        ))
                .collect(Collectors.toList());
        return ResultUtils.success(spaceLevelList);
    }


    /**
     * 以图搜图
     */
    @PostMapping("/search/picture")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<List<ImageSearchResult>> searchPictureByPicture(@RequestBody SearchPictureByPictureRequest searchPictureByPictureRequest) {
        ThrowUtils.throwIf(searchPictureByPictureRequest == null, ErrorCode.PARAMS_ERROR);
        Long pictureId = searchPictureByPictureRequest.getPictureId();
        ThrowUtils.throwIf(pictureId == null, ErrorCode.PARAMS_ERROR);
        Picture picture = pictureService.getById(pictureId);
        ThrowUtils.throwIf(picture == null, ErrorCode.NOT_FOUND_ERROR);
        List<ImageSearchResult> resultList = ImageSearchApiFacade.searchImage(picture.getUrl());
        return ResultUtils.success(resultList);
    }


    /**
     * 按颜色搜图
     */
    @PostMapping("/search/color")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<List<PictureVO>> searchPictureByColor(@RequestBody SearchPictureByColorRequest searchPictureByColorRequest,HttpServletRequest request) {
        ThrowUtils.throwIf(searchPictureByColorRequest == null, ErrorCode.PARAMS_ERROR);
        String picColor = searchPictureByColorRequest.getPicColor();
        Long spaceId = searchPictureByColorRequest.getSpaceId();
        User loginUser = userService.getLoginUser(request);
        List<PictureVO> pictureVOList = pictureService.searchPictureByColor(spaceId, picColor, loginUser);
        return ResultUtils.success(pictureVOList);
    }


    /**
     * 批量编辑图片
     */
    @PostMapping("/edit/batch")
    public BaseResponse<Boolean> editPictureByBatch(@RequestBody PictureEditByBatchRequest pictureEditByBatchRequest,HttpServletRequest request) {
        ThrowUtils.throwIf(pictureEditByBatchRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        pictureService.editPictureByBatch(pictureEditByBatchRequest, loginUser);
        return ResultUtils.success(true);
    }

    /**
     * 创建ai扩图任务接口
     */
    @PostMapping("/create/ai_out_painting_task")
    public BaseResponse<CreateOutPaintingTaskResponse> createPictureOutPaintingTask(@RequestBody CreatePictureOutPaintingTaskRequest createPictureOutPaintingTaskRequest, HttpServletRequest request) {
        if (createPictureOutPaintingTaskRequest == null || createPictureOutPaintingTaskRequest.getPictureId() == null)
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        CreateOutPaintingTaskResponse response = pictureService.createPictureOutPaintingTask(createPictureOutPaintingTaskRequest, loginUser);
        return ResultUtils.success(response);
    }

    /**
     * 查询ai扩图任务接口
     */
    @GetMapping("/out_painting/get_task")
    public BaseResponse<GetOutPaintingTaskResponse> getPictureOutPaintingTask(String taskId) {
        ThrowUtils.throwIf(StrUtil.isBlank(taskId), ErrorCode.PARAMS_ERROR);
        GetOutPaintingTaskResponse task = aliYunAiApi.getOutPaintingTask(taskId);
        return ResultUtils.success(task);
    }
}
