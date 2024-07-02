package org.jjuni.swaggerjwt.test;

import org.jjuni.swaggerjwt.common.dto.CommResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/test")
public class TestController {

    @GetMapping("test")
    @ResponseBody
    public CommResponse<?> test() {
        return CommResponse.createSuccess("요청 되는지 확인하는 테스트 url");
    }
}
