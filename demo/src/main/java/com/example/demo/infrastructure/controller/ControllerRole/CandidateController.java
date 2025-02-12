package com.example.demo.infrastructure.controller.ControllerRole;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/candidato")
public class CandidateController {

    @GetMapping("/info")
    public String candidateInfo() {
        return "Información del candidato";
    }
}
