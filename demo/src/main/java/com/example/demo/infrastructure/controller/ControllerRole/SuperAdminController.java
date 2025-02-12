package com.example.demo.infrastructure.controller.ControllerRole;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/super-admin")
public class SuperAdminController {

    @GetMapping("/dashboard")
    public String superAdminDashboard() {
        return "Bienvenido al panel de Super Admin";
    }
}
