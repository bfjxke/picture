package com.yupi.yupicturebackend.manager.auth;

import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.yupi.yupicturebackend.manager.auth.model.SpaceUserAuthConfig;
import com.yupi.yupicturebackend.manager.auth.model.SpaceUserRole;
import com.yupi.yupicturebackend.service.SpaceUserService;
import com.yupi.yupicturebackend.service.UserService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * 空间成员权限管理
 */
@Component
public class SpaceUserAuthManager {

    @Resource
    private UserService userService;

    @Resource
    private SpaceUserService spaceUserService;

    public static final SpaceUserAuthConfig SPACE_USER_AUTH_CONFIG;

    static {
        String json = ResourceUtil.readUtf8Str("biz/spaceUserAuthConfig.json");
        SPACE_USER_AUTH_CONFIG = JSONUtil.toBean(json, SpaceUserAuthConfig.class);
    }

    /**
     * 根据角色获取权限列表
     * @param spaceUserRole
     * @return
     */
    List<String> getPermissionsByRole(String spaceUserRole){
        if(StrUtil.isBlank(spaceUserRole)){
            return new ArrayList<>();
        }
        SpaceUserRole role = SPACE_USER_AUTH_CONFIG.getRoles().stream()
                .filter(r -> r.getKey().equals(spaceUserRole))
                .findFirst()//拿到找到的信息
                .orElse(null);
        if(role==null){
            return new ArrayList<>();
        }
        return role.getPermissions();
    }





}