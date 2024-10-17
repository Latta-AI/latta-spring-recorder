package ai.latta.examples.springdemo.controllers;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ExampleRestController {

    @GetMapping("/")
    String helloWorld() {
        return "Hello World!";
    }


    @GetMapping(value = "/exception")
    String exception(HttpServletResponse response) throws Exception {
        // set header
        response.setHeader("Content-Type", "text/plain");

        throw new Exception("Hello broken world");
    }
}
