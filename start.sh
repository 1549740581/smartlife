#!/bin/bash

# Smart Life Backend Service 启动脚本

echo "正在启动 Smart Life Backend Service..."

# 检查Java版本
java_version=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}')
echo "Java版本: $java_version"

# 检查Maven是否安装
if ! command -v mvn &> /dev/null; then
    echo "错误: Maven未安装或不在PATH中"
    exit 1
fi

# 编译项目
echo "正在编译项目..."
mvn clean compile -q

if [ $? -ne 0 ]; then
    echo "错误: 项目编译失败"
    exit 1
fi

echo "编译成功！"

# 启动应用
echo "正在启动应用..."
cd app-web
mvn spring-boot:run
