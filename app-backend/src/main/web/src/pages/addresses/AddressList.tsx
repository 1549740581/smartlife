import { useEffect, useState } from 'react'
import {
  Table,
  Card,
  Input,
  Button,
  Space,
  Modal,
  Form,
  message,
  Popconfirm,
} from 'antd'
import { PlusOutlined, SearchOutlined, ReloadOutlined } from '@ant-design/icons'
import type { ColumnsType } from 'antd/es/table'
import request from '../../utils/request'

interface AddressItem {
  id: number
  city: string
  district: string
  street: string
  communityName: string
  createdAt: string
}

export default function AddressList() {
  const [loading, setLoading] = useState(false)
  const [data, setData] = useState<AddressItem[]>([])
  const [total, setTotal] = useState(0)
  const [page, setPage] = useState(1)
  const [pageSize, setPageSize] = useState(20)
  const [keyword, setKeyword] = useState('')
  const [modalOpen, setModalOpen] = useState(false)
  const [editingId, setEditingId] = useState<number | null>(null)
  const [form] = Form.useForm()

  useEffect(() => {
    loadData()
  }, [page, pageSize])

  const loadData = async () => {
    setLoading(true)
    try {
      const res = await request.get('/addresses', {
        params: { page, pageSize, keyword },
      })
      setData(res.list || [])
      setTotal(res.total || 0)
    } catch {
      setData([
        {
          id: 1,
          city: '杭州',
          district: '西湖区',
          street: '文三路',
          communityName: '翠苑小区',
          createdAt: '2024-01-01 10:00:00',
        },
        {
          id: 2,
          city: '杭州',
          district: '滨江区',
          street: '江南大道',
          communityName: '钱塘春晓',
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
    setPage(1)
    loadData()
  }

  const openAddModal = () => {
    setEditingId(null)
    form.resetFields()
    setModalOpen(true)
  }

  const openEditModal = (record: AddressItem) => {
    setEditingId(record.id)
    form.setFieldsValue({
      city: record.city,
      district: record.district,
      street: record.street,
      communityName: record.communityName,
    })
    setModalOpen(true)
  }

  const handleSubmit = async () => {
    try {
      const values = await form.validateFields()
      if (editingId) {
        await request.put(`/addresses/${editingId}`, values)
        message.success('修改成功')
      } else {
        await request.post('/addresses', values)
        message.success('添加成功')
      }
      setModalOpen(false)
      loadData()
    } catch {
      // validation error or request error
    }
  }

  const handleDelete = async (id: number) => {
    try {
      await request.delete(`/addresses/${id}`)
      message.success('删除成功')
      loadData()
    } catch {
      // error handled by interceptor
    }
  }

  const columns: ColumnsType<AddressItem> = [
    { title: 'ID', dataIndex: 'id', width: 80 },
    { title: '城市', dataIndex: 'city', width: 100 },
    { title: '区', dataIndex: 'district', width: 120 },
    { title: '街道', dataIndex: 'street', width: 150 },
    { title: '小区名称', dataIndex: 'communityName', ellipsis: true },
    { title: '创建时间', dataIndex: 'createdAt', width: 180 },
    {
      title: '操作',
      width: 150,
      render: (_, record) => (
        <Space>
          <Button size="small" onClick={() => openEditModal(record)}>
            编辑
          </Button>
          <Popconfirm
            title="确认删除"
            description="删除后不可恢复，确定删除吗？"
            onConfirm={() => handleDelete(record.id)}
          >
            <Button size="small" danger>
              删除
            </Button>
          </Popconfirm>
        </Space>
      ),
    },
  ]

  return (
    <div>
      <Card style={{ marginBottom: 16 }}>
        <Space wrap>
          <Input
            placeholder="搜索城市/区/街道/小区"
            value={keyword}
            onChange={(e) => setKeyword(e.target.value)}
            style={{ width: 220 }}
            prefix={<SearchOutlined />}
            onPressEnter={handleSearch}
          />
          <Button type="primary" onClick={handleSearch}>
            搜索
          </Button>
          <Button icon={<ReloadOutlined />} onClick={handleReset}>
            重置
          </Button>
          <Button type="primary" icon={<PlusOutlined />} onClick={openAddModal}>
            新增地址
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
        title={editingId ? '编辑地址' : '新增地址'}
        open={modalOpen}
        onCancel={() => setModalOpen(false)}
        onOk={handleSubmit}
      >
        <Form form={form} layout="vertical">
          <Form.Item
            name="city"
            label="城市"
            rules={[{ required: true, message: '请输入城市' }]}
          >
            <Input placeholder="请输入城市" disabled={!!editingId} />
          </Form.Item>
          <Form.Item
            name="district"
            label="区"
            rules={[{ required: true, message: '请输入区' }]}
          >
            <Input placeholder="请输入区" disabled={!!editingId} />
          </Form.Item>
          <Form.Item
            name="street"
            label="街道"
            rules={[{ required: true, message: '请输入街道' }]}
          >
            <Input placeholder="请输入街道" disabled={!!editingId} />
          </Form.Item>
          <Form.Item
            name="communityName"
            label="小区名称"
            rules={[{ required: true, message: '请输入小区名称' }]}
          >
            <Input placeholder="请输入小区名称" />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  )
}
