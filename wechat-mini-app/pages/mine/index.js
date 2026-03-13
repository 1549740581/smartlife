const { request } = require('../../utils/request');

const TYPE_LABELS = {
  HOUSE: '房屋',
  PARKING: '车位',
  ITEM: '闲置物品'
};

const STATUS_LABELS = {
  APPROVED: '已通过',
  PENDING: '待审核',
  REJECTED: '已驳回',
  OFFLINE: '已下架',
  RENTED: '已出租'
};

Page({
  data: {
    user: {},
    rentals: [],
    isGuest: true,
    roleLabel: '访客',
    canReview: false,
    avatarText: '访'
  },
  onShow() {
    const app = getApp();
    const user = app.getCurrentUser();
    const isGuest = !(user && user.userId);
    const avatarText = isGuest ? '访' : ((user && user.nickname ? user.nickname.charAt(0) : '用') || '用');
    this.setData({
      user: user || {},
      isGuest,
      roleLabel: app.getUserRoleLabel(user),
      canReview: app.isReviewerUser(user),
      avatarText
    });
    if (user && user.userId) {
      this.loadRentals(user.userId);
      return;
    }
    this.setData({ rentals: [] });
  },
  async loadRentals(userId) {
    try {
      const rentals = await request({ url: `/api/rentals/user/${userId}` });
      this.setData({
        rentals: (rentals || []).map((item) => ({
          ...item,
          statusClass: (item.status || '').toLowerCase(),
          typeLabel: TYPE_LABELS[item.rentalType] || item.rentalType,
          statusLabel: STATUS_LABELS[item.status] || item.status,
          priceText: item.price ? `¥${item.price}` : '价格面议',
          locationText: [item.city, item.district, item.street, item.communityName].filter(Boolean).join(' / ')
        }))
      });
    } catch (err) {
      wx.showToast({ title: String(err), icon: 'none' });
    }
  },
  goDetail(e) {
    const { id } = e.currentTarget.dataset;
    wx.navigateTo({ url: `/pages/rental-detail/index?id=${id}&source=mine` });
  },
  goPublish() {
    const user = getApp().getCurrentUser();
    if (!user || !user.userId) {
      wx.navigateTo({ url: '/pages/login/index' });
      return;
    }
    wx.navigateTo({ url: '/pages/publish/index' });
  },
  goConversations() {
    const user = getApp().getCurrentUser();
    if (!user || !user.userId) {
      wx.navigateTo({ url: '/pages/login/index' });
      return;
    }
    wx.navigateTo({ url: '/pages/conversation-list/index' });
  },
  goLogin() {
    wx.navigateTo({ url: '/pages/login/index' });
  },
  switchAccount() {
    getApp().clearUser();
    getApp().clearAdminSession();
    this.setData({
      user: {},
      rentals: [],
      isGuest: true,
      roleLabel: '访客',
      canReview: false,
      avatarText: '访'
    });
    wx.navigateTo({ url: '/pages/login/index' });
  },
  goAdmin() {
    wx.navigateTo({ url: '/pages/admin/index' });
  },
  goMyComplaints() {
    wx.navigateTo({ url: '/pages/my-complaints/index' });
  }
});
