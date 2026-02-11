package com.yupi.yupicturebackend.common;

import lombok.Data;

/**
 * 通用分页请求类
 */
@Data
public class PageRequest {
 
 private int current = 1;
 
 private int pageSize = 10;
 
 private String sortField;
 
 private String sortOrder = "descend";
}