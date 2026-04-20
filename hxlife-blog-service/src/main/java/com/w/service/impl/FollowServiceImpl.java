package com.w.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.BooleanUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.w.client.UserClient;
import com.w.dto.Result;
import com.w.dto.UserDTO;
import com.w.entity.Follow;
import com.w.entity.User;
import com.w.mapper.FollowMapper;
import com.w.service.IFollowService;
import com.w.utils.RedisConstants;
import com.w.utils.UserHolder;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author wumm
 * @since 2026-04-10
 */
@RequiredArgsConstructor
@Service
public class FollowServiceImpl extends ServiceImpl<FollowMapper, Follow> implements IFollowService {

    private final UserClient userService;
    private final StringRedisTemplate stringRedisTemplate;
    @Override
    @Transactional
    public Result follow(Long id, Boolean isFollow) {
        Long userId= UserHolder.getUser().getId();
        //判断是否关注
        //用redis来放当前用户关注列表的用户id
        String key= RedisConstants.USER_FOLLOW_KEY+userId;
        if(!BooleanUtil.isTrue(isFollow)){
            Boolean isMember = stringRedisTemplate.opsForSet().isMember(key,id.toString());
            if(BooleanUtil.isTrue(isMember)){
                return Result.fail("您已关注该用户");
            }
            //关注
            Follow follow=new Follow();
            follow.setUserId(userId);
            follow.setFollowUserId(id);
            follow.setCreateTime(LocalDateTime.now());
            boolean isSuccuss = save(follow);
            if(isSuccuss){
                stringRedisTemplate.opsForSet().add(key,id.toString());
            }else{
                return Result.fail("系统繁忙，关注失败");
            }
            return Result.ok("关注成功");

        }else{
            //取关
            Boolean isMember = stringRedisTemplate.opsForSet().isMember(RedisConstants.USER_FOLLOW_KEY+userId,id.toString());
            if(!BooleanUtil.isTrue(isMember)){
                return Result.fail("您未关注该用户");
            }
            Follow follow = lambdaQuery().eq(Follow::getFollowUserId,id).eq(Follow::getUserId, userId).one();
            if(follow==null){
                return Result.fail("此人已不再您的关注列表中");
            }
            //取关
            boolean isSuccuss = removeById(follow);
            if(isSuccuss){
                stringRedisTemplate.opsForSet().remove(key,id.toString());
            }else{
                return Result.fail("系统繁忙，取关失败");
            }
            return Result.ok("取关成功");
        }


    }

    @Override
    public Result commonFollows(Long id) {
        Long userId=UserHolder.getUser().getId();
        //利用redis的intersect来获取共同关注
        String key1=RedisConstants.USER_FOLLOW_KEY+userId;
        String key2=RedisConstants.USER_FOLLOW_KEY+id;
        //这里是一个redis客户端bug，如果是调用（key1，key2）会不小心走上一个重载方法导致无限递归弄得我栈溢出
        Set<String> commonFollows = stringRedisTemplate.opsForSet().intersect(key1, Collections.singleton(key2));
        if(commonFollows==null||commonFollows.isEmpty()){
            return Result.ok("您和该用户没有共同关注");
        }
        List<Long> commons = commonFollows.stream().map(Long::valueOf).toList();
        if(CollUtil.isEmpty(commons)){
            return Result.ok("您和该用户没有共同关注");
        }
        List<User> users = userService.list(commons);
        List<UserDTO> result = users.stream().map(user -> BeanUtil.copyProperties(user, UserDTO.class)).toList();

        return Result.ok(result);
    }

}
