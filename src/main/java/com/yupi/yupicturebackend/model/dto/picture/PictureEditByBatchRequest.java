package com.yupi.yupicturebackend.model.dto.picture;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class PictureEditByBatchRequest implements Serializable {
    //*图片id列表
    private List<Long> pictureIdList;
    //空间id¥
    private Long spaceId;
    //*分类
    private String category;

    //*标签
    private List<String> tags;
    /**
     * 命名规则
     */
    private String nameRule;

    private static final long serialVersionUID = 1L;

}