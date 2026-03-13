const { request } = require('../../utils/request');

const ORDER_STATUS_LABELS = {
  PENDING_CONFIRMATION: '待房东确认',
  ACTIVE: '进行中',
  CANCEL_PENDING: '待取消确认',
  CANCELED: '已取消',
  COMPLETED: '已完成'
};

const RENTAL_STATUS_LABELS = {
  APPROVED: '已通过',
  PENDING: '待审核',
  REJECTED: '已驳回',
  OFFLINE: '已下架',
  RENTED: '已出租'
};

function buildDate(offsetDays) {
  const value = new Date();
  value.setDate(value.getDate() + offsetDays);
  return formatDate(value);
}

function formatDate(value) {
  const year = value.getFullYear();
  const month = `${value.getMonth() + 1}`.padStart(2, '0');
  const day = `${value.getDate()}`.padStart(2, '0');
  return `${year}-${month}-${day}`;
}

function plusDays(dateText, offsetDays) {
  const value = new Date(dateText);
  value.setDate(value.getDate() + offsetDays);
  return formatDate(value);
}

function formatOrder(order, currentUserId) {
  const isLandlord = Number(currentUserId) === Number(order.landlordUserId);
  const isTenant = Number(currentUserId) === Number(order.tenantUserId);
  return {
    ...order,
    statusText: ORDER_STATUS_LABELS[order.status] || order.status,
    periodText: order.startDate && order.endDate ? `${order.startDate} 至 ${order.endDate}` : '待确认',
    canAccept: isLandlord && order.status === 'PENDING_CONFIRMATION',
    canRequestCancel: (isLandlord || isTenant) && ['PENDING_CONFIRMATION', 'ACTIVE'].includes(order.status),
    canConfirmCancel:
      order.status === 'CANCEL_PENDING' &&
      ((isLandlord && !order.landlordCancelConfirmed) || (isTenant && !order.tenantCancelConfirmed)),
    canRenew: ['ACTIVE', 'COMPLETED'].includes(order.status)
  };
}

function formatMessage(message, currentUserId) {
  return {
    ...message,
    isMine: Number(message.senderUserId) === Number(currentUserId),
    isSystem: message.messageType === 'SYSTEM',
    orderStatusText: message.order ? (ORDER_STATUS_LABELS[message.order.status] || message.order.status) : '',
    orderPeriodText: message.order && message.order.startDate && message.order.endDate
      ? `${message.order.startDate} 至 ${message.order.endDate}`
      : ''
  };
}

function formatDetail(detail, currentUserId) {
  const rental = detail.rental || {};
  return {
    ...detail,
    rental: {
      ...rental,
      typeLabel: rental.rentalType === 'HOUSE' ? '房屋' : (rental.rentalType === 'PARKING' ? '车位' : '闲置物品'),
      statusText: RENTAL_STATUS_LABELS[rental.status] || rental.status,
      priceText: rental.price ? `¥${rental.price}` : '价格面议',
      locationText: [rental.city, rental.district, rental.street, rental.communityName].filter(Boolean).join(' / ')
    },
    orders: (detail.orders || []).map((item) => formatOrder(item, currentUserId)),
    messages: (detail.messages || []).map((item) => formatMessage(item, currentUserId))
  };
}

