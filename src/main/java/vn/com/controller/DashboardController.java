package vn.com.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    // Đổi từ "/dashboard.html" thành "/dashboard"
    @GetMapping("/dashboard")
    public String showDashboard() {
        return "dashboard"; // Trả về file templates/dashboard.html
    }
}