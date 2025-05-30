package com.blog.domain.auth.dto.responses;

public record OAuth2ProviderResponse(
    String access_token,
    String expires_in,
    String scope,
    String token_type
) {

}
