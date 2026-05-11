package com.blog.controller;

import com.blog.common.JwtUtils;
import com.blog.common.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Random;

@RestController
@RequestMapping("/api")
public class CaptchaController {

    private final JwtUtils jwtUtils;
    private final Random random = new Random();

    public CaptchaController(JwtUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }

    @GetMapping("/captcha")
    public Result<Map<String, String>> getCaptcha() {
        int a = random.nextInt(10) + 1;
        int b = random.nextInt(10) + 1;
        int answer = a + b;
        String question = a + " + " + b + " = ?";

        // Store answer in a short-lived token (2 min)
        String captchaKey = jwtUtils.generateCaptchaToken(answer);
        return Result.success(Map.of("captchaKey", captchaKey, "question", question));
    }
}
