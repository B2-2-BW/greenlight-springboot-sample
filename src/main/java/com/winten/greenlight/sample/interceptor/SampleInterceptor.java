package com.winten.greenlight.sample.interceptor;

import com.winten.greenlight.sample.greenlight.GreenlightService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class SampleInterceptor implements HandlerInterceptor {

    private final GreenlightService greenlightService = new GreenlightService();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 대기열 검증 로직 호출
        boolean checkResult = checkGreenlight(request, response);
        if (!checkResult) {
            return false;
        }


        return HandlerInterceptor.super.preHandle(request, response, handler);
    }

    private boolean checkGreenlight(HttpServletRequest request, HttpServletResponse response) throws Exception {
        boolean result = greenlightService.checkEntry(request, response);

        if (!result) { // result = false라면 대기화면으로 리다이렉트
            return false;
        }
        return true;
    }

}