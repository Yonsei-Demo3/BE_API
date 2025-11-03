package com.backend.security.auth.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.annotation.PostConstruct;

@Controller
public class KakaoLoginPageController {
    
    @PostConstruct
    public void checkKeys() {
        System.out.println("[KAKAO] clientId=" + clientId);
        System.out.println("[KAKAO] redirectUri=" + redirectUri);
    }

    @Value("${app.oauth.kakao.client-id:dummy}")
    private String clientId;

    @Value("${app.oauth.kakao.redirect-uri:http://localhost:8080/api/v1/auth/oauth/kakao/callback}")
    private String redirectUri;

    @GetMapping("/page")
    public String loginPage(Model model) {
        String location = "https://kauth.kakao.com/oauth/authorize"
                + "?response_type=code"
                + "&client_id=" + clientId
                + "&redirect_uri=" + redirectUri;
        model.addAttribute("location", location);
        return "login"; // templates/login.html
    }
}
