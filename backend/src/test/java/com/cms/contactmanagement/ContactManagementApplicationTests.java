package com.cms.contactmanagement;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Disabled("Disabled for unit-test runs: requires a configured/running SQL Server")
class ContactManagementApplicationTests {

    @Test
    void contextLoads() {
    }
}

