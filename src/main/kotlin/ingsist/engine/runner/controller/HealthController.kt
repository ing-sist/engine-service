package ingsist.engine.runner.controller

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/health")
class HealthController {
    @GetMapping("/check")
    fun healthCheck(): ResponseEntity<String> {
        return ResponseEntity.ok("Engine Service is healthy")
    }

    @GetMapping("/error")
    fun errorCheck(): ResponseEntity<String> {
        return ResponseEntity.status(500).body("Engine Service has an error")
    }
}
