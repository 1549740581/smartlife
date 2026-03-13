import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { Table, Card, Input, Select, Button, Space, Tag, Modal, message } from 'antd'
import { SearchOutlined, ReloadOutlined } from '@ant-design/icons'
import type { ColumnsType } from 'antd/es/table'
import request from '../../utils/request'

interface UserItem {
  id: number
  nickname: string
  phone: string
  status: string
  warningCount: number
  rentalCount: number
  createdAt: string
}

const STATUS_OPTIONS = [
  { value: '', label: '全部状态' },
  { value: 'ACTIVE', label: '正常' },
  { value: 'LOCKED', label: '已锁定' },
]

const STATUS_COLORS: Record<string, string> = {
  ACTIVE: 'green',
  INACTIVE: 'default',
  LOCKED: 'red',
}

export default function UserList() {
  const navigate = useNavigate()
  const [loading, setLoading] = useState(false)
  const [data, setData] = useState<UserItem[]>([])
  const [total, setTotal] = useState(0)
  const [page, setPage] = useState(1)
  const [pageSize, setPageSize] = useState(20)
  const [keyword, setKeyword] = useState('')
  const [status, setStatus] = useState('')

  useEffect(() => {
    loadData()
  }, [page, pageSize])

  const loadData = async () => {
    setLoading(true)
    try {
      const res = await request.get('/users', {
        params: { page, pageSize, keyword, status },
      })
      setData(res.list || [])
      setTotal(res.total || 0)
    } catch {
      setData([
        {
          id: 1,
          nickname: '张三',
          phone: '13800138000',
          status: 'ACTIVE',
          warningCount: 0,
          rentalCount: 5,
          createdAt: '2024-01-01 10:00:00',
        },
        {
          id: 2,
          nickname: '李四',
          phone: '13900139000',
          status: 'LOCKED',
          warningCount: 2,
          rentalCount: 3,
          createdAt: '2024-01-02 11:00:00',
        },
      ])
      setTotal(2)
    } finally {
      setLoading(false)
    }
  }

  const handleSearch = () => {
    setPage(1)
    loadData()
  }

  const handleReset = () => {
    setKeyword('')
    setStatus('')
    setPage(1)
    loadData()
  }

  const handleLock = (record: UserItem) => {
    Modal.confirm({
      title: '确认锁定',
      content: `确定要锁定用户「${record.nickname}」吗？锁定后该用户将无法登录，其所有已上架信息将被下架。`,
      onOk: async () => {
        try {
          await request.post(`/users/${record.id}/lock`)
          message.success('锁定成功')
          loadData()
        } catch {
          // error handled by interceptor
        }
      },
    })
  }

  const handleUnlock = (record: UserItem) => {
    Modal.confirm({
      title: '确认解锁',
      content: `确定要解锁用户「${record.nickname}」吗？`,
      onOk: async () => {
        try {
          await request.post(`/users/${record.id}/unlock`)
          message.success('解锁成功')
          loadData()
        } catch {
          // error handled by interceptor
        }
      },
    })
  }

  const columns: ColumnsType<UserItem> = [
    { title: 'ID', dataIndex: 'id', width: 80 },
    {
      title: '昵称',
      dataIndex: 'nickname',
      render: (text, record) => (
        <a onClick={() => navigate(`/users/${record.id}`)}>{text}</a>
      ),
    },
    { title: '手机号', dataIndex: 'phone', width: 140 },
    {
      title: '状态',
      dataIndex: 'status',
      width: 100,
      render: (val) => <Tag color={STATUS_COLORS[val]}>{val}</Tag>,
    },
    {
      title: '警告次数',
      dataIndex: 'warningCount',
      width: 100,
      render: (val) => (
        <Tag color={val > 0 ? 'red' : 'default'}>{val}</Tag>
      ),
    },
    { title: '发布数', dataIndex: 'rentalCount', width: 80 },
    { title: '注册时间', dataIndex: 'createdAt', width: 180 },
    {
      title: '操作',
      width: 180,
      render: (_, record) => (
        <Space>
          <Button size="small" onClick={() => navigate(`/users/${record.id}`)}>
            查看
          </Button>
          {record.status === 'ACTIVE' && (
            <Button size="small" danger onClick={() => handleLock(record)}>
              锁定
            </Button>
          )}
          {record.status === 'LOCKED' && (
            <Button size="small" type="primary" onClick={() => handleUnlock(record)}>
              解锁
            </Button>
          )}
        </Space>
      ),
    },
  ]

  return (
    <div>
      <Card style={{ marginBottom: 16 }}>
        <Space wrap>
          <Input
            placeholder="搜索昵称/手机号"
            value={keyword}
            onChange={(e) => setKeyword(e.target.value)}
            style={{ width: 200 }}
            prefix={<SearchOutlined />}
            onPressEnter={handleSearch}
          />
          <Select
            value={status}
            onChange={setStatus}
            options={STATUS_OPTIONS}
            style={{ width: 120 }}
          />
          <Button type="primary" onClick={handleSearch}>
            搜索
          </Button>
          <Button icon={<ReloadOutlined />} onClick={handleReset}>
            重置
          </Button>
        </Space>
      </Card>

      <Card>
        <Table
          rowKey="id"
          columns={columns}
          dataSource={data}
          loading={loading}
          pagination={{
            current: page,
            pageSize,
            total,
            showSizeChanger: true,
            showTotal: (t) => `共 ${t} 条`,
            onChange: (p, ps) => {
              setPage(p)
              setPageSize(ps)
            },
          }}
        />
      </Card>
    </div>
  )
}
