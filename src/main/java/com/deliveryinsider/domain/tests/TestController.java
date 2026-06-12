package com.deliveryinsider.domain.tests;

import com.deliveryinsider.global.responses.GlobalRes;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class TestController {
    @GetMapping("/test")
    public GlobalRes<String > test(){
        return GlobalRes.<String>builder()
                .code("00")
                .message("서버접속성공")
                .data("백엔드 작동 중").build();
    }
}
