package com.hyp.learn.dsm.uaa.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.client.JdbcClientDetailsService;
import org.springframework.security.oauth2.provider.code.AuthorizationCodeServices;
import org.springframework.security.oauth2.provider.code.JdbcAuthorizationCodeServices;
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.TokenEnhancerChain;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;

import javax.sql.DataSource;
import java.util.Arrays;

/**
 * token+h2数据库
 * @author hyp
 * Project name is learn-security
 * Include in com.hyp.learn.ds.uaa.config
 * hyp create at 19-12-20
 **/
@Configuration
@EnableAuthorizationServer
public class AuthorizationServerByH2 extends
        AuthorizationServerConfigurerAdapter {

    @Autowired
    private TokenStore tokenStore;
    @Autowired
    private JwtAccessTokenConverter accessTokenConverter;
    @Autowired
    private ClientDetailsService clientDetailsService;
    @Autowired
    private AuthorizationCodeServices authorizationCodeServices;
    @Autowired
    private AuthenticationManager authenticationManager;

    /**
     * 1.客户端详情相关配置
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public ClientDetailsService clientDetailsService(DataSource dataSource) {
        ClientDetailsService clientDetailsService = new JdbcClientDetailsService(dataSource);
        ((JdbcClientDetailsService)
                clientDetailsService).setPasswordEncoder(passwordEncoder());
        return clientDetailsService;
    }

    @Override
    public void configure(ClientDetailsServiceConfigurer clients)
            throws Exception {
        clients.withClientDetails(clientDetailsService);
    }

    /**
     * 2.配置令牌服务(token services)
     */
    @Bean
    public AuthorizationServerTokenServices tokenService() {
        DefaultTokenServices service = new DefaultTokenServices();
        service.setClientDetailsService(clientDetailsService);
        service.setSupportRefreshToken(true);//支持刷新令牌
        service.setTokenStore(tokenStore); //绑定tokenStore

        TokenEnhancerChain tokenEnhancerChain = new TokenEnhancerChain();
        tokenEnhancerChain.setTokenEnhancers(Arrays.asList(accessTokenConverter));

        service.setTokenEnhancer(tokenEnhancerChain);
        service.setAccessTokenValiditySeconds(7200); // 令牌默认有效期2小时
        service.setRefreshTokenValiditySeconds(259200); // 刷新令牌默认有效期3天
        return service;
    }

    /**
     * 3.配置令牌(token)的访问端点
     */
    @Bean
    public AuthorizationCodeServices authorizationCodeServices(DataSource dataSource) {
        return new JdbcAuthorizationCodeServices(dataSource);
        //设置授权码模式的授权码如何存取
    }

    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) {
        endpoints.authenticationManager(authenticationManager)
                .authorizationCodeServices(authorizationCodeServices)
                .tokenServices(tokenService())
                .allowedTokenEndpointRequestMethods(HttpMethod.POST);
    }

    /**
     * 4.配置令牌端点(Token Endpoint)的安全约束
     */
    @Override
    public void configure(AuthorizationServerSecurityConfigurer security) {
        security
                .tokenKeyAccess("permitAll()")
                .checkTokenAccess("permitAll()")
                .allowFormAuthenticationForClients()//允许表单认证
        ;
    }
}
