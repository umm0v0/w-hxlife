package com.w.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.w.dto.LoginFormDTO;
import com.w.dto.Result;
import com.w.dto.UserDTO;
import com.w.entity.User;
import com.w.mapper.UserMapper;
import com.w.service.IUserService;
import com.w.utils.RedisConstants;
import com.w.utils.RegexUtils;
import com.w.utils.SystemConstants;
import com.w.utils.UserHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.redis.connection.BitFieldSubCommands;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author wumm
 * @since 2026-04-10
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {
    private final StringRedisTemplate stringRedisTemplate;


    @Override
    public Result sendCode(String phone) {
        //验证手机号是否是有效的
        if(RegexUtils.isPhoneInvalid(phone)){
            return Result.fail("手机号格式错误");
        }
        // 发送验证码
        String code = RandomUtil.randomNumbers(6);

        stringRedisTemplate.opsForValue().set(RedisConstants.LOGIN_CODE_KEY+phone,code);
        stringRedisTemplate.expire(RedisConstants.LOGIN_CODE_KEY+phone,RedisConstants.LOGIN_CODE_TTL, TimeUnit.MINUTES);
        log.debug("验证码发送成功:{}",code);
        return Result.ok();


    }

    @Override
    public Result login(LoginFormDTO loginFormDTO) {
        //验证手机号是否符合格式
        if (RegexUtils.isPhoneInvalid(loginFormDTO.getPhone())) {
            return Result.fail("手机号格式错误");
        }
        //验证验证码是否符合格式
        if (RegexUtils.isCodeInvalid(loginFormDTO.getCode())) {
            return Result.fail("验证码格式错误");
        }
        //检验验证码是否正确
        String key= RedisConstants.LOGIN_CODE_KEY+ loginFormDTO.getPhone();
        String s = stringRedisTemplate.opsForValue().get(key);
        if(s==null||!loginFormDTO.getCode().equals(s)){
            return Result.fail("验证码错误");
        }

        //验证成功
        User user=lambdaQuery().eq(User::getPhone,loginFormDTO.getPhone()).one();
        if(user==null){
            //那么直接创建一个新的用户
            user=createUser(loginFormDTO.getPhone());
        }
        //设置登录的一个token
        String token= UUID.randomUUID().toString();
        UserDTO userDTO= BeanUtil.copyProperties(user, UserDTO.class);
        Map<String, Object> map = BeanUtil.beanToMap(userDTO, new HashMap<>(),
                CopyOptions.create().setIgnoreNullValue(true).
                        setFieldValueEditor((fieldName, fieldValue) -> fieldValue.toString()));
        stringRedisTemplate.opsForHash().putAll(RedisConstants.LOGIN_USER_KEY+token,map);
        stringRedisTemplate.expire(RedisConstants.LOGIN_USER_KEY+token,RedisConstants.LOGIN_USER_TTL,TimeUnit.MINUTES);
        return Result.ok(token);


    }

    @Override
    public Result sign() {
        //获取用户
        UserDTO user= UserHolder.getUser();
        if(user==null){
            return Result.fail("获取用户失败");
        }
        Long userId= user.getId();
        LocalDateTime now = LocalDateTime.now();
        int dayOfMonth = now.getDayOfMonth();
        String nowStr=now.format(DateTimeFormatter.ofPattern(":yyyyMM"));
        String key=RedisConstants.USER_SIGN_KEY+userId+nowStr;
        //存入redis
        stringRedisTemplate.opsForValue().setBit(key,dayOfMonth-1,true);
        return Result.ok();
    }


    @Override
    public Result signCount() {
        Long userId = UserHolder.getUser().getId();
        LocalDateTime now = LocalDateTime.now();
        String format = now.format(DateTimeFormatter.ofPattern(":yyyyMM"));
        String key=RedisConstants.USER_SIGN_KEY+userId+format;
        List<Long> results = stringRedisTemplate.opsForValue().bitField(key,
                BitFieldSubCommands.create().
                        get(BitFieldSubCommands.BitFieldType.unsigned(now.getDayOfMonth())).valueAt(0));
        if(results==null||results.isEmpty()){
            return Result.ok(0);
        }
        //这里看似是一个集合实际上就是一个二进制装换成10进制的一个数
        Long l = results.get(0);
        if(l==null||l==0){
            return Result.ok(0);
        }
        //得到最晚签到天数，指的是从最后一天开始的
        int count=0;
        while(true){
            if((l&1)==0){
                break;
            }else{
                count++;
                l>>>=1;
            }

        }
        return Result.ok(count);

    }

    private User createUser(String phone) {
        User user=new User();
        user.setPhone(phone);
        user.setNickName(SystemConstants.USER_NICK_NAME_PREFIX+RandomUtil.randomString(10));
        save(user);
        return user;
    }

}
