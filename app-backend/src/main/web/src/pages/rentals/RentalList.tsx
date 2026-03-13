import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import {
  Table,
  Card,
  Input,
  Select,
  Button,
  Space,
  Tag,
  Modal,
  Form,
  message,
} from 'antd'
import { SearchOutlined, ReloadOutlined } from '@ant-design/icons'
import type { ColumnsType } from 'antd/es/table'
import request from '../../utils/request'

interface RentalItem {
  id: number
  title: string
  rentalType: string
  status: string
  price: number | null
  publisherNickname: string
  publisherUserId: number
  createdAt: string
}

const TYPE_OPTIONS = [
  { value: '', label: '全部类型' },
  { value: 'HOUSE', label: '房屋' },
  { value: 'PARKING', label: '车位' },
  { value: 'ITEM', label: '闲置物品' },
]

const STATUS_OPTIONS = [
  { value: '', label: '全部状态' },
  { value: 'PENDING', label: '待审核' },
  { value: 'APPROVED', label: '已通过' },
  { value: 'REJECTED', label: '已驳回' },
  { value: 'OFFLINE', label: '已下架' },
  { value: 'RENTED', label: '已出租' },
]

const STATUS_COLORS: Record<string, string> = {
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

export default function RentalList() {
  const navigate = useNavigate()
  const [loading, setLoading] = useState(false)
  const [data, setData] = useState<RentalItem[]>([])
  const [total, setTotal] = useState(0)
  const [page, setPage] = useState(1)
  const [pageSize, setPageSize] = useState(20)
  const [keyword, setKeyword] = useState('')
  const [type, setType] = useState('')
  const [status, setStatus] = useState('')
  const [reviewModalOpen, setReviewModalOpen] = useState(false)
  const [currentRental, setCurrentRental] = useState<RentalItem | null>(null)
  const [reviewForm] = Form.useForm()

  useEffect(() => {
    loadData()
  }, [page, pageSize])

  const loadData = async () => {
    setLoading(true)
    try {
      const res = await request.get('/rentals', {
        params: { page, pageSize, keyword, type, status },
      })
      setData(res.list || [])
      setTotal(res.total || 0)
    } catch {
      // 使用模拟数据
      setData([
        {
          id: 1,
          title: '朝阳区精装两居室出租',
          rentalType: 'HOUSE',
          status: 'PENDING',
          price: 5000,
          publisherNickname: '张三',
          publisherUserId: 1,
          createdAt: '2024-03-01 10:00:00',
        },
        {
          id: 2,
          title: '地下车位月租',
          rentalType: 'PARKING',
          status: 'APPROVED',
          price: 500,
          publisherNickname: '李四',
          publisherUserId: 2,
          createdAt: '2024-03-02 11:00:00',
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
    setType('')
    setStatus('')
    setPage(1)
    loadData()
  }

  const openReviewModal = (record: RentalItem) => {
    setCurrentRental(record)
    setReviewModalOpen(true)
    reviewForm.resetFields()
  }

  const handleReview = async (approved: boolean) => {
    if (!currentRental) return
    try {
      const values = await reviewForm.validateFields()
      await request.post(`/rentals/${currentRental.id}/review`, {
        approved,
        reason: values.reason,
      })
      message.success(approved ? '审核通过' : '审核拒绝')
      setReviewModalOpen(false)
      loadData()
    } catch {
      if (!approved) {
        message.error('请填写拒绝理由')
      }
    }
  }

  const handleOffline = async (record: RentalItem) => {
    Modal.confirm({
      title: '确认下架',
      content: `确定要下架「${record.title}」吗？`,
      onOk: async () => {
        try {
          await request.post(`/rentals/${record.id}/offline`)
          message.success('下架成功')
          loadData()
        } catch {
          // error handled by interceptor
        }
      },
    })
  }

  const columns: ColumnsType<RentalItem> = [
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
      render: (val) => <Tag color={STATUS_COLORS[val]}>{val}</Tag>,
    },
    {
      title: '价格',
      dataIndex: 'price',
      width: 100,
      render: (val) => (val ? `¥${val}` : '面议'),
    },
    { title: '发布人', dataIndex: 'publisherNickname', width: 120 },
    { title: '发布时间', dataIndex: 'createdAt', width: 180 },
    {
      title: '操作',
      width: 180,
      render: (_, record) => (
        <Space>
          <Button size="small" onClick={() => navigate(`/rentals/${record.id}`)}>
            查看
          </Button>
          {record.status === 'PENDING' && (
            <Button size="small" type="primary" onClick={() => openReviewModal(record)}>
              审核
            </Button>
          )}
          {record.status === 'APPROVED' && (
            <Button size="small" danger onClick={() => handleOffline(record)}>
              下架
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
            placeholder="搜索标题"
            value={keyword}
            onChange={(e) => setKeyword(e.target.value)}
            style={{ width: 200 }}
            prefix={<SearchOutlined />}
            onPressEnter={handleSearch}
          />
          <Select
            value={type}
            onChange={setType}
            options={TYPE_OPTIONS}
            style={{ width: 120 }}
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

      <Modal
        title="审核信息"
        open={reviewModalOpen}
        onCancel={() => setReviewModalOpen(false)}
        footer={
          <Space>
            <Button onClick={() => setReviewModalOpen(false)}>取消</Button>
            <Button danger onClick={() => handleReview(false)}>
              拒绝
            </Button>
            <Button type="primary" onClick={() => handleReview(true)}>
              通过
            </Button>
          </Space>
        }
      >
        <p>标题：{currentRental?.title}</p>
        <Form form={reviewForm} layout="vertical">
          <Form.Item
            name="reason"
            label="拒绝理由（拒绝时必填）"
          >
            <Input.TextArea rows={3} placeholder="请输入拒绝理由" />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  )
}
