package com.ngtukien.f1;

import com.fasterxml.jackson.datatype.hibernate6.Hibernate6Module;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class F1Application {
    public static void main(String[] args) {
        SpringApplication.run(F1Application.class, args);
    }

    /**
     * Danh sách lazy mà DAO chưa nạp sẽ được trả về null thay vì gây LazyInitializationException,
     * nên mỗi phương thức DAO chỉ trả đúng phần "kèm danh sách ..." như báo cáo.
     */
    @Bean
    public Hibernate6Module hibernate6Module() {
        return new Hibernate6Module();
    }
}
