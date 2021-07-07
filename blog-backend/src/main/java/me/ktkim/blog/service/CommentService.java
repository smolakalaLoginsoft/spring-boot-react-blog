package me.ktkim.blog.service;

import me.ktkim.blog.common.Exception.BadRequestException;
import me.ktkim.blog.model.domain.Comment;
import me.ktkim.blog.model.domain.Post;
import me.ktkim.blog.model.domain.User;
import me.ktkim.blog.model.dto.CommentDto;
import me.ktkim.blog.repository.CommentRepository;
import me.ktkim.blog.repository.PostRepository;
import me.ktkim.blog.security.service.CustomUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

/**
 * @author Kim Keumtae
 */
@Service
@Transactional(noRollbackFor = {NullPointerException.class})
public class CommentService {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private PostRepository postRepository;

    @PersistenceContext
    protected EntityManager entityManager;

    public Optional<Comment> findForId(Long id) {
        return commentRepository.findById(id);
    }

    public Optional<Post> findPostForId(Long id) {
        return postRepository.findById(id);
    }

    public Optional<List<Comment>> findCommentsByPostId(Long id) {
        return commentRepository.findByPostId(id);
    }

    public CommentDto registerComment(CommentDto commentDto, CustomUserDetails customUserDetails) {
        Optional<Post> postForId = this.findPostForId(commentDto.getPostId());
        if (postForId.isPresent()) {
            Comment newComment = new Comment();
            newComment.setBody(commentDto.getBody());
            newComment.setPost(postForId.get());
            newComment.setUser(new User(customUserDetails.getId(), customUserDetails.getName()));
            registerComment(newComment);
            return new CommentDto(newComment);
        } else {
            throw new BadRequestException("Not exist post.");
        }
    }

    public void registerComment(Comment comment) {
        String sql2 = "INSERT INTO COMMENT(COMMENT_ID, BODY, CREATED_DATE, LAST_MODIFIED_DATE, POST_ID, USER_ID) VALUES(null, '" + comment.getBody() + "', '" + comment.getCreatedDate() + "', '" + comment.getLastModifiedDate() + "', " + comment.getPost().getId() + ", " + comment.getUser().getId() + ");";
        Query query = entityManager.createNativeQuery(sql2);
        query.executeUpdate();
    }

    public Optional<CommentDto> editPost(CommentDto editCommentDto) {
        return this.findForId(editCommentDto.getId())
                .map(comment -> {
                    comment.setBody(editCommentDto.getBody());
                    return comment;
                })
                .map(CommentDto::new);
    }

    public void deletePost(Long id) {
        commentRepository.findById(id).ifPresent(comment -> {
            commentRepository.delete(comment);
        });
    }
}
