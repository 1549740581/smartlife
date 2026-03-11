package com.yxtech.smartlife.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.yxtech.smartlife.factory.TestDataFactory
import io.kotest.core.spec.style.BehaviorSpec
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.http.MediaType
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.util.*

/**
 * UserController 测试类
 * 使用MockMvc进行Web层测试
 *
 * @author Smart Life Team
 */
@WebMvcTest(UserController::class)
@ContextConfiguration(classes = [UserControllerTest.TestConfig::class])
class UserControllerTest : BehaviorSpec() {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var userService: UserService

    @TestConfiguration
    class TestConfig {
        @Bean
        @Primary
        fun mockUserService(): UserService = mockk()
    }

    init {
        Given("UserController API测试") {
            
            When("POST /api/users - 创建用户") {
                Then("应该成功创建用户") {
                    // Given
                    val request = CreateUserRequest()
                    request.username = "testuser"
                    request.password = "password123"
                    request.email = "test@example.com"
                    request.nickname = "Test User"
                    
                    val createdUser = TestDataFactory.createTestUser(
                        1L, "testuser", "password123", "test@example.com", "Test User"
                    )
                    
                    every { userService.createUser(any()) } returns createdUser
                    
                    // When & Then
                    mockMvc.perform(
                        post("/api/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                    )
                        .andExpect(status().isCreated)
                        .andExpect(jsonPath("$.id").value(1))
                        .andExpect(jsonPath("$.username").value("testuser"))
                        .andExpect(jsonPath("$.email").value("test@example.com"))
                        .andExpect(jsonPath("$.nickname").value("Test User"))
                    
                    verify { userService.createUser(any()) }
                }
            }
            
            When("GET /api/users/{id} - 根据ID获取用户") {
                Then("应该返回用户信息") {
                    // Given
                    val userId = 1L
                    val user = TestDataFactory.createTestUser(userId)
                    
                    every { userService.findById(userId) } returns Optional.of(user)
                    
                    // When & Then
                    mockMvc.perform(get("/api/users/{id}", userId))
                        .andExpect(status().isOk)
                        .andExpect(jsonPath("$.id").value(userId))
                        .andExpect(jsonPath("$.username").value(user.username))
                        .andExpect(jsonPath("$.email").value(user.email))
                }

                Then("用户不存在时应该返回404") {
                    // Given
                    val userId = 999L
                    every { userService.findById(userId) } returns Optional.empty()
                    
                    // When & Then
                    mockMvc.perform(get("/api/users/{id}", userId))
                        .andExpect(status().isNotFound)
                }
            }
            
            When("GET /api/users/username/{username} - 根据用户名获取用户") {
                Then("应该返回用户信息") {
                    // Given
                    val username = "testuser"
                    val user = TestDataFactory.createTestUser(null, username)
                    
                    every { userService.findByUsername(username) } returns Optional.of(user)
                    
                    // When & Then
                    mockMvc.perform(get("/api/users/username/{username}", username))
                        .andExpect(status().isOk)
                        .andExpect(jsonPath("$.username").value(username))
                }
            }
            
            When("GET /api/users/check/username/{username} - 检查用户名是否存在") {
                Then("用户名存在时应该返回true") {
                    // Given
                    val username = "existinguser"
                    every { userService.existsByUsername(username) } returns true
                    
                    // When & Then
                    mockMvc.perform(get("/api/users/check/username/{username}", username))
                        .andExpect(status().isOk)
                        .andExpect(jsonPath("$.exists").value(true))
                }

                Then("用户名不存在时应该返回false") {
                    // Given
                    val username = "nonexistentuser"
                    every { userService.existsByUsername(username) } returns false
                    
                    // When & Then
                    mockMvc.perform(get("/api/users/check/username/{username}", username))
                        .andExpect(status().isOk)
                        .andExpect(jsonPath("$.exists").value(false))
                }
            }
        }
    }
}
