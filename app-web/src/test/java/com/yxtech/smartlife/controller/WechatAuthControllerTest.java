package com.yxtech.smartlife.controller;

import com.yxtech.smartlife.TestWebApplication;
import com.yxtech.smartlife.entity.User;
import com.yxtech.smartlife.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(WechatAuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@ContextConfiguration(classes = TestWebApplication.class)
class WechatAuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Test
    void loginShouldCreateOrReturnWechatUser() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setOpenId("wx-code");
        user.setNickname("张三");

        when(userService.loginOrRegisterWechatUser("wx-code", "张三", "https://img/avatar.png")).thenReturn(user);

        mockMvc.perform(post("/api/wechat/login")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "code":"wx-code",
                                  "nickname":"张三",
                                  "avatarUrl":"https://img/avatar.png"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.userId").value(1))
                .andExpect(jsonPath("$.data.openId").value("wx-code"))
                .andExpect(jsonPath("$.data.nickname").value("张三"));
    }
}
