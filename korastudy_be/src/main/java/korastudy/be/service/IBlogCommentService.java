package korastudy.be.service;

import korastudy.be.dto.request.blog.PostCommentRequest;
import korastudy.be.dto.response.blog.PostCommentResponse;
import korastudy.be.security.userprinciple.AccountDetailsImpl;

import java.util.List;

public interface IBlogCommentService {

    // Lấy tất cả comment của 1 post
    List<PostCommentResponse> getAllComments(Long postId);

    // Tạo comment mới (cần đăng nhập)
    PostCommentResponse addComment(Long postId, PostCommentRequest request, AccountDetailsImpl currentUser);

    // Update comment (chỉ user tạo comment hoặc admin)
    PostCommentResponse updateComment(Long postId, Long commentId, PostCommentRequest request, AccountDetailsImpl currentUser);

    // Xoá comment (chỉ user tạo comment hoặc admin)
    void deleteComment(Long postId, Long commentId, AccountDetailsImpl currentUser);
}