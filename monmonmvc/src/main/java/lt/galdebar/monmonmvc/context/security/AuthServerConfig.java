package lt.galdebar.monmonmvc.context.security;

import lt.galdebar.monmonmvc.context.security.jwt.JwtConfigurer;
import lt.galdebar.monmonmvc.context.security.jwt.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

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
                .antMatchers("/",
                        "/user/login",
                        "/user/register/**",
                        "/user/changeemail/confirm/*").permitAll()
                .antMatchers("/user/me",
                        "user/changeemail",
                        "user/changepassword",
                        "user/getlinkedusers",
                        "user/deleteuser",
                        "user/link",
                        "shoppingitems/**",
                        "categorysearch/**"
                        ).hasAuthority("user");

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

    @Bean // is this really necessary?...
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

}
