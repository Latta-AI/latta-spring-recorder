package ai.latta.examples.springdemo.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ExampleMvcController {

    @GetMapping("/mvc")
    public String index(Model model) {
        model.addAttribute("message", "Hello from Spring MVC Controller!");
        return "index";
    }
}
