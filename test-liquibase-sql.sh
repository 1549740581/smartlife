#!/bin/bash

# Liquibase SQL格式测试脚本
# 用于验证SQL格式的changelog是否正常工作

echo "=== Liquibase SQL格式测试 ==="

# 检查Liquibase配置
echo "1. 检查Liquibase配置文件..."
if [ -f "liquibase.properties" ]; then
    echo "✓ liquibase.properties 存在"
    grep "changeLogFile" liquibase.properties
else
    echo "✗ liquibase.properties 不存在"
    exit 1
fi

# 检查SQL changelog文件
echo -e "\n2. 检查SQL changelog文件..."
SQL_MASTER="app-core/src/main/resources/db/changelog/db.changelog-master.sql"
if [ -f "$SQL_MASTER" ]; then
    echo "✓ 主changelog文件存在: $SQL_MASTER"
else
    echo "✗ 主changelog文件不存在: $SQL_MASTER"
    exit 1
fi

# 验证changelog语法
echo -e "\n3. 验证changelog语法..."
if command -v mvn &> /dev/null; then
    echo "使用Maven验证..."
    mvn liquibase:validate -q
    if [ $? -eq 0 ]; then
        echo "✓ Changelog语法验证通过"
    else
        echo "✗ Changelog语法验证失败"
        exit 1
    fi
else
    echo "Maven未安装，跳过语法验证"
fi

# 检查数据库连接（可选）
echo -e "\n4. 检查数据库连接状态..."
if command -v mysql &> /dev/null; then
    # 从配置文件读取数据库信息
    DB_URL=$(grep "^url=" liquibase.properties | cut -d'=' -f2)
    DB_USER=$(grep "^username=" liquibase.properties | cut -d'=' -f2)
    
    if [[ $DB_URL == *"localhost"* ]]; then
        echo "检测到本地数据库配置，尝试连接..."
        # 这里可以添加数据库连接测试
        echo "提示: 请确保MySQL服务正在运行，数据库 'smart_life' 已创建"
    fi
else
    echo "MySQL客户端未安装，跳过数据库连接检查"
fi

# 显示changeset状态（如果数据库可用）
echo -e "\n5. 显示changeset状态..."
if command -v mvn &> /dev/null; then
    echo "获取changeset状态..."
    mvn liquibase:status -q 2>/dev/null || echo "无法连接数据库或获取状态"
fi

echo -e "\n=== 测试完成 ==="
echo "如果所有检查都通过，你可以运行以下命令来更新数据库："
echo "  mvn liquibase:update"
echo ""
echo "或者先生成SQL预览："
echo "  mvn liquibase:updateSQL"
echo ""
echo "查看详细的使用说明："
echo "  cat app-core/src/main/resources/db/changelog/README-SQL-FORMAT.md"
