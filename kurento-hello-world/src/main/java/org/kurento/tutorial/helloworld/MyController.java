package org.kurento.tutorial.helloworld;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MyController { 
    @GetMapping("/index")
    public String index() {
    	System.out.println("controller-index");
        return "index.html";
    }


    @GetMapping("/vue")
    public String vue() {
    	System.out.println("controller-vue");
        return "vue";
    }
}
