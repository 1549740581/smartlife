package com.yxtech.smartlife.base

import io.kotest.core.spec.style.BehaviorSpec
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

/**
 * 集成测试基础类
 * 提供Spring Boot测试环境和TestContainers支持
 *
 * @author Smart Life Team
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
abstract class BaseIntegrationTest : BehaviorSpec()
