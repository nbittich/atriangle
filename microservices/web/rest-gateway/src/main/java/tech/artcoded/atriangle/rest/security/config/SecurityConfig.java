package tech.artcoded.atriangle.rest.security.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import tech.artcoded.atriangle.rest.user.UserRepository;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;

import static tech.artcoded.atriangle.rest.user.Role.ADMIN;
import static tech.artcoded.atriangle.rest.user.Role.USER;

@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http.cors()
        .and()
        .authorizeRequests()
        // mapping
        .antMatchers(HttpMethod.DELETE, "/**")
        .hasAnyAuthority(ADMIN.getAuthority())

        .antMatchers(HttpMethod.POST, "/api/**")
        .hasAuthority(ADMIN.getAuthority())
        .antMatchers(HttpMethod.PUT, "/api/**")
        .hasAuthority(ADMIN.getAuthority())

        .antMatchers(HttpMethod.GET, "/user/**")
        .authenticated()
        .antMatchers(HttpMethod.POST, "/user/**")
        .authenticated()

        .antMatchers(HttpMethod.POST, "/proxy/**/sparql")
        .permitAll()
        .antMatchers(HttpMethod.GET, "/proxy/**")
        .permitAll() // read only
        .antMatchers(HttpMethod.POST, "/proxy/**")
        .hasAuthority(ADMIN.getAuthority())
        .antMatchers(HttpMethod.PUT, "/proxy/**")
        .hasAuthority(ADMIN.getAuthority())

        .antMatchers(HttpMethod.GET, "/api/**/swagger-ui.html")
        .permitAll()
        .antMatchers(HttpMethod.GET, "/api/**/webjars/**")
        .permitAll()
        .antMatchers(HttpMethod.GET, "/api/**/swagger-resources/**")
        .permitAll()
        .antMatchers(HttpMethod.GET, "/api/**/v2/api-docs/**")
        .permitAll()
        .antMatchers(HttpMethod.GET, "/api/**/csrf/**")
        .permitAll()
        .antMatchers(HttpMethod.GET, "/index.html")
        .permitAll()

        .antMatchers(HttpMethod.GET, "/api/**")
        .hasAnyAuthority(ADMIN.getAuthority(), USER.getAuthority())


        .antMatchers(HttpMethod.OPTIONS, "/**")
        .permitAll()

        .anyRequest()
        .permitAll()

        // csrf
        .and()
        .csrf()
        .disable()

        // http basic
        .httpBasic()
        .realmName("ATriangleGateway")

        // exception handling
        .and()
        .exceptionHandling()
        .authenticationEntryPoint((req, resp, e) -> resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED))

        .and()
        .formLogin()
        .disable()

        // jwt filter
        .addFilter(new JwtAuthenticationFilter(authenticationManager(), env))
        .addFilter(new JwtAuthorizationFilter(authenticationManager(), env))

        // stateless
        .sessionManagement()
        .sessionCreationPolicy(SessionCreationPolicy.STATELESS);
  }

  @Bean
  public BCryptPasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Inject
  public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
    auth.userDetailsService(userRepository)
        .passwordEncoder(passwordEncoder());
  }

  @Inject
  UserRepository userRepository;

  @Inject
  Environment env;


}
