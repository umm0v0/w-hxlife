package com.w.controller;



import com.w.dto.Result;
import com.w.entity.BlogComments;
import com.w.service.IBlogCommentsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author wumm
 * @since 2026-04-10
 */
@Tag(name = "博客评论接口")
@RequiredArgsConstructor
@RestController
@RequestMapping("/blog-comments")
public class BlogCommentsController {

    private final IBlogCommentsService blogCommentsService;
    @Operation(summary = "发布评论")
    @PostMapping("/save")
    public Result saveComment(@RequestBody BlogComments blogComments){
        return blogCommentsService.saveComment(blogComments);
    }

    @Operation(summary = "查询评论列表")
    @GetMapping("/list")
    public Result queryCommentList(@RequestParam("blogId") Long blogId,
                                   @RequestParam(value = "minTime") Long minTime,
                                   @RequestParam(value = "offset",defaultValue = "0") Long offset){
        return blogCommentsService.queryCommentList(blogId,minTime,offset);
    }

    @Operation(summary = "查询子评论列表")
    @GetMapping("/reply-list")
    public Result queryReplyList(@RequestParam("commentId") Long commentId){
        return blogCommentsService.queryReplyList(commentId);
    }
}
