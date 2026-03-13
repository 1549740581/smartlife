import { useEffect, useState } from 'react'
import { Row, Col, Card, Statistic, Spin } from 'antd'
import {
  UserOutlined,
  FileTextOutlined,
  ClockCircleOutlined,
  CheckCircleOutlined,
  HomeOutlined,
  WarningOutlined,
} from '@ant-design/icons'
import ReactECharts from 'echarts-for-react'
import request from '../utils/request'

interface OverviewData {
  totalUsers: number
  activeUsers: number
  totalRentals: number
  pendingRentals: number
  approvedRentals: number
  rentedRentals: number
  pendingComplaints: number
}

interface TrendData {
  dates: string[]
  users: number[]
  rentals: number[]
}

interface DistributionData {
  typeDistribution: { name: string; value: number }[]
  statusDistribution: { name: string; value: number }[]
}

export default function Dashboard() {
  const [loading, setLoading] = useState(true)
  const [overview, setOverview] = useState<OverviewData | null>(null)
  const [trends, setTrends] = useState<TrendData | null>(null)
  const [distributions, setDistributions] = useState<DistributionData | null>(null)

  useEffect(() => {
    loadData()
  }, [])

  const loadData = async () => {
    setLoading(true)
    try {
      const [overviewRes, trendsRes, distributionsRes]: any[] = await Promise.all([
        request.post('/dashboard/overview', {}),
        request.post('/dashboard/trends', {}),
        request.post('/dashboard/distributions', {}),
      ])
      setOverview(overviewRes)
      setTrends(trendsRes)
      setDistributions(distributionsRes)
    } catch {
      // 使用模拟数据
      setOverview({
        totalUsers: 128,
        activeUsers: 45,
        totalRentals: 256,
        pendingRentals: 12,
        approvedRentals: 180,
        rentedRentals: 35,
        pendingComplaints: 3,
      })
      setTrends({
        dates: ['03-01', '03-02', '03-03', '03-04', '03-05', '03-06', '03-07'],
        users: [5, 8, 12, 6, 10, 15, 8],
        rentals: [10, 15, 8, 20, 12, 18, 14],
      })
      setDistributions({
        typeDistribution: [
          { name: '房屋', value: 150 },
          { name: '车位', value: 80 },
          { name: '闲置物品', value: 26 },
        ],
        statusDistribution: [
          { name: '待审核', value: 12 },
          { name: '已通过', value: 180 },
          { name: '已驳回', value: 29 },
          { name: '已下架', value: 20 },
          { name: '已出租', value: 35 },
        ],
      })
    } finally {
      setLoading(false)
    }
  }

  const trendOption = {
    tooltip: { trigger: 'axis' },
    legend: { data: ['新增用户', '新增信息'] },
    xAxis: { type: 'category', data: trends?.dates || [] },
    yAxis: { type: 'value' },
    series: [
      {
        name: '新增用户',
        type: 'line',
        smooth: true,
        data: trends?.users || [],
      },
      {
        name: '新增信息',
        type: 'line',
        smooth: true,
        data: trends?.rentals || [],
      },
    ],
  }

  const typeOption = {
    tooltip: { trigger: 'item' },
    legend: { orient: 'vertical', left: 'left' },
    series: [
      {
        name: '信息类型',
        type: 'pie',
        radius: '70%',
        data: distributions?.typeDistribution || [],
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.5)',
          },
        },
      },
    ],
  }

  const statusOption = {
    tooltip: { trigger: 'item' },
    legend: { orient: 'vertical', left: 'left' },
    series: [
      {
        name: '信息状态',
        type: 'pie',
        radius: '70%',
        data: distributions?.statusDistribution || [],
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.5)',
          },
        },
      },
    ],
  }

  if (loading) {
    return (
      <div style={{ textAlign: 'center', padding: 100 }}>
        <Spin size="large" />
      </div>
    )
  }

  return (
    <div>
      <h2 style={{ marginBottom: 24 }}>数据面板</h2>
      <Row gutter={[16, 16]}>
        <Col xs={24} sm={12} lg={6}>
          <Card>
            <Statistic
              title="用户总数"
              value={overview?.totalUsers}
              prefix={<UserOutlined />}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card>
            <Statistic
              title="活跃用户"
              value={overview?.activeUsers}
              prefix={<UserOutlined style={{ color: '#52c41a' }} />}
              valueStyle={{ color: '#52c41a' }}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card>
            <Statistic
              title="信息总数"
              value={overview?.totalRentals}
              prefix={<FileTextOutlined />}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card>
            <Statistic
              title="待审核"
              value={overview?.pendingRentals}
              prefix={<ClockCircleOutlined style={{ color: '#faad14' }} />}
              valueStyle={{ color: '#faad14' }}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card>
            <Statistic
              title="已上架"
              value={overview?.approvedRentals}
              prefix={<CheckCircleOutlined style={{ color: '#52c41a' }} />}
              valueStyle={{ color: '#52c41a' }}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card>
            <Statistic
              title="已出租"
              value={overview?.rentedRentals}
              prefix={<HomeOutlined style={{ color: '#1890ff' }} />}
              valueStyle={{ color: '#1890ff' }}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card>
            <Statistic
              title="待处理投诉"
              value={overview?.pendingComplaints}
              prefix={<WarningOutlined style={{ color: '#ff4d4f' }} />}
              valueStyle={{ color: '#ff4d4f' }}
            />
          </Card>
        </Col>
      </Row>

      <Row gutter={[16, 16]} style={{ marginTop: 24 }}>
        <Col xs={24} lg={12}>
          <Card title="近7天趋势">
            <ReactECharts option={trendOption} style={{ height: 300 }} />
          </Card>
        </Col>
        <Col xs={24} lg={6}>
          <Card title="信息类型分布">
            <ReactECharts option={typeOption} style={{ height: 300 }} />
          </Card>
        </Col>
        <Col xs={24} lg={6}>
          <Card title="信息状态分布">
            <ReactECharts option={statusOption} style={{ height: 300 }} />
          </Card>
        </Col>
      </Row>
    </div>
  )
}
