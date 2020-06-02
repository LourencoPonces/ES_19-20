package pt.ulisboa.tecnico.socialsoftware.tutor.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

@RestController
public class AuthController {
    @Autowired
    private AuthService authService;

    @Value("${base.url}")
    private String baseUrl;

    @Value("${oauth.consumer.key}")
    private String oauthConsumerKey;

    @Value("${oauth.consumer.secret}")
    private String oauthConsumerSecret;

    @Value("${callback.url}")
    private String callbackUrl;

    @GetMapping("/auth/fenix")
    public AuthDto fenixAuth(@RequestParam String code, HttpServletResponse response) {
        FenixEduInterface fenix = new FenixEduInterface(baseUrl, oauthConsumerKey, oauthConsumerSecret, callbackUrl);
        fenix.authenticate(code);
        AuthDto dto = this.authService.fenixAuth(fenix);
        setAuthCookie(dto, response);
        return dto;
    }

    @GetMapping("/auth/demo/student")
    public AuthDto demoStudentAuth(HttpServletResponse response) {
       AuthDto dto = this.authService.demoStudentAuth();
       setAuthCookie(dto, response);
       return dto;
    }

    @GetMapping("/auth/demo/teacher")
    public AuthDto demoTeacherAuth(HttpServletResponse response) {
        AuthDto dto = this.authService.demoTeacherAuth();
        setAuthCookie(dto, response);
        return dto;
    }

    @GetMapping("/auth/demo/admin")
    public AuthDto demoAdminAuth(HttpServletResponse response) {
        AuthDto dto = this.authService.demoAdminAuth();
        setAuthCookie(dto, response);
        return dto;
    }

    private void setAuthCookie(AuthDto dto, HttpServletResponse response) {
        Cookie authCookie = new Cookie(JwtTokenProvider.COOKIE_NAME, dto.getToken());
        authCookie.setHttpOnly(true);
        authCookie.setSecure(true);
        authCookie.setMaxAge(JwtTokenProvider.EXPIRATION_TIME + 1);

        response.addCookie(authCookie);
    }
}