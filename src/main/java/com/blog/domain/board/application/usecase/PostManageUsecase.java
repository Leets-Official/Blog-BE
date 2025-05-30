package com.blog.domain.board.application.usecase;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.blog.domain.board.application.dto.PostCreateRequest;
import com.blog.domain.board.application.dto.PostReadAllResponse;
import com.blog.domain.board.application.dto.PostReadResponse;
import com.blog.domain.board.application.dto.PostUpdateRequest;
import com.blog.domain.board.domain.entity.Content;
import com.blog.domain.board.domain.entity.Post;
import com.blog.domain.board.domain.service.ContentDeleteService;
import com.blog.domain.board.domain.service.ContentGetService;
import com.blog.domain.board.domain.service.ContentSaveService;
import com.blog.domain.board.domain.service.PostDeleteService;
import com.blog.domain.board.domain.service.PostGetService;
import com.blog.domain.board.domain.service.PostSaveService;
import com.blog.domain.board.domain.service.PostUpdateService;
import com.blog.domain.board.domain.service.PostValidateService;
import com.blog.domain.comment.domain.entity.Comment;
import com.blog.domain.comment.domain.service.CommentDeleteService;
import com.blog.domain.comment.domain.service.CommentGetService;
import com.blog.domain.user.domain.entity.User;
import com.blog.domain.user.domain.service.UserGetService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PostManageUsecase {
	private final UserGetService userGetService;
	private final PostSaveService postSaveService;
	private final PostGetService postGetService;
	private final PostUpdateService postUpdateService;
	private final PostValidateService postValidateService;
	private final PostDeleteService postDeleteService;
	private final CommentGetService commentGetService;
	private final CommentDeleteService commentDeleteService;
	private final ContentSaveService contentCreateService;
	private final ContentGetService contentGetService;
	private final ContentDeleteService contentDeleteService;

	@Transactional
	public void createPost(Long userId, PostCreateRequest dto) {
		User user = userGetService.find(userId);
		Post post = Post.CreatePost(dto.title(), user);

		contentCreateService.create(dto.contents(), post);
		postSaveService.save(post);
	}

	@Transactional(readOnly = true)
	public PostReadResponse readPost(Long userId, UUID postId) {
		User user = userGetService.find(userId);
		Post post = postGetService.find(postId);

		List<Comment> comments = commentGetService.findALlByPost(post);
		List<Content> contents = contentGetService.findAll(post);

		return PostReadResponse.toResponse(post, user, contents, comments);
	}

	@Transactional(readOnly = true)
	public PostReadResponse readPostNoToken(UUID postId) {
		Post post = postGetService.find(postId);
		List<Comment> comments = commentGetService.findALlByPost(post);
		List<Content> contents = contentGetService.findAll(post);

		return PostReadResponse.toResponse(post, null, contents, comments);
	}

	@Transactional(readOnly = true)
	public PostReadAllResponse readAllPost(Long userId, int size, int page) {
		User user = userGetService.find(userId);
		Page<Post> postPage = postGetService.findAll(size, page);
		List<Post> posts = postPage.getContent();

		List<PostReadResponse> dtos = posts.stream()
			.map(post -> {
				List<Content> contents = contentGetService.findAll(post);
				List<Comment> comments = commentGetService.findALlByPost(post);
				return PostReadResponse.toResponse(post, user, contents, comments);
			}).toList();

		return PostReadAllResponse.toResponse(dtos, postPage.getTotalPages());
	}

	@Transactional(readOnly = true)
	public PostReadAllResponse readAllPostNoToken(int size, int page) {
		Page<Post> postPage = postGetService.findAll(size, page);
		List<Post> posts = postPage.getContent();

		List<PostReadResponse> dtos = posts.stream()
			.map(post -> {
				List<Content> contents = contentGetService.findAll(post);
				List<Comment> comments = commentGetService.findALlByPost(post);
				return PostReadResponse.toResponse(post, null, contents, comments);
			}).toList();

		return PostReadAllResponse.toResponse(dtos, postPage.getTotalPages());
	}

	@Transactional
	public void updatePost(Long userId, UUID postId, PostUpdateRequest dto) {
		User user = userGetService.find(userId);
		Post post = postGetService.find(postId);

		postValidateService.certificate(post, user);

		contentDeleteService.deleteAllByPost(post);
		contentCreateService.create(dto.contents(), post);

		postUpdateService.update(post, dto);
	}

	@Transactional
	public void deletePost(Long userId, UUID postId) {
		User user = userGetService.find(userId);
		Post post = postGetService.find(postId);

		postValidateService.certificate(post, user);

		contentDeleteService.deleteAllByPost(post);
		commentDeleteService.deleteAll(post);
		postDeleteService.delete(post);
	}
}
