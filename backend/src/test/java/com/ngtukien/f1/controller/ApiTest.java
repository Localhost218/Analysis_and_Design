package com.ngtukien.f1.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Kiểm tra định dạng JSON và mã lỗi HTTP của các API chính. */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ApiTest {
    @Autowired
    MockMvc mvc;

    @Test
    void loginFailMessage() throws Exception {
        mvc.perform(post("/api/organizers/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"a\",\"password\":\"x\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Tên đăng nhập hoặc mật khẩu không chính xác"));
    }

    @Test
    void loginDoesNotExposePassword() throws Exception {
        mvc.perform(post("/api/organizers/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"a\",\"password\":\"a@123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("Nguyễn Văn A"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void searchSponsorsWithoutContracts() throws Exception {
        mvc.perform(get("/api/sponsors").param("name", "ar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].name", hasItems("Aramco", "Qatar Airways")))
                .andExpect(jsonPath("$[0].listContract").value(nullValue()));
    }

    @Test
    void raceInfoHasResultsAndPaymentStatus() throws Exception {
        // C17 – Singapore 2026: Verstappen thắng, đã trả; Lawson hạng 7 chưa trả
        mvc.perform(get("/api/races/41"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.season.name").value("F1 FIA 2026"))
                .andExpect(jsonPath("$.listRegisteredReceipt[0].registeredDriver.driver.fullName").value("Max Verstappen"))
                .andExpect(jsonPath("$.listRegisteredReceipt[0].reward").value(100000000))
                .andExpect(jsonPath("$.listRegisteredReceipt[0].paymentStatus").value("Đã trả"))
                .andExpect(jsonPath("$.listRegisteredReceipt[6].paymentStatus").value("Chưa trả"));
    }

    @Test
    void payInvoiceAndErrors() throws Exception {
        // C18 – United States 2026 (chưa trả): 854 Antonelli hạng 1, 855 Leclerc hạng 2, 862 Lawson (không có TK mặc định)
        mvc.perform(get("/api/races/42"));
        mvc.perform(post("/api/invoices").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"organizer\":{\"id\":1},\"listRegisteredReceipt\":[{\"id\":854},{\"id\":862}]}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Thanh toán không thành công: Thiếu thông tin nhận thưởng: Liam Lawson"));
        mvc.perform(post("/api/invoices").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"organizer\":{\"id\":1},\"listRegisteredReceipt\":[{\"id\":854},{\"id\":855}]}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.amount").value(172000000))
                .andExpect(jsonPath("$.organizer.fullName").value("Nguyễn Văn A"))
                .andExpect(jsonPath("$.listRegisteredReceipt", hasSize(2)))
                .andExpect(jsonPath("$.listRegisteredReceipt[0].race.season.year").value(2026));
    }

    @Test
    void contractInvalid() throws Exception {
        mvc.perform(post("/api/contracts").contentType(MediaType.APPLICATION_JSON).content("{\"value\":0}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Thông tin hợp đồng không hợp lệ"));
    }

    @Test
    void universalLoginAllRoles() throws Exception {
        // 1. Organizer
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"a\",\"password\":\"a@123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("ORGANIZER"))
                .andExpect(jsonPath("$.user.fullName").value("Nguyễn Văn A"));

        // 2. Sponsor login bằng username
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"b\",\"password\":\"b@123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("SPONSOR"))
                .andExpect(jsonPath("$.user.name").value("Heineken"));

        // 3. Sponsor login bằng mã số thuế
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"0123456789\",\"password\":\"b@123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("SPONSOR"))
                .andExpect(jsonPath("$.user.name").value("Heineken"));

        // 4. Driver
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"c\",\"password\":\"c@123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("DRIVER"))
                .andExpect(jsonPath("$.user.fullName").value("Lando Norris"));

        // 5. Customer
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"d\",\"password\":\"d@123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("CUSTOMER"));
    }

    @Test
    void payDirectRewardSuccessWithoutBankAccount() throws Exception {
        mvc.perform(get("/api/races/42"));
        // Receipt 862 là Liam Lawson (không có tài khoản ngân hàng mặc định)
        // Khi chọn "Trả thưởng trực tiếp", thanh toán thành công
        mvc.perform(post("/api/invoices").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"organizer\":{\"id\":1},\"paymentMethod\":\"Trả thưởng trực tiếp\",\"listRegisteredReceipt\":[{\"id\":862}]}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.paymentMethod").value("Trả thưởng trực tiếp"))
                .andExpect(jsonPath("$.status").value("Thanh toán thành công"));
    }

    @Test
    void driverRewardsEndpoint() throws Exception {
        mvc.perform(get("/api/drivers/3/rewards"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", not(empty())))
                .andExpect(jsonPath("$[0].registeredDriver.driver.fullName").value("Lando Norris"));
    }

    @Test
    void sponsorContractsEndpoint() throws Exception {
        mvc.perform(get("/api/sponsors/2/contracts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(2))))
                .andExpect(jsonPath("$[*].status", hasItems("Chờ ký", "Đã ký")));
    }

    @Test
    void sponsorLoginEndpoint() throws Exception {
        // Đăng nhập bằng taxCode
        mvc.perform(post("/api/sponsors/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"0123456789\",\"password\":\"b@123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Heineken"));

        // Sai mật khẩu
        mvc.perform(post("/api/sponsors/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"0123456789\",\"password\":\"wrong\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void driverLoginEndpoint() throws Exception {
        mvc.perform(post("/api/drivers/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"c\",\"password\":\"c@123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("Lando Norris"));
    }

    @Test
    void authUniversalLoginAllRoles() throws Exception {
        // Organizer
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"a\",\"password\":\"a@123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("ORGANIZER"));

        // Sponsor qua taxCode
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"0123456789\",\"password\":\"b@123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("SPONSOR"));

        // Driver
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"c\",\"password\":\"c@123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("DRIVER"));

        // Customer
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"d\",\"password\":\"d@123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("CUSTOMER"));
    }
}

