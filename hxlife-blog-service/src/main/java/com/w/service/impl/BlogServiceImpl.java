package com.w.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.w.client.UserClient;
import com.w.dto.Result;
import com.w.dto.UserDTO;
import com.w.entity.Blog;
import com.w.entity.Follow;
import com.w.entity.ScrollResult;
import com.w.entity.User;
import com.w.mapper.BlogMapper;
import com.w.service.IBlogService;
import com.w.service.IFollowService;
import com.w.utils.RabbitMqConstants;
import com.w.utils.RedisConstants;
import com.w.utils.UserHolder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

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
public class BlogServiceImpl extends ServiceImpl<BlogMapper, Blog> implements IBlogService {

    private final StringRedisTemplate stringRedisTemplate;
    private final IFollowService followService;
    private final RabbitTemplate rabbitTemplate;
    private final UserClient userService;

    @Override
    public Result saveBlog(Blog blog) {
        Long userId= UserHolder.getUser().getId();
        if(blog==null){
            return Result.fail("博客不能为空");
        }
        boolean isSave = save(blog);
        if(!isSave){
            return Result.fail("保存失败，请重试");
        }
        Map<String,Object>map=new HashMap<>();
        map.put("blogId",blog.getId());
        map.put("userId",userId);
        rabbitTemplate.convertAndSend(RabbitMqConstants.FEED_EXCHANGE,RabbitMqConstants.FEED_KEY,map);

        return Result.ok(blog.getId());


    }

    @Override
    public Result feedToFans(String blogId,String userId) {
        //推送到关注该用户的收件箱

        List<Follow> follows = followService.lambdaQuery().eq(Follow::getFollowUserId, userId).list();
        if(CollUtil.isEmpty(follows)){
            return Result.ok(blogId);
        }
        List<String> followsKeyId = follows.stream().
                map(follow -> RedisConstants.FEED_KEY+follow.getUserId()).
                toList();
        //相比于LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)，使用System.currentTimeMillis()精度更高
        long epochSecond = System.currentTimeMillis();
        for(String key:followsKeyId){
            stringRedisTemplate.opsForZSet().add(key, blogId,epochSecond);
        }
        return Result.ok(blogId);
    }

    @Override
    public Result queryFollowBlog(Long lastTime, Long offset) {
        Long userId= UserHolder.getUser().getId();
        String key=RedisConstants.FEED_KEY+userId;

        Set<ZSetOperations.TypedTuple<String>> typedTuples = stringRedisTemplate.opsForZSet().
                reverseRangeByScoreWithScores(
                key, 0, lastTime, offset, 2
        );
        if(typedTuples==null||typedTuples.isEmpty()){
            return Result.ok();
        }

        int os=1;
        long minTime=Integer.MAX_VALUE;
        List<Long>ids=new ArrayList<>(typedTuples.size());
        for(ZSetOperations.TypedTuple<String> typedTuple:typedTuples) {
            Long value = Long.valueOf(typedTuple.getValue());
            long score = typedTuple.getScore().longValue();
            ids.add(value);
            if(score==minTime){
                //如果相等，偏移量加一
                os++;
            }else{
                //如果不同，重置偏移量为1
                minTime=score;
                os=1;
            }

        }
        //从mysql查询出来的播客可能会乱，排序一下
        String idsStr= StrUtil.join(",",ids);
        List<Blog> result = lambdaQuery().in(Blog::getId, ids).
                last("order by field(id,"+idsStr+")").
                list();
        if(result==null||result.isEmpty()){
            return Result.ok();
        }
        ScrollResult scrollResult=new ScrollResult();
        scrollResult.setData(result);
        scrollResult.setOffset(os);
        scrollResult.setMinTime(minTime);

        return Result.ok(scrollResult);

    }

    @Override
    @Transactional
    public Result likeBlog(Long id) {
        Long userId=UserHolder.getUser().getId();
        String key = RedisConstants.BLOG_LIKED_KEY+id;
        boolean isLike=false;
        Double score = stringRedisTemplate.opsForZSet().score(key, userId.toString());
        if(score==null){
            //没有点赞过
            boolean isSuccess = lambdaUpdate().eq(Blog::getId, id).setSql("liked=liked+1").update();
            if(!isSuccess){
                return Result.fail("点赞失败，请重试");
            }
            stringRedisTemplate.opsForZSet().add(key,userId.toString(),System.currentTimeMillis());
            isLike=true;
        }else{
            //取消点赞
            boolean update = lambdaUpdate().eq(Blog::getId, id).setSql("liked=liked-1").update();
            if (!update) {
                return Result.fail("取消失败，请重试");

            }
            stringRedisTemplate.opsForZSet().remove(key,userId.toString());
            isLike=false;
        }
        //渲染点赞过的用户
        long timeStamp=System.currentTimeMillis();
        Set<String> strings = stringRedisTemplate.opsForZSet().reverseRangeByScore(key, 0, timeStamp, 0, 5);
        if(strings==null||strings.isEmpty()){
            return Result.ok(new LikeInfo(Collections.emptyList(),false,0));
        }
        List<Long> ids = strings.stream().map(Long::valueOf).toList();
        List<User> users = userService.list(ids);
        List<UserDTO> result = users.stream().map(user -> BeanUtil.copyProperties(user, UserDTO.class)).toList();
        //渲染点赞过的用户
        Integer liked = lambdaQuery().eq(Blog::getId, id).select(Blog::getLiked).one().getLiked();

        return Result.ok(new LikeInfo(result, isLike, liked));

    }
    @Data
    @AllArgsConstructor
    private static class LikeInfo{
        private List<UserDTO>likes;
        private Boolean isLike;
        private Integer liked;

    }


}
