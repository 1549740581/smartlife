import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { Table, Card, Select, Button, Space, Tag } from 'antd'
import { ReloadOutlined } from '@ant-design/icons'
import type { ColumnsType } from 'antd/es/table'
import request from '../../utils/request'

interface ComplaintItem {
  id: number
  rentalTitle: string
  rentalId: number
  complainantNickname: string
  complainantUserId: number
  respondentNickname: string
  respondentUserId: number
  reason: string
  status: string
  createdAt: string
}

const STATUS_OPTIONS = [
  { value: '', label: '全部状态' },
  { value: 'PENDING', label: '待处理' },
  { value: 'ACCEPTED', label: '已接受' },
  { value: 'REJECTED', label: '已驳回' },
]

const STATUS_COLORS: Record<string, string> = {
  PENDING: 'orange',
  ACCEPTED: 'green',
  REJECTED: 'default',
}

export default function ComplaintList() {
  const navigate = useNavigate()
  const [loading, setLoading] = useState(false)
  const [data, setData] = useState<ComplaintItem[]>([])
  const [total, setTotal] = useState(0)
  const [page, setPage] = useState(1)
  const [pageSize, setPageSize] = useState(20)
  const [status, setStatus] = useState('')

  useEffect(() => {
    loadData()
  }, [page, pageSize])

  const loadData = async () => {
    setLoading(true)
    try {
      const res = await request.get('/complaints', {
        params: { page, pageSize, status },
      })
      setData(res.list || [])
      setTotal(res.total || 0)
    } catch {
      setData([
        {
          id: 1,
          rentalTitle: '朝阳区精装两居室出租',
          rentalId: 1,
          complainantNickname: '李四',
          complainantUserId: 2,
          respondentNickname: '张三',
          respondentUserId: 1,
          reason: '信息不实，实际房屋与描述不符',
          status: 'PENDING',
          createdAt: '2024-03-01 10:00:00',
        },
      ])
      setTotal(1)
    } finally {
      setLoading(false)
    }
  }

  const handleSearch = () => {
    setPage(1)
    loadData()
  }

  const handleReset = () => {
    setStatus('')
    setPage(1)
    loadData()
  }

  const columns: ColumnsType<ComplaintItem> = [
    { title: 'ID', dataIndex: 'id', width: 80 },
    {
      title: '被投诉信息',
      dataIndex: 'rentalTitle',
      ellipsis: true,
      render: (text, record) => (
        <a onClick={() => navigate(`/rentals/${record.rentalId}`)}>{text}</a>
      ),
    },
    {
      title: '投诉人',
      dataIndex: 'complainantNickname',
      width: 120,
      render: (text, record) => (
        <a onClick={() => navigate(`/users/${record.complainantUserId}`)}>{text}</a>
      ),
    },
    {
      title: '被投诉人',
      dataIndex: 'respondentNickname',
      width: 120,
      render: (text, record) => (
        <a onClick={() => navigate(`/users/${record.respondentUserId}`)}>{text}</a>
      ),
    },
    {
      title: '投诉理由',
      dataIndex: 'reason',
      ellipsis: true,
    },
    {
      title: '状态',
      dataIndex: 'status',
      width: 100,
      render: (val) => <Tag color={STATUS_COLORS[val]}>{val}</Tag>,
    },
    { title: '提交时间', dataIndex: 'createdAt', width: 180 },
    {
      title: '操作',
      width: 120,
      render: (_, record) => (
        <Space>
          <Button size="small" onClick={() => navigate(`/complaints/${record.id}`)}>
            查看
          </Button>
          {record.status === 'PENDING' && (
            <Button
              size="small"
              type="primary"
              onClick={() => navigate(`/complaints/${record.id}`)}
            >
              处理
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
