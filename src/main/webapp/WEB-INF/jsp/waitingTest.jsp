<%--
  Created by IntelliJ IDEA.
  User: Daniel Choi
  Date: 2025-08-06 수
  Time: 오전 9:59
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>대기화면이지롱</title>
    <script>
        function getCookie(cookieName) {
            const name = cookieName + "=";
            const decodedCookies = decodeURIComponent(document.cookie);
            const cookies = decodedCookies.split(';');
            for (let i = 0; i < cookies.length; i++) {
                let cookie = cookies[i].trim();
                if (cookie.indexOf(name) === 0) {
                    return cookie.substring(name.length, cookie.length);
                }
            }
            return null; // TOKEN 쿠키가 없으면 null 반환
        }

        function handleClick() {
            window.location.href = '/itemPtc?slitmCd=40A000001'
        }
        window.addEventListener('beforeunload', function () {
            // let token = getCookie('X-GREENLIGHT-TOKEN');
            // const data = JSON.stringify({g: token});
            // const blob = new Blob([data], { type: 'application/json' });
            navigator.sendBeacon('${coreApiUrl}/api/v1/customer/leave', {});
        });
    </script>
</head>
<body>
대기화면이지롱

<button onclick="handleClick()">이동하기</button>

</body>
</html>