const { request } = require('../../utils/request');

const ORDER_STATUS_LABELS = {
  PENDING_CONFIRMATION: '待房东确认',
  ACTIVE: '进行中',
  CANCEL_PENDING: '待取消确认',
  CANCELED: '已取消',
  COMPLETED: '已完成'
};

function formatConversation(item, userId) {
  const isLandlord = Number(userId) === Number(item.landlordUserId);
  const latestOrder = item.latestOrder || null;
  return {
    ...item,
    roleLabel: isLandlord ? '房东视角' : '租客视角',
    counterpartLabel: item.counterpartNickname || `用户${item.counterpartUserId || ''}`,
    lastMessagePreview: item.lastMessagePreview || '还没有消息，点进去开始沟通',
    orderStatusText: latestOrder ? (ORDER_STATUS_LABELS[latestOrder.status] || latestOrder.status) : '',
    orderDateText: latestOrder && latestOrder.startDate && latestOrder.endDate
      ? `${latestOrder.startDate} 至 ${latestOrder.endDate}`
      : '',
    rentRangeText: item.rentStartDate && item.rentEndDate ? `${item.rentStartDate} 至 ${item.rentEndDate}` : ''
  };
}

Page({
  data: {
    rentalId: 0,
    pageTitle: '租赁沟通',
    conversations: [],
    loading: false
  },
  onLoad(options) {
    this.setData({
      rentalId: Number(options.rentalId || 0),
      pageTitle: options.title ? decodeURIComponent(options.title) : '租赁沟通'
    });
  },
  onShow() {
    const user = getApp().getCurrentUser();
    if (!user || !user.userId) {
      wx.navigateTo({ url: '/pages/login/index' });
      return;
    }
    this.loadConversations(user.userId);
  },
  async loadConversations(userId) {
    try {
      this.setData({ loading: true });
      const conversations = await request({
        url: '/api/rental-conversations',
        data: { userId }
      });
      const filtered = (conversations || [])
        .filter((item) => !this.data.rentalId || Number(item.rentalInfoId) === Number(this.data.rentalId))
        .map((item) => formatConversation(item, userId));
      this.setData({ conversations: filtered });
    } catch (err) {
      wx.showToast({ title: String(err), icon: 'none' });
    } finally {
      this.setData({ loading: false });
    }
  },
  goDetail(e) {
    const { id } = e.currentTarget.dataset;
    wx.navigateTo({ url: `/pages/conversation-detail/index?id=${id}` });
  }
});
