package lt.galdebar.monmonmvc.context.security;

import lt.galdebar.monmonmvc.context.security.jwt.JwtConfigurer;
import lt.galdebar.monmonmvc.context.security.jwt.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.endpoint.NimbusAuthorizationCodeTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.client.web.HttpSessionOAuth2AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;

@Configuration
@EnableConfigurationProperties
public class AuthServerConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private MongoUserDetailsService mongoUserDetailsService;


    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(mongoUserDetailsService);
    }

    @Autowired
    JwtTokenProvider jwtTokenProvider;


    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS);

        http
                .authorizeRequests()
                .antMatchers("/", "/login/**", "/signup").permitAll()
                .antMatchers("/**").hasAuthority("user");

//        http.oauth2Login();

        http
                .oauth2Login()
                .tokenEndpoint()
                .accessTokenResponseClient(accessTokenResponseClient());

        http
                .oauth2Login()
                .redirectionEndpoint()
                .baseUri("/login/oauth2/code/github");

        http
                .oauth2Login()
                .authorizationEndpoint()
                .baseUri("login/oauth2/authorization")
                .authorizationRequestRepository(authorizationRequestRepository());

        http
                .exceptionHandling(
                        e ->
                                e.authenticationEntryPoint(
                                        new HttpStatusEntryPoint(
                                                HttpStatus.UNAUTHORIZED
                                        )
                                )
                );
        http
                .apply(new JwtConfigurer(jwtTokenProvider));


        http
                .cors();

        http
                .httpBasic().disable()
                .csrf().disable();
    }


    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthorizationRequestRepository<OAuth2AuthorizationRequest>
    authorizationRequestRepository() {
        return new HttpSessionOAuth2AuthorizationRequestRepository();
    }

    @Bean
    public OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest>
    accessTokenResponseClient() {

        return new NimbusAuthorizationCodeTokenResponseClient();
    }

    @Bean // is this really necessary?...
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }


}
