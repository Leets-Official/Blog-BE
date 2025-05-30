package com.blog.domain.auth.service;

import com.blog.domain.auth.dto.requests.LoginPostRequest;
import com.blog.domain.auth.dto.requests.OAuthRegisterRequest;
import com.blog.domain.auth.dto.responses.OAuthLoginResponse;
import com.blog.domain.auth.dto.responses.OAuthRegisterRequiredResponse;
import com.blog.domain.auth.dto.responses.RegisterPostResponse;
import com.blog.domain.user.domain.entity.User;
import com.blog.domain.user.domain.service.UserService;
import com.blog.domain.user.exception.EmailDuplicateException;
import com.blog.domain.user.exception.NicknameDuplicateException;
import com.blog.global.common.oauth.MemberInfoFromProviders;
import com.blog.global.config.properties.AppConfigProperties;
import jakarta.transaction.Transactional;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class OAuth2Service {
  // service
  private final AuthService authService;
  private final UserService userService;

  // utils
  private final AppConfigProperties appConfigProperties;

  @Transactional
  public OAuthLoginResponse oauth2Login(MemberInfoFromProviders memberInfoFromProviders) {
    Optional<User> getLoginAvailableResponse =
        this.userService.checkLoginAvailableByKakaoId(memberInfoFromProviders.id());

    if (getLoginAvailableResponse.isEmpty()) {
      return OAuthRegisterRequiredResponse.from(memberInfoFromProviders);
    }

    return this.authService.login(memberInfoFromProviders);
  }

  @Transactional
  public RegisterPostResponse oauth2Register(OAuthRegisterRequest oAuthRegisterRequest) {
    boolean isEmailDuplicate = this.userService.checkEmailDuplicate(oAuthRegisterRequest.email());
    if (isEmailDuplicate) {
      throw new EmailDuplicateException();
    }

    boolean isNicknameDuplicate = this.userService.checkNicknameDuplicate(oAuthRegisterRequest.nickname());
    if (isNicknameDuplicate) {
      throw new NicknameDuplicateException();
    }

    User user = User.create(oAuthRegisterRequest,
        this.userService.hashPassword(this.appConfigProperties.getOauthDummyPassword())
    );

    return RegisterPostResponse.of(this.userService.save(user));
  }
}
