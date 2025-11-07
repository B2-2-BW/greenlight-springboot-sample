package com.winten.greenlight.sample.greenlight;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class GreenlightService {
    private static final String TOKEN_COOKIE_NAME = "X-GREENLIGHT-TOKEN";
    private static final String GREENLIGHT_BASE = "https://api.greenlight.hyundai-ite.com";
//    private static final String GREENLIGHT_BASE = "http://localhost:18080";

    private final RestTemplate restTemplate = new RestTemplate();

    public boolean checkEntry(HttpServletRequest request, HttpServletResponse response) throws Exception {
        // 영향도 최소화를 위한 대기열 부분 적용처리
        // 인터셉터에서 pattern으로 제외하는 방법이 불가능할 경우 수동으로 적용하는 방법도 가능
        if (!isGreenlightRequired(request)) {
            return true;
        }

        String tokenFromCookie = null;
        var paramMap = request.getParameterMap();
        if (paramMap.get("g") != null && (paramMap.get("g").length > 0)) {
            tokenFromCookie = paramMap.get("g")[0];
        }
        if (tokenFromCookie == null) {
            tokenFromCookie = readCookie(request, TOKEN_COOKIE_NAME);
        }

        // 대기열 대기요청 API 헤더 세팅
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        if (tokenFromCookie != null && !tokenFromCookie.isBlank()) {
            headers.add(TOKEN_COOKIE_NAME, tokenFromCookie); // X-GREENLIGHT-TOKEN 헤더
        }

        // 현재 URL 전달
        Map<String, String> body = new HashMap<>();
        HttpEntity<Map<String, String>> httpEntity = new HttpEntity<>(body, headers);

        ResponseEntity<QueueCheckResponse> res;
        try {
            // 대기열 대기요청 API 호출
             res = restTemplate.postForEntity(
                    GREENLIGHT_BASE + "/api/v1/customer/verify",
                    httpEntity,
                    QueueCheckResponse.class
            );
        } catch (HttpClientErrorException.NotFound e) {
            return true; // 대기 대상이 아닌 경우
        }

        if (!res.getStatusCode().is2xxSuccessful() || res.getBody() == null) {
            return true; // API 실패 시 기존 로직 유지
        }

        QueueCheckResponse result = res.getBody();
        if (!result.getVerified()) {
            String landingId = "0NFXE62QE2XCA";
            String currentUrl = buildCurrentUrl(request);
            // destinationUrl이 있으면 우선 사용, 없으면 현재 URL로 복귀하도록 redirect 파라미터로 전달(구현 환경에 맞게 조정)
            String redirectTo = "https://greenlight.hyundai-ite.com/l/" + landingId + "?redirectUrl=" + URLEncoder.encode(currentUrl, StandardCharsets.UTF_8); // WAITING인 상태라면 대기화면으로 이동
            response.sendRedirect(redirectTo);
            return false; // 더 이상 체인 진행 안 함
        }

        return true;
    }

    private boolean isGreenlightRequired(HttpServletRequest request) {
        return "/itemPtc".equals(request.getRequestURI());
    }

    // 현재 URL 추출. 유틸성 함수
    private String buildCurrentUrl(HttpServletRequest request) {
        StringBuffer url = request.getRequestURL();
        String qs = request.getQueryString();
        return (qs != null) ? url.append('?').append(qs).toString() : url.toString();
    }

    // 쿠키 조회 추출. 유틸성 함수
    private String readCookie(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return null;
        for (Cookie c : cookies) {
            if (name.equals(c.getName())) {
                return c.getValue();
            }
        }
        return null;
    }

    // 쿠키 저장. 유틸성 함수
    private void addCookie(HttpServletResponse response, String name, String value) {
        Cookie cookie = new Cookie(name, value);
        cookie.setPath("/");
        cookie.setMaxAge(24 * 60 * 60); // 1일
        // 권장 보안 속성
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        response.addCookie(cookie);
    }
}