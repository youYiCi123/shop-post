package com.jxm.upstage.service.Impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.BCrypt;
import cn.hutool.extra.spring.SpringUtil;
import cn.hutool.json.JSONUtil;
import com.jxm.common.api.CommonResult;
import com.jxm.common.api.ResultCode;
import com.jxm.common.constant.AuthConstant;
import com.jxm.common.domain.UserDto;
import com.jxm.common.exception.Asserts;
import com.jxm.common.service.RedisService;
import com.jxm.upstage.dto.UmsAdminLoginParam;
import com.jxm.upstage.dto.UmsAdminParam;
import com.jxm.upstage.feign.AuthService;
import com.jxm.upstage.mapper.UmsAdminMapper;
import com.jxm.upstage.mapper.UmsAdminRoleRelationDao;
import com.jxm.upstage.model.UmsAdmin;
import com.jxm.upstage.model.UmsRole;
import com.jxm.upstage.service.UmsAdminCacheService;
import com.jxm.upstage.service.UmsAdminService;
import com.nimbusds.jose.JWSObject;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * UmsAdminService实现类
 * Created by macro on 2018/4/26.
 */
@Service
public class UmsAdminServiceImpl implements UmsAdminService {
    private static final Logger LOGGER = LoggerFactory.getLogger(UmsAdminServiceImpl.class);

    @Autowired
    private UmsAdminMapper adminMapper;

    @Autowired
    private RedisService redisService;

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private AuthService authService;

    @Autowired
    private UmsAdminRoleRelationDao adminRoleRelationDao;

    @Override
    public UmsAdmin getAdminByUsername(String username) {
        List<UmsAdmin> adminList = adminMapper.selectAdminByUsername(username);
        if (adminList != null && adminList.size() > 0) {
            return adminList.get(0);
        }
        return null;
    }

    @Override
    public List<UmsRole> getRoleList(Long adminId) {
        return adminRoleRelationDao.getRoleList(adminId);
    }

    @Override
    public UmsAdmin register(UmsAdminParam umsAdminParam) {
        UmsAdmin umsAdmin = new UmsAdmin();
        BeanUtils.copyProperties(umsAdminParam, umsAdmin);
        umsAdmin.setCreateTime(new Date());
        umsAdmin.setStatus(1);
        //查询是否有相同用户名的用户
        List<UmsAdmin> adminList = adminMapper.selectAdminByUsername(umsAdminParam.getUsername());
        if (adminList.size() > 0) {
            return null;
        }
        //将密码进行加密操作
        String encodePassword = BCrypt.hashpw(umsAdmin.getPassword());
        umsAdmin.setPassword(encodePassword);
        adminMapper.insert(umsAdmin);
        return umsAdmin;
    }

    @Override
    public CommonResult login(UmsAdminLoginParam umsAdminLoginParam) {
        // 查询验证码
        String code = (String) redisService.get(umsAdminLoginParam.getUuid());
        // 清除验证码
        redisService.del(umsAdminLoginParam.getUuid());
        if (StringUtils.isBlank(code)) {
            return CommonResult.failed("验证码不存在或已过期");
        }
        if (StringUtils.isBlank(umsAdminLoginParam.getCode()) || !umsAdminLoginParam.getCode().equalsIgnoreCase(code)) {
            return CommonResult.failed("验证码错误");
        }

        if(StrUtil.isEmpty(umsAdminLoginParam.getUsername())||StrUtil.isEmpty(umsAdminLoginParam.getPassword())){
            Asserts.fail("用户名或密码不能为空！");
        }
        Map<String, String> params = new HashMap<>();
        params.put("client_id", AuthConstant.ADMIN_CLIENT_ID);
        params.put("client_secret","wr123456");
        params.put("grant_type","password");
        params.put("username",umsAdminLoginParam.getUsername());
        params.put("password",umsAdminLoginParam.getPassword());
        CommonResult restResult = authService.getAccessToken(params);
        if(ResultCode.SUCCESS.getCode()==restResult.getCode()&&restResult.getData()!=null){
//            updateLoginTimeByUsername(username);
//  todo          insertLoginLog(username);
        }
        return restResult;
    }

    @Override
    public UmsAdmin getCurrentAdmin() throws ParseException {
        String token = request.getHeader(AuthConstant.JWT_TOKEN_HEADER);
        if(StrUtil.isEmpty(token)){
            Asserts.fail(ResultCode.UNAUTHORIZED);
        }
        String realToken = token.replace(AuthConstant.JWT_TOKEN_PREFIX, "");
        JWSObject jwsObject = JWSObject.parse(realToken);
        String userStr = jwsObject.getPayload().toString();
        UserDto userDto = JSONUtil.toBean(userStr, UserDto.class);

        UmsAdmin admin = getCacheService().getAdmin(userDto.getId());
        if(admin!=null){
            return admin;
        }else{
            admin = adminMapper.selectByPrimaryKey(userDto.getId());
            getCacheService().setAdmin(admin);
            return admin;
        }
    }

    @Override
    public UserDto loadUserByUsername(String username){
        //获取用户信息
        UmsAdmin admin = getAdminByUsername(username);
        if (admin != null) {
            List<UmsRole> roleList = getRoleList(admin.getId());
            UserDto userDTO = new UserDto();
            BeanUtils.copyProperties(admin,userDTO);
            if(CollUtil.isNotEmpty(roleList)){
                List<String> roleStrList = roleList.stream().map(item -> item.getId() + "_" + item.getName()).collect(Collectors.toList());
                userDTO.setRoles(roleStrList);
            }
            return userDTO;
        }
        return null;
    }

    @Override
    public UmsAdminCacheService getCacheService() {
        return SpringUtil.getBean(UmsAdminCacheService.class);
    }
}
