package com.winten.greenlight.sample.greenlight;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Slf4j
public class GreenlightService {
    private static final String ID_COOKIE_NAME = "X-GREENLIGHT-ID"; // greenlight ID 쿠기명, 고정값
    private static final String ID_QUERY_KEY = "gUserId"; // greenlight ID 쿼리명, 고정값
    private static final String GREENLIGHT_BASE = "https://api.greenlight.hyundai-ite.com"; // 그린라이트 API URL, 고정값

    private static final Set<String> TARGET_URLS = Set.of("/itemPtc"); // 영향도 최소화를 위한 대기열 대상 URL 제한, POC를 위한 임시 세팅값
    private static final String POC_LANDING_ID = "0NFJNJ5B33YZM"; // 제공된 ID 입력, POC를 위한 임시 세팅값

    private final RestTemplate restTemplate = new RestTemplate();

    public boolean checkEntry(HttpServletRequest request, HttpServletResponse response) throws Exception {
        // 영향도 최소화를 위한 대기열 부분 적용처리
        // 인터셉터에서 pattern으로 제외하는 방법도 있으나, 여기서는 간단하게 수동으로 적용
        if (!isGreenlightRequired(request)) {
            return true;
        }

        // 토큰 추출
        String greenlightId = readQuery(request, ID_QUERY_KEY);
        if (greenlightId == null) {
            greenlightId = readCookie(request, ID_COOKIE_NAME);
        }

        // 토큰이 있다면 검증 시도
        if (greenlightId != null && !greenlightId.isBlank()) {
            // 대기열 대기요청 API 헤더 세팅
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
            headers.add(ID_COOKIE_NAME, greenlightId); // X-GREENLIGHT-ID 헤더

            // 현재 URL 전달
            Map<String, String> body = new HashMap<>();
            HttpEntity<Map<String, String>> httpEntity = new HttpEntity<>(body, headers);

            ResponseEntity<QueueCheckResponse> res = null;
            try {
                // 대기열 검증 요청 API 호출
                res = restTemplate.postForEntity(
                        GREENLIGHT_BASE + "/api/v1/customer/verify",
                        httpEntity,
                        QueueCheckResponse.class
                );
            } catch (Exception e) {
                log.warn(e.getMessage());
            }

            if (res != null && res.getBody() != null) {
                QueueCheckResponse result = res.getBody();
                if (result.getVerified()) {
                    return true; // 여기까지 왔다면 정상적인 토큰을 가지고 검증에 성공해서 진입 가능한 경우
                }
            }
        }

        // 이곳에 도달했다면 대기해야는 경우
        String currentUrl = buildCurrentUrl(request);
        // destinationUrl이 있으면 우선 사용, 없으면 현재 URL로 복귀하도록 redirect 파라미터로 전달(구현 환경에 맞게 조정)
        String redirectTo = "https://greenlight.hyundai-ite.com/l/" + POC_LANDING_ID + "?redirectUrl=" + URLEncoder.encode(currentUrl, StandardCharsets.UTF_8); // WAITING인 상태라면 대기화면으로 이동
        response.sendRedirect(redirectTo);
        return false; // 더 이상 체인 진행 안 함
    }

    private String readQuery(HttpServletRequest request, String queryKey) {
        try {
            return request.getParameterMap().get(queryKey)[0];
        } catch (Exception ignored) {
            return null;
        }
    }

    private boolean isGreenlightRequired(HttpServletRequest request) {
        return TARGET_URLS.contains(request.getRequestURI());
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