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
                        .content("{\"username\":\"b\",\"password\":\"x\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Tên đăng nhập hoặc mật khẩu không chính xác"));
    }

    @Test
    void loginDoesNotExposePassword() throws Exception {
        mvc.perform(post("/api/organizers/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"b\",\"password\":\"b@123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("Nguyễn Thị B"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void searchSponsorsWithoutContracts() throws Exception {
        mvc.perform(get("/api/sponsors").param("name", "red"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].name", hasItems("Red Bull", "Red Blue")))
                .andExpect(jsonPath("$[0].listContract").value(nullValue()));
    }

    @Test
    void raceInfoHasResultsAndPaymentStatus() throws Exception {
        mvc.perform(get("/api/races/3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.season.name").value("F1 FIA 2025"))
                .andExpect(jsonPath("$.listRegisteredReceipt[0].registeredDriver.driver.fullName").value("Trần Văn B"))
                .andExpect(jsonPath("$.listRegisteredReceipt[0].reward").value(100000000))
                .andExpect(jsonPath("$.listRegisteredReceipt[2].paymentStatus").value("Đã trả"));
    }

    @Test
    void payInvoiceAndErrors() throws Exception {
        mvc.perform(get("/api/races/3"));
        mvc.perform(post("/api/invoices").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"organizer\":{\"id\":2},\"listRegisteredReceipt\":[{\"id\":1},{\"id\":8}]}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Thanh toán không thành công: Thiếu thông tin nhận thưởng: Lê Minh Cường"));
        mvc.perform(post("/api/invoices").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"organizer\":{\"id\":2},\"listRegisteredReceipt\":[{\"id\":1},{\"id\":2}]}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.amount").value(172000000))
                .andExpect(jsonPath("$.organizer.fullName").value("Nguyễn Thị B"))
                .andExpect(jsonPath("$.listRegisteredReceipt", hasSize(2)))
                .andExpect(jsonPath("$.listRegisteredReceipt[0].race.season.year").value(2025));
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
                        .content("{\"username\":\"redblue\",\"password\":\"redblue@123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("SPONSOR"))
                .andExpect(jsonPath("$.user.name").value("Red Blue"));

        // 3. Sponsor login bằng mã số thuế
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"0123456789\",\"password\":\"redblue@123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("SPONSOR"))
                .andExpect(jsonPath("$.user.name").value("Red Blue"));

        // 4. Driver
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"driver.tranvanb\",\"password\":\"123456\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("DRIVER"))
                .andExpect(jsonPath("$.user.fullName").value("Trần Văn B"));

        // 5. Customer
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"customer\",\"password\":\"customer@123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("CUSTOMER"));
    }

    @Test
    void payDirectRewardSuccessWithoutBankAccount() throws Exception {
        mvc.perform(get("/api/races/3"));
        // Receipt 8 là Lê Minh Cường (không có tài khoản ngân hàng mặc định)
        // Khi chọn "Trả thưởng trực tiếp", thanh toán thành công
        mvc.perform(post("/api/invoices").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"organizer\":{\"id\":2},\"paymentMethod\":\"Trả thưởng trực tiếp\",\"listRegisteredReceipt\":[{\"id\":8}]}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.paymentMethod").value("Trả thưởng trực tiếp"))
                .andExpect(jsonPath("$.status").value("Thanh toán thành công"));
    }

    @Test
    void driverRewardsEndpoint() throws Exception {
        mvc.perform(get("/api/drivers/20/rewards"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", not(empty())))
                .andExpect(jsonPath("$[0].registeredDriver.driver.fullName").value("Trần Văn B"));
    }

    @Test
    void sponsorContractsEndpoint() throws Exception {
        mvc.perform(get("/api/sponsors/4/contracts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(2))))
                .andExpect(jsonPath("$[*].status", hasItems("Chờ ký", "Đã ký")));
    }

    @Test
    void sponsorLoginEndpoint() throws Exception {
        // Đăng nhập bằng taxCode
        mvc.perform(post("/api/sponsors/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"0123456789\",\"password\":\"redblue@123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Red Blue"));

        // Sai mật khẩu
        mvc.perform(post("/api/sponsors/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"0123456789\",\"password\":\"wrong\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void driverLoginEndpoint() throws Exception {
        mvc.perform(post("/api/drivers/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"driver.tranvanb\",\"password\":\"123456\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("Trần Văn B"));
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
                        .content("{\"username\":\"0123456789\",\"password\":\"redblue@123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("SPONSOR"));

        // Driver
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"driver.tranvanb\",\"password\":\"123456\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("DRIVER"));

        // Customer
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"customer\",\"password\":\"customer@123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("CUSTOMER"));
    }
}

