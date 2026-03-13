import { useEffect, useState } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { Card, Descriptions, Tag, Button, Space, Table, Spin, Modal, message } from 'antd'
import { ArrowLeftOutlined } from '@ant-design/icons'
import type { ColumnsType } from 'antd/es/table'
import request from '../../utils/request'

interface UserDetail {
  id: number
  nickname: string
  phone: string
  email: string
  status: string
  warningCount: number
  createdAt: string
}

interface RentalItem {
  id: number
  title: string
  rentalType: string
  status: string
  createdAt: string
}

const STATUS_COLORS: Record<string, string> = {
  ACTIVE: 'green',
  INACTIVE: 'default',
  LOCKED: 'red',
}

const RENTAL_STATUS_COLORS: Record<string, string> = {
  PENDING: 'orange',
  APPROVED: 'green',
  REJECTED: 'red',
  OFFLINE: 'default',
  RENTED: 'blue',
}

const TYPE_LABELS: Record<string, string> = {
  HOUSE: '房屋',
  PARKING: '车位',
  ITEM: '闲置物品',
}

export default function UserDetail() {
  const { id } = useParams()
  const navigate = useNavigate()
  const [loading, setLoading] = useState(true)
  const [user, setUser] = useState<UserDetail | null>(null)
  const [rentals, setRentals] = useState<RentalItem[]>([])

  useEffect(() => {
    loadData()
  }, [id])

  const loadData = async () => {
    setLoading(true)
    try {
      const [userRes, rentalsRes] = await Promise.all([
        request.get(`/users/${id}`),
        request.get(`/users/${id}/rentals`),
      ])
      setUser(userRes)
      setRentals(rentalsRes || [])
    } catch {
      setUser({
        id: Number(id),
        nickname: '张三',
        phone: '13800138000',
        email: 'zhangsan@example.com',
        status: 'ACTIVE',
        warningCount: 0,
        createdAt: '2024-01-01 10:00:00',
      })
      setRentals([
        {
          id: 1,
          title: '朝阳区精装两居室出租',
          rentalType: 'HOUSE',
          status: 'APPROVED',
          createdAt: '2024-03-01 10:00:00',
        },
      ])
    } finally {
      setLoading(false)
    }
  }

  const handleLock = () => {
    Modal.confirm({
      title: '确认锁定',
      content: `确定要锁定用户「${user?.nickname}」吗？锁定后该用户将无法登录，其所有已上架信息将被下架。`,
      onOk: async () => {
        try {
          await request.post(`/users/${id}/lock`)
          message.success('锁定成功')
          loadData()
        } catch {
          // error handled by interceptor
        }
      },
    })
  }

  const handleUnlock = () => {
    Modal.confirm({
      title: '确认解锁',
      content: `确定要解锁用户「${user?.nickname}」吗？`,
      onOk: async () => {
        try {
          await request.post(`/users/${id}/unlock`)
          message.success('解锁成功')
          loadData()
        } catch {
          // error handled by interceptor
        }
      },
    })
  }

  const rentalColumns: ColumnsType<RentalItem> = [
    { title: 'ID', dataIndex: 'id', width: 80 },
    {
      title: '标题',
      dataIndex: 'title',
      ellipsis: true,
      render: (text, record) => (
        <a onClick={() => navigate(`/rentals/${record.id}`)}>{text}</a>
      ),
    },
    {
      title: '类型',
      dataIndex: 'rentalType',
      width: 100,
      render: (val) => TYPE_LABELS[val] || val,
    },
    {
      title: '状态',
      dataIndex: 'status',
      width: 100,
      render: (val) => <Tag color={RENTAL_STATUS_COLORS[val]}>{val}</Tag>,
    },
    { title: '发布时间', dataIndex: 'createdAt', width: 180 },
  ]

  if (loading) {
    return (
      <div style={{ textAlign: 'center', padding: 100 }}>
        <Spin size="large" />
      </div>
    )
  }

  if (!user) {
    return <div>用户不存在</div>
  }

  return (
    <div>
      <Button
        icon={<ArrowLeftOutlined />}
        style={{ marginBottom: 16 }}
        onClick={() => navigate(-1)}
      >
        返回
      </Button>

      <Card title="用户信息" style={{ marginBottom: 16 }}>
        <Descriptions column={2}>
          <Descriptions.Item label="用户ID">{user.id}</Descriptions.Item>
          <Descriptions.Item label="状态">
            <Tag color={STATUS_COLORS[user.status]}>{user.status}</Tag>
          </Descriptions.Item>
          <Descriptions.Item label="昵称">{user.nickname}</Descriptions.Item>
          <Descriptions.Item label="手机号">{user.phone}</Descriptions.Item>
          <Descriptions.Item label="邮箱">{user.email || '-'}</Descriptions.Item>
          <Descriptions.Item label="警告次数">
            <Tag color={user.warningCount > 0 ? 'red' : 'default'}>
              {user.warningCount}
            </Tag>
          </Descriptions.Item>
          <Descriptions.Item label="注册时间">{user.createdAt}</Descriptions.Item>
        </Descriptions>

        <Space style={{ marginTop: 16 }}>
          {user.status === 'ACTIVE' && (
            <Button danger onClick={handleLock}>
              锁定用户
            </Button>
          )}
          {user.status === 'LOCKED' && (
            <Button type="primary" onClick={handleUnlock}>
              解锁用户
            </Button>
          )}
        </Space>
      </Card>

      <Card title="发布的信息">
        <Table
          rowKey="id"
          columns={rentalColumns}
          dataSource={rentals}
          pagination={false}
        />
      </Card>
    </div>
  )
}
