package pl.epoint.hackyeah.config

import io.github.jhipster.config.JHipsterProperties
import io.github.jhipster.security.AjaxAuthenticationFailureHandler
import io.github.jhipster.security.AjaxAuthenticationSuccessHandler
import io.github.jhipster.security.AjaxLogoutSuccessHandler
import org.springframework.beans.factory.BeanInitializationException
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.http.HttpMethod
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.builders.WebSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.authentication.RememberMeServices
import org.springframework.security.web.csrf.CsrfFilter
import org.springframework.web.filter.CorsFilter
import org.zalando.problem.spring.web.advice.security.SecurityProblemSupport
import pl.epoint.hackyeah.security.AuthoritiesConstants
import javax.annotation.PostConstruct

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
@Import(SecurityProblemSupport::class)
class SecurityConfiguration(private val authenticationManagerBuilder: AuthenticationManagerBuilder, private val userDetailsService: UserDetailsService,
                            private val jHipsterProperties: JHipsterProperties, private val rememberMeServices: RememberMeServices, private val corsFilter: CorsFilter, private val problemSupport: SecurityProblemSupport) : WebSecurityConfigurerAdapter() {

    @PostConstruct
    fun init() {
        try {
            authenticationManagerBuilder
                    .userDetailsService(userDetailsService)
                    .passwordEncoder(passwordEncoder())
        } catch (e: Exception) {
            throw BeanInitializationException("Security configuration failed", e)
        }

    }

    @Bean
    @Throws(Exception::class)
    override fun authenticationManagerBean(): AuthenticationManager {
        return super.authenticationManagerBean()
    }

    @Bean
    fun ajaxAuthenticationSuccessHandler(): AjaxAuthenticationSuccessHandler {
        return AjaxAuthenticationSuccessHandler()
    }

    @Bean
    fun ajaxAuthenticationFailureHandler(): AjaxAuthenticationFailureHandler {
        return AjaxAuthenticationFailureHandler()
    }

    @Bean
    fun ajaxLogoutSuccessHandler(): AjaxLogoutSuccessHandler {
        return AjaxLogoutSuccessHandler()
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }

    @Throws(Exception::class)
    override fun configure(web: WebSecurity?) {
        web!!.ignoring()
                .antMatchers(HttpMethod.OPTIONS, "/**")
                .antMatchers("/app/**/*.{js,html}")
                .antMatchers("/i18n/**")
                .antMatchers("/content/**")
                .antMatchers("/swagger-ui/index.html")
                .antMatchers("/test/**")
    }

    @Throws(Exception::class)
    public override fun configure(http: HttpSecurity) {

        http
                .csrf().disable()
//                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
//                .and()
                .addFilterBefore(corsFilter, CsrfFilter::class.java)
                .exceptionHandling()
                .authenticationEntryPoint(problemSupport)
                .accessDeniedHandler(problemSupport)
                .and()
                .rememberMe()
                .rememberMeServices(rememberMeServices)
                .rememberMeParameter("remember-me")
                .key(jHipsterProperties.security.rememberMe.key)
                .and()
                .formLogin()
                .loginProcessingUrl("/api/authentication")
                .successHandler(ajaxAuthenticationSuccessHandler())
                .failureHandler(ajaxAuthenticationFailureHandler())
                .usernameParameter("j_username")
                .passwordParameter("j_password")
                .permitAll()
                .and()
                .logout()
                .logoutUrl("/api/logout")
                .logoutSuccessHandler(ajaxLogoutSuccessHandler())
                .permitAll()
                .and()
                .headers()
                .frameOptions()
                .disable()
                .and()
                .authorizeRequests()
                .antMatchers("/api/ws/**").permitAll()
                .antMatchers("/api/matches/**").permitAll()
                .antMatchers("/api/register").permitAll()
                .antMatchers("/api/login-taken").permitAll()
                .antMatchers("/api/email-taken").permitAll()
                .antMatchers("/api/activate").permitAll()
                .antMatchers("/api/tables").permitAll()
                .antMatchers("/api/tables/**").permitAll()
                .antMatchers("/api/authenticate").permitAll()
                .antMatchers("/api/account/reset-password/init").permitAll()
                .antMatchers("/api/account/reset-password/finish").permitAll()
                .antMatchers("/api/**").authenticated()
                .antMatchers("/management/health").permitAll()
                .antMatchers("/management/info").permitAll()
                .antMatchers("/management/**").hasAuthority(AuthoritiesConstants.ADMIN)

    }
}