Page({
  data: {
    conversationId: 0,
    detail: null,
    loading: false,
    draftContent: '',
    startDate: buildDate(1),
    endDate: buildDate(30),
    renewBaseOrderId: 0,
    sendingMessage: false,
    sendingOrder: false,
    handlingOrderId: 0
  },
  onLoad(options) {
    this.setData({
      conversationId: Number(options.id || 0)
    });
  },
  onShow() {
    const user = getApp().getCurrentUser();
    if (!user || !user.userId) {
      wx.navigateTo({ url: '/pages/login/index' });
      return;
    }
    this.loadDetail(user.userId);
  },
  async loadDetail(userId) {
    try {
      this.setData({ loading: true });
      const detail = await request({
        url: `/api/rental-conversations/${this.data.conversationId}`,
        data: { userId }
      });
      this.setData({
        detail: formatDetail(detail, userId)
      });
      this.markAsRead(userId);
    } catch (err) {
      wx.showToast({ title: String(err), icon: 'none' });
    } finally {
      this.setData({ loading: false });
    }
  },
  async markAsRead(userId) {
    try {
      await request({
        url: '/api/rental-conversations/mark-read',
        method: 'POST',
        data: {
          conversationId: this.data.conversationId,
          userId: userId
        }
      });
    } catch (err) {
      console.error('Mark read failed:', err);
    }
  },
  onDraftInput(e) {
    this.setData({ draftContent: e.detail.value });
  },
  onStartDateChange(e) {
    this.setData({ startDate: e.detail.value });
  },
  onEndDateChange(e) {
    this.setData({ endDate: e.detail.value });
  },
  clearOrderForm() {
    this.setData({
      renewBaseOrderId: 0,
      startDate: buildDate(1),
      endDate: buildDate(30)
    });
  },
  useRenewOrder(e) {
    const order = ((this.data.detail && this.data.detail.orders) || [])
      .find((item) => Number(item.id) === Number(e.currentTarget.dataset.orderId));
    if (!order) {
      return;
    }
    this.setData({
      renewBaseOrderId: order.id,
      startDate: order.endDate ? plusDays(order.endDate, 1) : buildDate(1),
      endDate: order.endDate ? plusDays(order.endDate, 30) : buildDate(30)
    });
    wx.showToast({ title: '已切换为续约申请', icon: 'none' });
  },
  async sendMessage() {
    const user = getApp().getCurrentUser();
    const content = (this.data.draftContent || '').trim();
    if (!content) {
      wx.showToast({ title: '请输入消息内容', icon: 'none' });
      return;
    }
    try {
      this.setData({ sendingMessage: true });
      await request({
        url: `/api/rental-conversations/${this.data.conversationId}/messages`,
        method: 'POST',
        data: {
          userId: user.userId,
          content
        }
      });
      this.setData({ draftContent: '' });
      this.loadDetail(user.userId);
    } catch (err) {
      wx.showToast({ title: String(err), icon: 'none' });
    } finally {
      this.setData({ sendingMessage: false });
    }
  },
  async sendOrderCard() {
    const user = getApp().getCurrentUser();
    if (!this.data.startDate || !this.data.endDate) {
      wx.showToast({ title: '请选择租期', icon: 'none' });
      return;
    }
    try {
      this.setData({ sendingOrder: true });
      if (this.data.renewBaseOrderId) {
        await request({
          url: `/api/rental-orders/${this.data.renewBaseOrderId}/renew`,
          method: 'POST',
          data: {
            userId: user.userId,
            startDate: this.data.startDate,
            endDate: this.data.endDate
          }
        });
      } else {
        await request({
          url: `/api/rental-conversations/${this.data.conversationId}/orders`,
          method: 'POST',
          data: {
            userId: user.userId,
            startDate: this.data.startDate,
            endDate: this.data.endDate
          }
        });
      }
      this.clearOrderForm();
      this.loadDetail(user.userId);
      wx.showToast({ title: '租期卡片已发送', icon: 'success' });
    } catch (err) {
      wx.showToast({ title: String(err), icon: 'none' });
    } finally {
      this.setData({ sendingOrder: false });
    }
  },
  async acceptOrder(e) {
    await this.handleOrderAction(e.currentTarget.dataset.orderId, '/accept');
  },
  async requestCancel(e) {
    await this.handleOrderAction(e.currentTarget.dataset.orderId, '/cancel/request');
  },
  async confirmCancel(e) {
    await this.handleOrderAction(e.currentTarget.dataset.orderId, '/cancel/confirm');
  },
  async handleOrderAction(orderId, suffix) {
    const user = getApp().getCurrentUser();
    try {
      this.setData({ handlingOrderId: Number(orderId) });
      await request({
        url: `/api/rental-orders/${orderId}${suffix}`,
        method: 'POST',
        data: {
          userId: user.userId,
          reason: ''
        }
      });
      this.loadDetail(user.userId);
      wx.showToast({ title: '操作完成', icon: 'success' });
    } catch (err) {
      wx.showToast({ title: String(err), icon: 'none' });
    } finally {
      this.setData({ handlingOrderId: 0 });
    }
  }
});
