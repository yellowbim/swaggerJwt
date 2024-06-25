package org.jjuni.swaggerjwt.test;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/test")
public class TestController {

    @GetMapping("test")
    public String test() {
        return "요청 되는지 확인하는 테스트 url";
    }
}
