package com.example.demo;

import com.example.demo.service.HelloService;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class HelloServiceTest {

    @Test
    void testGreet() {
        HelloService service = new HelloService();
        String result = service.greet();
        assertEquals("Hello CI/CD", result);
    }
}
