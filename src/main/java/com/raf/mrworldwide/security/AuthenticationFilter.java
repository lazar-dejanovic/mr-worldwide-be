package com.raf.mrworldwide.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.raf.mrworldwide.exceptions.AuthorizationException;
import com.raf.mrworldwide.exceptions.HttpResponseException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.filter.GenericFilterBean;

import java.io.IOException;

@Slf4j
public class AuthenticationFilter extends GenericFilterBean {

	private static final String HEADER_STRING = "Authorization";

    private final ObjectMapper objectMapper;
	private final RequestMatcher requestMatcher;

    public AuthenticationFilter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;

        // Builder for PathPatternRequestMatcher
        PathPatternRequestMatcher.Builder builder = PathPatternRequestMatcher.withDefaults();
        this.requestMatcher = new OrRequestMatcher(
                // OPTIONS should be ignored
                builder.matcher(HttpMethod.OPTIONS, "/**"),
                builder.matcher("/api/login"),
                builder.matcher("/api/login/system"),
                builder.matcher("/api/users/forgot-password"),
                builder.matcher("/api/users/reset-password"),
                builder.matcher("/api/social-media-connections/linkedin"),
                builder.matcher("/api/competitors/agent/**")
        );
    }

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {
		HttpServletRequest httpRequest = (HttpServletRequest) request;
		if (requestMatcher.matches(httpRequest)) {
			filterChain.doFilter(request, response);
			return;
		}

		Authentication authentication;
		try {
			authentication = getAuthentication((HttpServletRequest) request);
			SecurityContextHolder.getContext().setAuthentication(authentication);
		} catch (HttpResponseException e) {
			HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
			ResponseStatus responseStatus = AnnotationUtils.findAnnotation(e.getClass(), ResponseStatus.class);
			if (responseStatus != null) {
				status = responseStatus.code();
			}
			else if (e.getHttpStatus() != null) {
				status = e.getHttpStatus();
			}

			((HttpServletResponse) response).setStatus(status.value());
			response.getWriter().write(objectMapper.writeValueAsString(e.getMessage()));
			return;
		} catch (Exception e) {
			((HttpServletResponse) response).setStatus(500);
			response.getWriter().write(objectMapper.writeValueAsString(e.getMessage()));
			return;
		}

		filterChain.doFilter(request, response);
	}

	Authentication getAuthentication(HttpServletRequest request) {
		String token = request.getHeader(HEADER_STRING);

		if (token == null) {
            throw new AuthorizationException("Token not provided!");
		} else {
			return null;
		}
	}

}
