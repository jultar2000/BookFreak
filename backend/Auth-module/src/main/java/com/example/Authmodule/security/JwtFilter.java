package com.example.Authmodule.security;

import com.example.Authmodule.service.JwtService;
import com.example.Authmodule.service.UserDetailService;

import lombok.AllArgsConstructor;
import org.apache.http.HttpException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@Component
@AllArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    private final UserDetailService userDetailService;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        List<RequestMatcher> ignoredPaths = List.of(
                new AntPathRequestMatcher("/api/v1/auth/**"),
                new AntPathRequestMatcher("/v3/api-docs/**"));
        return ignoredPaths
                .stream()
                .anyMatch(requestMatcher ->
                        requestMatcher.matches(request));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String jwt = jwtService.getJwtFromRequest(request);
        String username = jwtService.extractUsernameFromToken(jwt);
        UserDetails userDetails = userDetailService.loadUserByUsername(username);
        if (jwtService.validateToken(jwt, userDetails)) {
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails,
                    null, userDetails.getAuthorities());
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } else {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        }
        filterChain.doFilter(request, response);
    }
}
