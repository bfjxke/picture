package com.yupi.yupicturebackend.utils;

import java.awt.*;

/**
 * 工具类:颜色转换
 */
public class ColorTransformUtils {
    private ColorTransformUtils() {
            //工具类不需要实例化
    }

    /**
     * 获取标准颜色，将Cos的5位色值转成6位
     */
    public static String getStandardColor(String color) {
        //每一种 rgb 色值都有可能只有一个0，要转换为 00)
        //如果是六位，不用转换，如果是五位，要给第三位后面加个 0
        //示例:0x080e0 => 0x0800e
        if (color.length() == 7) {
            color = color.substring(0, 4) + "0" + color.substring(4, 7);
        }
        return color;
    }
}