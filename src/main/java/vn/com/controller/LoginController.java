package vn.com.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {

    @GetMapping("/")
    public String redirectToLogin() {
        return "redirect:/login"; // Tự động đá về form login
    }

    @GetMapping("/login")
    public String showLoginPage() {
        return "auth/login"; // Trả về file templates/auth/login.html
    }
}