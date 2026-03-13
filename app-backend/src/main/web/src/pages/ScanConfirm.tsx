import { useEffect, useState } from 'react'
import { useSearchParams } from 'react-router-dom'
import { Button, Card, Result, Spin, message } from 'antd'
import { CheckCircleOutlined, CloseCircleOutlined


} from '@ant-design/icons'
import request from '../utils/request'

export default function ScanConfirm() {
  const [searchParams] = useSearchParams()
  const ticket = searchParams.get('ticket')
  const [status, setStatus] = useState<'loading' | 'pending' | 'confirmed' | 'expired' | 'error'>('loading')
  const [confirming, setConfirming] = useState(false)

  useEffect(() => {
    if (!ticket) {
      setStatus('error')
      return
    }
    checkTicket()
  }, [ticket])

  const checkTicket = async () => {
    try {
      const res: any = await request.post('/qrcode/status', { ticket })
      if (res.status === 'PENDING' || res.status === 'SCANNED') {
        setStatus('pending')
      } else if (res.status === 'CONFIRMED') {
        setStatus('confirmed')
      } else {
        setStatus('expired')
      }
    } catch {
      setStatus('error')
    }
  }

  const handleConfirm = async () => {
    if (!ticket) return
    setConfirming(true)
    try {
      await request.post('/qrcode/confirm', { ticket, adminId: 1 })
      setStatus('confirmed')
      message.success('登录成功！请返回电脑端查看')
    } catch {
      message.error('确认失败，请重试')
    } finally {
      setConfirming(false)
    }
  }

  if (status === 'loading') {
    return (
      <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '100vh', background: '#f0f2f5' }}>
        <Spin size="large" />
      </div>
    )
  }

  if (status === 'error' || !ticket) {
    return (
      <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '100vh', background: '#f0f2f5' }}>
        <Card style={{ width: 320 }}>
          <Result
            status="error"
            title="无效的二维码"
            subTitle="请重新扫描二维码"
          />
        </Card>
      </div>
    )
  }

  if (status === 'expired') {
    return (
      <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '100vh', background: '#f0f2f5' }}>
        <Card style={{ width: 320 }}>
          <Result
            icon={<CloseCircleOutlined style={{ color: '#ff4d4f' }} />}
            title="二维码已过期"
            subTitle="请返回电脑端刷新二维码后重新扫描"
          />
        </Card>
      </div>
    )
  }

  if (status === 'confirmed') {
    return (
      <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '100vh', background: '#f0f2f5' }}>
        <Card style={{ width: 320 }}>
          <Result
            icon={<CheckCircleOutlined style={{ color: '#52c41a' }} />}
            title="登录成功"
            subTitle="请返回电脑端继续操作"
          />
        </Card>
      </div>
    )
  }

  return (
    <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '100vh', background: '#f0f2f5' }}>
      <Card style={{ width: 320, textAlign: 'center' }}>
        <h2 style={{ marginBottom: 24 }}>小区生活助手</h2>
        <p style={{ marginBottom: 24, color: '#666' }}>管理后台登录确认</p>
        <Button
          type="primary"
          size="large"
          block
          loading={confirming}
          onClick={handleConfirm}
        >
          确认登录
        </Button>
        <p style={{ marginTop: 16, fontSize: 12, color: '#999' }}>
          点击上方按钮确认登录到管理后台
        </p>
      </Card>
    </div>
  )
}
