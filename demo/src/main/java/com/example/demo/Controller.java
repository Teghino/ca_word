package com.example.demo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class Controller {
    @Autowired
    private Environment environment;
    @GetMapping("/")
    public String home(){
        return "home";
    }
    @Scheduled(fixedRate = 5000) // Esegue il metodo ogni 5000 millisecondi (5 secondi)
    public void myScheduledMethod() {
        // Aggiungi qui la logica che vuoi eseguire ad intervalli regolari
        System.out.println(environment.getProperty("local.server.port"));
    }
}
