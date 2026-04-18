package com.whx.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.whx.dto.Result;
import com.whx.dto.UserDTO;
import com.whx.entity.Blog;
import com.whx.entity.BlogComments;
import com.whx.entity.User;
import com.whx.mapper.BlogCommentsMapper;
import com.whx.service.IBlogCommentsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.whx.service.IBlogService;
import com.whx.service.IUserService;
import com.whx.utils.RabbitMqConstants;
import com.whx.utils.RedisConstants;
import com.whx.utils.UserHolder;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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
public class BlogCommentsServiceImpl extends ServiceImpl<BlogCommentsMapper, BlogComments> implements IBlogCommentsService {


    private final IBlogService blogService;
    private final StringRedisTemplate stringRedisTemplate;
    private final RabbitTemplate rabbitTemplate;
    private final IUserService userService;
    @Override
    @Transactional
    public Result saveComment(BlogComments blogComments) {
        //获取当前用户
        Long userId= UserHolder.getUser().getId();;
        blogComments.setUserId(userId);
        //校验
        if(blogComments.getContent()==null){
            return Result.fail("评论内容不能为空");
        }
        //设置层级
        if(blogComments.getAnswerId()!=null&&blogComments.getAnswerId()!=0){
            //二级评论
            BlogComments answerComments=getById(blogComments.getAnswerId());
            if (answerComments == null) {
                return Result.fail("回复的评论不存在");
            }
            //如果回复的是一级评论那么父级评论id就是当前回复blogId，否则，需要拿到这个回复blog的id
            Long parentId=answerComments.getParentId()==0?answerComments.getId():answerComments.getParentId();
            blogComments.setParentId(parentId);
        }else{
            //属于一级评论,前端没有传，默认0L
            blogComments.setParentId(0L);
            blogComments.setAnswerId(0L);
        }
        //保存评论
        boolean isSave = save(blogComments);
        if(!isSave){
            return Result.fail("评论发布失败");
        }
        boolean isUpdate = blogService.lambdaUpdate().eq(Blog::getId, blogComments.getBlogId()).
                setSql("comments=comments+1").update();
        if(!isUpdate){
            return Result.fail("评论发布失败");
        }
        //TODO:这里的数据库不经脚本，大流量刷呀，之后看情况有时间优化一下
        rabbitTemplate.convertAndSend(RabbitMqConstants.BLOG_COMMENT_CACHE_EXCHANGE,RabbitMqConstants.BLOG_COMMENT_CACHE_KEY,blogComments);

        return Result.ok();

    }

    public void updateCommentCache(BlogComments blogComments) {
        String blogId=blogComments.getBlogId().toString();
        if(blogComments.getParentId()==0){
            //一级评论
            stringRedisTemplate.opsForZSet().add(RedisConstants.BLOG_COMMENTS_ROOTID_KEY+blogId,blogComments.getId().toString(),System.currentTimeMillis());
        }else{
            stringRedisTemplate.opsForHash().increment(RedisConstants.BLOG_COMMENTS_COUNT_KEY+blogId,blogComments.getParentId().toString(),1L);
        }
    }

    @Override
    public Result queryCommentList(Long blogId, Long minTime, Long offset) {
        //TODO:这里查询offset的更新逻辑我没有做，之后再说吧，也做过了，这玩意还要再建一个类来返回回去
        //查询评论列表
        String key = RedisConstants.BLOG_COMMENTS_ROOTID_KEY + blogId;
        Set<ZSetOperations.TypedTuple<String>> typedTuples = stringRedisTemplate.opsForZSet().reverseRangeByScoreWithScores(key, 0, minTime, offset, 10);
        if (typedTuples == null || typedTuples.isEmpty()) {
            return Result.ok(Collections.emptyList());
        }
        List<Long> commentIds = new ArrayList<>(typedTuples.size());
        for (ZSetOperations.TypedTuple<String> typedTuple : typedTuples) {
            String value = typedTuple.getValue();
            if (value != null) {
                commentIds.add(Long.valueOf(value));
            }
        }
        if(commentIds.isEmpty()){
            return Result.ok(Collections.emptyList());
        }
        String idStr = StrUtil.join(",", commentIds);
        List<BlogComments> comments = lambdaQuery().in(BlogComments::getId, commentIds).last("order by field(id," + idStr + ")").list();
        if (comments == null || comments.isEmpty()) {
            return Result.ok(Collections.emptyList());
        }


        //因为用户可能评论多次，所以需要用set,并将其放在一个map里面，方便等会放进评论里面
        Set<Long>userIds=comments.stream().map(BlogComments::getUserId).collect(Collectors.toSet());
        List<User> userList = userService.lambdaQuery().in(User::getId, userIds).list();
        Map<Long, UserDTO> userDTOMap = userList.stream().map(user -> BeanUtil.copyProperties(user, UserDTO.class)).
                collect(Collectors.toMap(UserDTO::getId, user -> user));


        //子评论个数
        String key2=RedisConstants.BLOG_COMMENTS_COUNT_KEY+blogId;
        List<Object> countList = stringRedisTemplate.opsForHash().multiGet(key2, commentIds.stream().map(Object::toString).collect(Collectors.toList()));
        for(int i=0;i<comments.size();i++){
            //设置用户信息
            Long userId=comments.get(i).getUserId();
            UserDTO userDTO=userDTOMap.get(userId);
            comments.get(i).setUserDTO(userDTO);

            //设置子评论个数
            Object countObj=countList.get(i);
            int replyCount=countObj!=null?Integer.parseInt(countObj.toString()):0;
            comments.get(i).setCommentCount(replyCount);
        }
        //至于子评论列表，等到用户点击展开再查询
        return Result.ok(comments);
    }

    @Override
    public Result queryReplyList(Long commentId) {
        //查询子评论列表
        List<BlogComments> list = lambdaQuery().eq(BlogComments::getParentId, commentId).list();
        if(list==null||list.isEmpty()){
            return Result.ok();
        }
        //获取用户
        Set<Long>userIds=new HashSet<>();
        for(BlogComments comment:list){
            userIds.add(comment.getUserId());
            if (comment.getAnswerId()!=null&&comment.getAnswerId()!=0){
                userIds.add(comment.getAnswerId());
            }
        }
        Map<Long, UserDTO> userDTOMap = userService.lambdaQuery().in(User::getId, userIds).list().
                stream().map(user -> BeanUtil.copyProperties(user, UserDTO.class)).
                collect(Collectors.toMap(UserDTO::getId, user -> user));

        for(BlogComments comment:list){
            //返回用户信息
            Long userId=comment.getUserId();
            UserDTO userDTO=userDTOMap.get(userId);
            comment.setUserDTO(userDTO);

            //返回回复的谁的信息
            if (comment.getAnswerId()!=null&&comment.getAnswerId()!=0){
                UserDTO replyUserDTO=userDTOMap.get(comment.getAnswerId());
                comment.setReplyUserDTO(replyUserDTO);
            }

        }
        return Result.ok(list);
    }
}
