const { request } = require('../../utils/request');

const TYPE_LABELS = {
  HOUSE: '房屋',
  PARKING: '车位',
  ITEM: '闲置物品'
};

function isExpired(expiresAt) {
  return !!expiresAt && new Date(expiresAt).getTime() <= Date.now();
}

const ORDER_STATUS_LABELS = {
  PENDING_CONFIRMATION: '待房东确认',
  ACTIVE: '进行中',
  CANCEL_PENDING: '待取消确认',
  CANCELED: '已取消',
  COMPLETED: '已完成'
};

Page({
  data: {
    username: 'admin',
    password: 'admin123',
    adminToken: '',
    expiresAt: '',
    pendingRentals: [],
    orderList: [],
    activeRejectId: null,
    rejectReasons: {},
    reviewingId: null,
    cancelingOrderId: null
  },
  onShow() {
    const app = getApp();
    const currentUser = app.getCurrentUser();
    if (!app.isReviewerUser(currentUser)) {
      app.clearAdminSession();
      this.setData({ adminToken: '', expiresAt: '', pendingRentals: [], orderList: [] });
      wx.showToast({ title: '仅指定审核员可进入', icon: 'none' });
      wx.navigateBack({
        fail() {
          wx.switchTab({ url: '/pages/home/index' });
        }
      });
      return;
    }
    const adminSession = app.globalData.adminSession || wx.getStorageSync('adminSession');
    if (adminSession && isExpired(adminSession.expiresAt)) {
      app.clearAdminSession();
      this.setData({ adminToken: '', expiresAt: '', pendingRentals: [], orderList: [] });
      wx.showToast({ title: '管理员会话已过期', icon: 'none' });
      return;
    }
    if (adminSession && adminSession.adminToken) {
      this.setData({
        adminToken: adminSession.adminToken,
        expiresAt: adminSession.expiresAt || ''
      });
      this.loadDashboard();
    }
  },
  onUsernameInput(e) {
    this.setData({ username: e.detail.value });
  },
  onPasswordInput(e) {
    this.setData({ password: e.detail.value });
  },
  async login() {
    try {
      const result = await request({
        url: '/api/admin/login',
        method: 'POST',
        data: {
          username: this.data.username,
          password: this.data.password
        }
      });
      getApp().setAdminSession(result);
      this.setData({
        adminToken: result.adminToken,
        expiresAt: result.expiresAt || ''
      });
      this.loadDashboard();
    } catch (err) {
      wx.showToast({ title: String(err), icon: 'none' });
    }
  },
  async logout() {
    try {
      if (this.data.adminToken) {
        await request({
          url: '/api/admin/logout',
          method: 'POST',
          adminToken: this.data.adminToken
        });
      }
    } catch (err) {
      // Ignore logout request failure and clear the local session anyway.
    } finally {
      getApp().clearAdminSession();
      this.setData({
        adminToken: '',
        expiresAt: '',
        pendingRentals: [],
        orderList: [],
        activeRejectId: null,
        rejectReasons: {},
        reviewingId: null,
        cancelingOrderId: null
      });
      wx.showToast({ title: '已退出登录', icon: 'success' });
    }
  },
  async loadDashboard() {
    await Promise.all([this.loadPending(), this.loadOrders()]);
  },
  async loadPending() {
    try {
      if (isExpired(this.data.expiresAt)) {
        this.logout();
        return;
      }
      const pendingRentals = await request({
        url: '/api/admin/rentals/pending',
        adminToken: this.data.adminToken
      });
      this.setData({
        pendingRentals: (pendingRentals || []).map((item) => ({
          ...item,
          typeLabel: TYPE_LABELS[item.rentalType] || item.rentalType,
          priceText: item.price ? `¥${item.price}` : '价格面议'
        }))
      });
    } catch (err) {
      if (String(err).includes('expired') || String(err).includes('invalid')) {
        getApp().clearAdminSession();
        this.setData({ adminToken: '', expiresAt: '', pendingRentals: [], orderList: [] });
      }
      wx.showToast({ title: String(err), icon: 'none' });
    }
  },
  async loadOrders() {
    try {
      if (isExpired(this.data.expiresAt)) {
        this.logout();
        return;
      }
      const orderList = await request({
        url: '/api/admin/orders',
        adminToken: this.data.adminToken
      });
      this.setData({
        orderList: (orderList || []).map((item) => ({
          ...item,
          statusLabel: ORDER_STATUS_LABELS[item.status] || item.status,
          typeLabel: TYPE_LABELS[item.rentalType] || item.rentalType,
          dateText: item.startDate && item.endDate ? `${item.startDate} 至 ${item.endDate}` : '待确认',
          canCancel: ['PENDING_CONFIRMATION', 'ACTIVE', 'CANCEL_PENDING'].includes(item.status)
        }))
      });
    } catch (err) {
      if (String(err).includes('expired') || String(err).includes('invalid')) {
        getApp().clearAdminSession();
        this.setData({ adminToken: '', expiresAt: '', pendingRentals: [], orderList: [] });
      }
      wx.showToast({ title: String(err), icon: 'none' });
    }
  },
  goDetail(e) {
    const id = e.currentTarget.dataset.id;
    wx.navigateTo({ url: `/pages/rental-detail/index?id=${id}&source=admin` });
  },
  async approveReview(e) {
    const id = e.currentTarget.dataset.id;
    try {
      this.setData({ reviewingId: id });
      await request({
        url: `/api/admin/rentals/${id}/review`,
        method: 'POST',
        adminToken: this.data.adminToken,
        data: {
          action: 'APPROVE',
          approved: true,
          reason: ''
        }
      });
      wx.showToast({ title: '处理完成', icon: 'success' });
      this.loadDashboard();
    } catch (err) {
      if (String(err).includes('expired') || String(err).includes('invalid')) {
        getApp().clearAdminSession();
        this.setData({ adminToken: '', expiresAt: '', pendingRentals: [], orderList: [] });
      }
      wx.showToast({ title: String(err), icon: 'none' });
    } finally {
      this.setData({ reviewingId: null });
    }
  },
  openRejectForm(e) {
    const id = e.currentTarget.dataset.id;
    this.setData({
      activeRejectId: id,
      rejectReasons: {
        ...this.data.rejectReasons,
        [id]: this.data.rejectReasons[id] || ''
      }
    });
  },
  onRejectReasonInput(e) {
    const id = Number(e.currentTarget.dataset.id);
    const nextReasons = {
      ...this.data.rejectReasons,
      [id]: e.detail.value
    };
    this.setData({ rejectReasons: nextReasons });
  },
  cancelReject() {
    this.setData({ activeRejectId: null });
  },
  async submitReject(e) {
    const id = e.currentTarget.dataset.id;
    const reason = (this.data.rejectReasons[id] || '').trim();
    if (!reason) {
      wx.showToast({ title: '请填写拒绝原因', icon: 'none' });
      return;
    }
    try {
      this.setData({ reviewingId: id });
      await request({
        url: `/api/admin/rentals/${id}/review`,
        method: 'POST',
        adminToken: this.data.adminToken,
        data: {
          action: 'REJECT',
          approved: false,
          reason
        }
      });
      const nextReasons = { ...this.data.rejectReasons };
      delete nextReasons[id];
      this.setData({
        activeRejectId: null,
        rejectReasons: nextReasons
      });
      wx.showToast({ title: '已拒绝', icon: 'success' });
      this.loadDashboard();
    } catch (err) {
      if (String(err).includes('expired') || String(err).includes('invalid')) {
        getApp().clearAdminSession();
        this.setData({ adminToken: '', expiresAt: '', pendingRentals: [], orderList: [] });
      }
      wx.showToast({ title: String(err), icon: 'none' });
    } finally {
      this.setData({ reviewingId: null });
    }
  },
  async cancelOrder(e) {
    const id = Number(e.currentTarget.dataset.id);
    try {
      this.setData({ cancelingOrderId: id });
      await request({
        url: `/api/admin/orders/${id}/cancel`,
        method: 'POST',
        adminToken: this.data.adminToken,
        data: {
          reason: '管理员取消订单'
        }
      });
      wx.showToast({ title: '订单已取消', icon: 'success' });
      this.loadOrders();
    } catch (err) {
      if (String(err).includes('expired') || String(err).includes('invalid')) {
        getApp().clearAdminSession();
        this.setData({ adminToken: '', expiresAt: '', pendingRentals: [], orderList: [] });
      }
      wx.showToast({ title: String(err), icon: 'none' });
    } finally {
      this.setData({ cancelingOrderId: null });
    }
  }
});
