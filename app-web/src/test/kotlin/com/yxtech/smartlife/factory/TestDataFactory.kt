package com.yxtech.smartlife.factory

import com.yxtech.smartlife.entity.User

/**
 * 测试数据工厂类
 * 提供创建测试数据的便捷方法
 *
 * @author Smart Life Team
 */
object TestDataFactory {

    /**
     * 创建测试用户
     */
    fun createTestUser(
        id: Long? = null,
        username: String = "testuser",
        password: String = "password123",
        email: String = "test@example.com",
        nickname: String = "Test User",
        phone: String? = null,
        status: User.UserStatus = User.UserStatus.ACTIVE,
        deleted: Boolean = false
    ): User {
        val user = User()
        user.id = id
        user.username = username
        user.password = password
        user.email = email
        user.nickname = nickname
        user.phone = phone
        user.status = status
        user.deleted = deleted
        return user
    }

    /**
     * 创建用户创建请求
     */
    fun createUserRequest(
        username: String = "testuser",
        password: String = "password123",
        email: String = "test@example.com",
        nickname: String = "Test User",
        phone: String? = null
    ): CreateUserRequest {
        val request = CreateUserRequest()
        request.username = username
        request.password = password
        request.email = email
        request.nickname = nickname
        request.phone = phone
        return request
    }
}

/**
 * 扩展函数：创建测试用户
 */
fun testUser(
    id: Long? = null,
    username: String = "testuser",
    password: String = "password123",
    email: String = "test@example.com",
    nickname: String = "Test User",
    phone: String? = null,
    status: User.UserStatus = User.UserStatus.ACTIVE,
    deleted: Boolean = false
): User = TestDataFactory.createTestUser(id, username, password, email, nickname, phone, status, deleted)
