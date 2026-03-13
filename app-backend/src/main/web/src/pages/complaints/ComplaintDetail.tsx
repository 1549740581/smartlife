import { useEffect, useState } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import {
  Card,
  Descriptions,
  Tag,
  Button,
  Space,
  Image,
  Spin,
  Modal,
  Form,
  Input,
  message,
} from 'antd'
import { ArrowLeftOutlined } from '@ant-design/icons'
import request from '../../utils/request'

interface ComplaintDetail {
  id: number
  rentalId: number
  rentalTitle: string
  complainantUserId: number
  complainantNickname: string
  respondentUserId: number
  respondentNickname: string
  respondentWarningCount: number
  reason: string
  evidenceUrls: string[]
  status: string
  processResult: string | null
  processedAt: string | null
  createdAt: string
}

const STATUS_COLORS: Record<string, string> = {
  PENDING: 'orange',
  ACCEPTED: 'green',
  REJECTED: 'default',
}

export default function ComplaintDetail() {
  const { id } = useParams()
  const navigate = useNavigate()
  const [loading, setLoading] = useState(true)
  const [complaint, setComplaint] = useState<ComplaintDetail | null>(null)
  const [processModalOpen, setProcessModalOpen] = useState(false)
  const [processForm] = Form.useForm()

  useEffect(() => {
    loadDetail()
  }, [id])

  const loadDetail = async () => {
    setLoading(true)
    try {
      const res = await request.get(`/complaints/${id}`)
      setComplaint(res)
    } catch {
      setComplaint({
        id: Number(id),
        rentalId: 1,
        rentalTitle: '朝阳区精装两居室出租',
        complainantUserId: 2,
        complainantNickname: '李四',
        respondentUserId: 1,
        respondentNickname: '张三',
        respondentWarningCount: 0,
        reason: '信息不实，实际房屋与描述不符',
        evidenceUrls: [],
        status: 'PENDING',
        processResult: null,
        processedAt: null,
        createdAt: '2024-03-01 10:00:00',
      })
    } finally {
      setLoading(false)
    }
  }

  const handleProcess = async (accepted: boolean) => {
    try {
      const values = await processForm.validateFields()
      await request.post(`/complaints/${id}/process`, {
        accepted,
        result: values.result,
      })
      message.success(accepted ? '投诉已接受' : '投诉已驳回')
      setProcessModalOpen(false)
      loadDetail()
    } catch {
      if (!accepted) {
        message.error('请填写驳回理由')
      }
    }
  }

  if (loading) {
    return (
      <div style={{ textAlign: 'center', padding: 100 }}>
        <Spin size="large" />
      </div>
    )
  }

  if (!complaint) {
    return <div>投诉不存在</div>
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

      <Card title="投诉详情" style={{ marginBottom: 16 }}>
        <Descriptions column={2}>
          <Descriptions.Item label="投诉ID">{complaint.id}</Descriptions.Item>
          <Descriptions.Item label="状态">
            <Tag color={STATUS_COLORS[complaint.status]}>{complaint.status}</Tag>
          </Descriptions.Item>
          <Descriptions.Item label="被投诉信息">
            <a onClick={() => navigate(`/rentals/${complaint.rentalId}`)}>
              {complaint.rentalTitle}
            </a>
          </Descriptions.Item>
          <Descriptions.Item label="提交时间">{complaint.createdAt}</Descriptions.Item>
          <Descriptions.Item label="投诉人">
            <a onClick={() => navigate(`/users/${complaint.complainantUserId}`)}>
              {complaint.complainantNickname}
            </a>
          </Descriptions.Item>
          <Descriptions.Item label="被投诉人">
            <a onClick={() => navigate(`/users/${complaint.respondentUserId}`)}>
              {complaint.respondentNickname}
            </a>
            <Tag color={complaint.respondentWarningCount > 0 ? 'red' : 'default'} style={{ marginLeft: 8 }}>
              警告 {complaint.respondentWarningCount} 次
            </Tag>
          </Descriptions.Item>
          <Descriptions.Item label="投诉理由" span={2}>
            {complaint.reason}
          </Descriptions.Item>
          {complaint.processResult && (
            <Descriptions.Item label="处理结果" span={2}>
              {complaint.processResult}
            </Descriptions.Item>
          )}
          {complaint.processedAt && (
            <Descriptions.Item label="处理时间">
              {complaint.processedAt}
            </Descriptions.Item>
          )}
        </Descriptions>

        {complaint.status === 'PENDING' && (
          <Space style={{ marginTop: 16 }}>
            <Button type="primary" onClick={() => setProcessModalOpen(true)}>
              处理投诉
            </Button>
          </Space>
        )}
      </Card>

      {complaint.evidenceUrls && complaint.evidenceUrls.length > 0 && (
        <Card title="证据材料">
          <Image.PreviewGroup>
            <Space wrap>
              {complaint.evidenceUrls.map((url, index) => (
                <Image key={index} width={150} height={150} src={url} style={{ objectFit: 'cover' }} />
              ))}
            </Space>
          </Image.PreviewGroup>
        </Card>
      )}

      <Modal
        title="处理投诉"
        open={processModalOpen}
        onCancel={() => setProcessModalOpen(false)}
        footer={
          <Space>
            <Button onClick={() => setProcessModalOpen(false)}>取消</Button>
            <Button danger onClick={() => handleProcess(false)}>
              驳回
            </Button>
            <Button type="primary" onClick={() => handleProcess(true)}>
              接受
            </Button>
          </Space>
        }
      >
        <p style={{ marginBottom: 16, color: '#ff4d4f' }}>
          注意：接受投诉将会下架被投诉信息，并给被投诉人增加一次警告。
          {complaint.respondentWarningCount >= 1 && (
            <strong>该用户已有 {complaint.respondentWarningCount} 次警告，再次警告将被锁定账号！</strong>
          )}
        </p>
        <Form form={processForm} layout="vertical">
          <Form.Item name="result" label="处理说明（驳回时必填）">
            <Input.TextArea rows={3} placeholder="请输入处理说明" />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  )
}
