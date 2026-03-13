import { useEffect, useState } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import {
  Card,
  Descriptions,
  Tag,
  Button,
  Space,
  Image,
  Table,
  Spin,
  Modal,
  Form,
  Input,
  message,
} from 'antd'
import { ArrowLeftOutlined } from '@ant-design/icons'
import request from '../../utils/request'

interface RentalDetail {
  id: number
  title: string
  description: string
  rentalType: string
  status: string
  price: number | null
  contactName: string
  contactPhone: string
  city: string
  district: string
  street: string
  communityName: string
  imageUrls: string[]
  publisherUserId: number
  publisherNickname: string
  createdAt: string
  updatedAt: string
  reviewRecords: {
    id: number
    action: string
    fromStatus: string
    toStatus: string
    reason: string
    reviewerUserId: number
    createdAt: string
  }[]
}

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

export default function RentalDetail() {
  const { id } = useParams()
  const navigate = useNavigate()
  const [loading, setLoading] = useState(true)
  const [rental, setRental] = useState<RentalDetail | null>(null)
  const [reviewModalOpen, setReviewModalOpen] = useState(false)
  const [reviewForm] = Form.useForm()

  useEffect(() => {
    loadDetail()
  }, [id])

  const loadDetail = async () => {
    setLoading(true)
    try {
      const res = await request.get(`/rentals/${id}`)
      setRental(res)
    } catch {
      // 使用模拟数据
      setRental({
        id: Number(id),
        title: '朝阳区精装两居室出租',
        description: '小区环境优美，交通便利，近地铁站，周边配套设施完善。',
        rentalType: 'HOUSE',
        status: 'PENDING',
        price: 5000,
        contactName: '张先生',
        contactPhone: '13800138000',
        city: '杭州',
        district: '西湖区',
        street: '文三路',
        communityName: '翠苑小区',
        imageUrls: [],
        publisherUserId: 1,
        publisherNickname: '张三',
        createdAt: '2024-03-01 10:00:00',
        updatedAt: '2024-03-01 10:00:00',
        reviewRecords: [],
      })
    } finally {
      setLoading(false)
    }
  }

  const handleReview = async (approved: boolean) => {
    try {
      const values = await reviewForm.validateFields()
      await request.post(`/rentals/${id}/review`, {
        approved,
        reason: values.reason,
      })
      message.success(approved ? '审核通过' : '审核拒绝')
      setReviewModalOpen(false)
      loadDetail()
    } catch {
      if (!approved) {
        message.error('请填写拒绝理由')
      }
    }
  }

  const handleOffline = () => {
    Modal.confirm({
      title: '确认下架',
      content: `确定要下架「${rental?.title}」吗？`,
      onOk: async () => {
        try {
          await request.post(`/rentals/${id}/offline`)
          message.success('下架成功')
          loadDetail()
        } catch {
          // error handled by interceptor
        }
      },
    })
  }

  if (loading) {
    return (
      <div style={{ textAlign: 'center', padding: 100 }}>
        <Spin size="large" />
      </div>
    )
  }

  if (!rental) {
    return <div>信息不存在</div>
  }

  const reviewColumns = [
    { title: 'ID', dataIndex: 'id', width: 80 },
    { title: '操作', dataIndex: 'action', width: 100 },
    { title: '原状态', dataIndex: 'fromStatus', width: 100 },
    { title: '新状态', dataIndex: 'toStatus', width: 100 },
    { title: '原因', dataIndex: 'reason', ellipsis: true },
    { title: '时间', dataIndex: 'createdAt', width: 180 },
  ]

  return (
    <div>
      <Button
        icon={<ArrowLeftOutlined />}
        style={{ marginBottom: 16 }}
        onClick={() => navigate(-1)}
      >
        返回
      </Button>

      <Card title="基本信息" style={{ marginBottom: 16 }}>
        <Descriptions column={2}>
          <Descriptions.Item label="ID">{rental.id}</Descriptions.Item>
          <Descriptions.Item label="状态">
            <Tag color={STATUS_COLORS[rental.status]}>{rental.status}</Tag>
          </Descriptions.Item>
          <Descriptions.Item label="标题" span={2}>
            {rental.title}
          </Descriptions.Item>
          <Descriptions.Item label="类型">
            {TYPE_LABELS[rental.rentalType] || rental.rentalType}
          </Descriptions.Item>
          <Descriptions.Item label="价格">
            {rental.price ? `¥${rental.price}` : '面议'}
          </Descriptions.Item>
          <Descriptions.Item label="描述" span={2}>
            {rental.description}
          </Descriptions.Item>
          <Descriptions.Item label="联系人">{rental.contactName}</Descriptions.Item>
          <Descriptions.Item label="联系电话">{rental.contactPhone}</Descriptions.Item>
          <Descriptions.Item label="地址" span={2}>
            {[rental.city, rental.district, rental.street, rental.communityName]
              .filter(Boolean)
              .join(' / ')}
          </Descriptions.Item>
          <Descriptions.Item label="发布人">
            <a onClick={() => navigate(`/users/${rental.publisherUserId}`)}>
              {rental.publisherNickname}
            </a>
          </Descriptions.Item>
          <Descriptions.Item label="发布时间">{rental.createdAt}</Descriptions.Item>
        </Descriptions>

        <Space style={{ marginTop: 16 }}>
          {rental.status === 'PENDING' && (
            <Button type="primary" onClick={() => setReviewModalOpen(true)}>
              审核
            </Button>
          )}
          {rental.status === 'APPROVED' && (
            <Button danger onClick={handleOffline}>
              下架
            </Button>
          )}
        </Space>
      </Card>

      {rental.imageUrls && rental.imageUrls.length > 0 && (
        <Card title="图片" style={{ marginBottom: 16 }}>
          <Image.PreviewGroup>
            <Space wrap>
              {rental.imageUrls.map((url, index) => (
                <Image key={index} width={150} height={150} src={url} style={{ objectFit: 'cover' }} />
              ))}
            </Space>
          </Image.PreviewGroup>
        </Card>
      )}

      {rental.reviewRecords && rental.reviewRecords.length > 0 && (
        <Card title="审核记录">
          <Table
            rowKey="id"
            columns={reviewColumns}
            dataSource={rental.reviewRecords}
            pagination={false}
          />
        </Card>
      )}

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
        <Form form={reviewForm} layout="vertical">
          <Form.Item name="reason" label="拒绝理由（拒绝时必填）">
            <Input.TextArea rows={3} placeholder="请输入拒绝理由" />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  )
}
