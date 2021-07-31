package org.kurento.tutorial.helloworld;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MyController { 
//    @GetMapping({"/", "/error"})
//    public String index() {
//        return "index";
//    }


    @GetMapping("/vue")
    public String join() {
        return "vue";
    }
}
