package com.jxm.upstage.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;

/**
 * 用户登录参数
 * Created by macro on 2018/4/26.
 */
@Getter
@Setter
public class UmsAdminParam {
    @ApiModelProperty(value = "用户名", required = true)
    @NotEmpty
    private String username;
    @ApiModelProperty(value = "密码", required = true)
    @NotEmpty
    private String password;
    @ApiModelProperty(value = "部门")
    private Long depId;
    @ApiModelProperty(value = "手机号")
    private String phone;
    @ApiModelProperty(value = "用户头像")
    private String icon;
    @ApiModelProperty(value = "性别")
    private Integer sex;
    @ApiModelProperty(value = "邮箱")
    @Email
    private String email;
    @ApiModelProperty(value = "职务")
    private String duty;
    @ApiModelProperty(value = "用户昵称")
    private String nickName;
    @ApiModelProperty(value = "备注")
    private String note;
}
