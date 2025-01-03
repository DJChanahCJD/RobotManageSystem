package robotManageSystem.interceptor;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.fasterxml.jackson.databind.ObjectMapper;

import robotManageSystem.dto.BaseResponse;
import robotManageSystem.utils.JwtUtil;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        // 放行登录相关接口
        if (request.getRequestURI().contains("/api/auth/")) {
            return true;
        }

        // 获取token
        String token = request.getHeader("Access-Token");
        if (token == null || token.isEmpty()) {
            handleUnauthorized(response, "请先登录");
            return false;
        }

        return true;
    }

    private void handleUnauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(401);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(
            objectMapper.writeValueAsString(
                BaseResponse.error(message)
            )
        );
    }
}