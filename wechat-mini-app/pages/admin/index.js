const { request } = require('../../utils/request');

const TYPE_LABELS = {
  HOUSE: '房屋',
  PARKING: '车位',
  ITEM: '闲置物品'
};

function isExpired(expiresAt) {
  return !!expiresAt && new Date(expiresAt).getTime() <= Date.now();
}

Page({
  data: {
    username: 'admin',
    password: 'admin123',
    adminToken: '',
    expiresAt: '',
    pendingRentals: []
  },
  onShow() {
    const app = getApp();
    const currentUser = app.getCurrentUser();
    if (!app.isReviewerUser(currentUser)) {
      app.clearAdminSession();
      this.setData({ adminToken: '', expiresAt: '', pendingRentals: [] });
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
      this.setData({ adminToken: '', expiresAt: '', pendingRentals: [] });
      wx.showToast({ title: '管理员会话已过期', icon: 'none' });
      return;
    }
    if (adminSession && adminSession.adminToken) {
      this.setData({
        adminToken: adminSession.adminToken,
        expiresAt: adminSession.expiresAt || ''
      });
      this.loadPending();
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
      this.loadPending();
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
        pendingRentals: []
      });
      wx.showToast({ title: '已退出登录', icon: 'success' });
    }
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
        this.setData({ adminToken: '', expiresAt: '', pendingRentals: [] });
      }
      wx.showToast({ title: String(err), icon: 'none' });
    }
  },
  goDetail(e) {
    const id = e.currentTarget.dataset.id;
    wx.navigateTo({ url: `/pages/rental-detail/index?id=${id}&source=admin` });
  },
  async review(e) {
    const id = e.currentTarget.dataset.id;
    const approved = e.currentTarget.dataset.approved;
    const reason = approved ? '' : '信息不完整，请补充后重新提交';
    try {
      await request({
        url: `/api/admin/rentals/${id}/review`,
        method: 'POST',
        adminToken: this.data.adminToken,
        data: {
          action: approved ? 'APPROVE' : 'REJECT',
          approved,
          reason
        }
      });
      wx.showToast({ title: '处理完成', icon: 'success' });
      this.loadPending();
    } catch (err) {
      if (String(err).includes('expired') || String(err).includes('invalid')) {
        getApp().clearAdminSession();
        this.setData({ adminToken: '', expiresAt: '', pendingRentals: [] });
      }
      wx.showToast({ title: String(err), icon: 'none' });
    }
  }
});
