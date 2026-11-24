package com.hrms;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = "jwt.secret=test-only-secret-key-with-more-than-32-characters")
class HrmsApplicationTests {

    @Test
    void contextLoads() {
    }

}
