package robotManageSystem.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import robotManageSystem.interceptor.AuthInterceptor;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Autowired
    private AuthInterceptor authInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
               .addPathPatterns("/api/**")         // 拦截所有api请求
               .excludePathPatterns(               // 排除不需要拦截的路径
                   "/api/auth/login",
                   "/api/auth/logout"
               );

    }
}