package lt.galdebar.monmonapi.app.context.security;

import lt.galdebar.monmonapi.app.context.security.jwt.JwtConfigurer;
import lt.galdebar.monmonapi.app.context.security.jwt.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;

@Configuration
@EnableConfigurationProperties
public class AuthServerConfig extends WebSecurityConfigurerAdapter {
    @Autowired
    private ShoppingListDetailsService listDetailsService;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(listDetailsService);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS);

        http
                .authorizeRequests()
                .antMatchers("/lists/create",
                        "/lists/login").permitAll()
                .antMatchers(
                        "/items/**",
                        "/categories/**",
                        "/lists/delete",
                        "/lists/changepassword"
                ).hasAuthority("user");

        http
                .apply(new JwtConfigurer(tokenProvider));

        http.cors();

        http
                .httpBasic().disable()
                .csrf().disable();

    }


    @Bean
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }
}
