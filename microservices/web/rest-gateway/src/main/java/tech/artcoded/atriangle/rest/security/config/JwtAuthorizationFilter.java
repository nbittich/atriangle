package tech.artcoded.atriangle.rest.security.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import tech.artcoded.atriangle.rest.user.Role;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class JwtAuthorizationFilter extends BasicAuthenticationFilter {


  private final Environment env;

  public JwtAuthorizationFilter(AuthenticationManager authenticationManager, Environment env) {
    super(authenticationManager);
    this.env = env;
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                  FilterChain filterChain) throws IOException, ServletException {
    Authentication authentication = getAuthentication(request);
    if (authentication != null) {
      SecurityContextHolder.getContext()
                           .setAuthentication(authentication);
    }

    filterChain.doFilter(request, response);
  }

  private UsernamePasswordAuthenticationToken getAuthentication(HttpServletRequest request) {
    String token = request.getHeader("X-Auth-Token");
    if (StringUtils.isNotEmpty(token) && token.startsWith(env.getRequiredProperty("jwt.prefix"))) {
      byte[] signingKey = env.getRequiredProperty("jwt.secret")
                             .getBytes();
      try {

        Jws<Claims> parsedToken = Jwts.parserBuilder()
                                      .requireAudience("string")
                                      .setSigningKey(signingKey)
                                      .build()
                                      .parseClaimsJws(token.replace("Bearer ", ""));

        String username = parsedToken
          .getBody()
          .getSubject();

        List<GrantedAuthority> authorities = ((List<?>) parsedToken.getBody()
                                                                   .get("rol")).stream()
                                                                               .map(String::valueOf)
                                                                               .map(Role::valueOf)
                                                                               .collect(Collectors.toList());

        if (StringUtils.isNotEmpty(username)) {
          return new UsernamePasswordAuthenticationToken(username, null, authorities);
        }
      }
      catch (Exception e) {
        logger.error("error occured while authorization phase ", e);
      }
    }

    return null;
  }
}
