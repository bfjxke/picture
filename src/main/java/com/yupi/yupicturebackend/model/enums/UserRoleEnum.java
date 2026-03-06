package com.yupi.yupicturebackend.model.enums;


import cn.hutool.core.util.ObjUtil;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * 用户角色枚举
 */
@Getter//自动生成get方法
public enum UserRoleEnum {

    USER("用户", "user"),
    ADMIN("管理员", "admin");

    private final String text;
    private final String value;

    UserRoleEnum(String text, String value) {
        this.text = text;
        this.value = value;
    }


    public static UserRoleEnum getEnumByValue(String value) {
        if (ObjUtil.isEmpty(value)) {
            return null;
        }
        for (UserRoleEnum userRoleEnum : UserRoleEnum.values()) {
            if (userRoleEnum.value.equals(value)) {
                return userRoleEnum;
            }
        }
        return null;

        //可选择优化效率(map直接查找)
//        Map<String,UserRoleEnum> map =new HashMap<>();
//        map.put("admin",ADMIN);
    }

}
