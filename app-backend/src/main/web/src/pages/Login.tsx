import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { Card, Button, message, Spin } from 'antd'
import { ReloadOutlined } from '@ant-design/icons'
import { useAuthStore } from '../stores/auth'
import request from '../utils/request'

export default function Login() {
  const [qrCodeUrl, setQrCodeUrl] = useState('')
  const [loading, setLoading] = useState(false)
  const [polling, setPolling] = useState(false)
  const [ticket, setTicket] = useState('')
  const navigate = useNavigate()
  const { setAuth, isAuthenticated } = useAuthStore()

  useEffect(() => {
    if (isAuthenticated) {
      navigate('/dashboard', { replace: true })
    }
  }, [isAuthenticated, navigate])

  const isDev = import.meta.env.DEV

  const devLogin = async () => {
    setLoading(true)
    try {
      const res: any = await request.post('/qrcode', {})
      await request.post('/qrcode/confirm', { ticket: res.ticket, adminId: 1 })
      const statusRes: any = await request.post('/qrcode/status', { ticket: res.ticket })
      if (statusRes.status === 'CONFIRMED') {
        setAuth(statusRes.token, { id: statusRes.adminId, displayName: statusRes.displayName })
        message.success('登录成功')
        navigate('/dashboard', { replace: true })
      }
    } catch {
      message.error('登录失败')
    } finally {
      setLoading(false)
    }
  }

  const fetchQrCode = async () => {
    setLoading(true)
    try {
      const res: any = await request.post('/qrcode', {})
      // 生成扫码确认页面的 URL
      let origin = window.location.origin
      const publicUrl = import.meta.env.VITE_PUBLIC_URL
      if (publicUrl) {
        origin = publicUrl
      }
      const confirmUrl = `${origin}/scan-confirm?ticket=${res.ticket}`
      const qrUrl = `https://api.qrserver.com/v1/create-qr-code/?size=200x200&data=${encodeURIComponent(confirmUrl)}`
      setQrCodeUrl(qrUrl)
      setTicket(res.ticket)
      setPolling(true)
    } catch {
      message.error('获取二维码失败')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    fetchQrCode()
  }, [])

  useEffect(() => {
    if (!polling || !ticket) return

    const interval = setInterval(async () => {
      try {
        const res: any = await request.post('/qrcode/status', { ticket })
        if (res.status === 'CONFIRMED') {
          setPolling(false)
          setAuth(res.token, { id: res.adminId, displayName: res.displayName })
          message.success('登录成功')
          navigate('/dashboard', { replace: true })
        } else if (res.status === 'EXPIRED') {
          setPolling(false)
          message.warning('二维码已过期，请刷新')
        }
      } catch {
        // 忽略轮询错误
      }
    }, 2000)

    return () => clearInterval(interval)
  }, [polling, ticket, setAuth, navigate])

  return (
    <div
      style={{
        minHeight: '100vh',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
      }}
    >
      <Card
        style={{ width: 400, textAlign: 'center' }}
        title={
          <div style={{ fontSize: 24, fontWeight: 600 }}>Smart Life 管理后台</div>
        }
      >
        <div style={{ marginBottom: 24 }}>
          <p style={{ color: '#666' }}>请使用微信扫码登录</p>
        </div>
        <div
          style={{
            width: 240,
            height: 240,
            margin: '0 auto 24px',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            border: '1px solid #f0f0f0',
            borderRadius: 8,
          }}
        >
          {loading ? (
            <Spin size="large" />
          ) : qrCodeUrl ? (
            <img
              src={qrCodeUrl}
              alt="微信登录二维码"
              style={{ width: '100%', height: '100%', objectFit: 'contain' }}
            />
          ) : (
            <div style={{ color: '#999' }}>二维码加载失败</div>
          )}
        </div>
        <Button
          icon={<ReloadOutlined />}
          onClick={fetchQrCode}
          loading={loading}
        >
          刷新二维码
        </Button>
        {isDev && (
          <Button
            type="primary"
            style={{ marginLeft: 8 }}
            onClick={devLogin}
            loading={loading}
          >
            开发模式登录
          </Button>
        )}
        <div style={{ marginTop: 16, color: '#999', fontSize: 12 }}>
          二维码有效期 5 分钟
        </div>
      </Card>
    </div>
  )
}
