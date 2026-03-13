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

function buildStatusText(rental) {
  if (!rental || !rental.status) {
    return '';
  }
  return STATUS_LABELS[rental.status] || rental.status;
}

function clearAdminSessionIfUnauthorized(err) {
  const message = String(err);
  if (message.includes('expired') || message.includes('invalid')) {
    getApp().clearAdminSession();
  }
}

Page({
  data: {
    rentalId: null,
    source: 'public',
    rental: null,
    loading: false,
    openingConversation: false,
    isFavorite: false,
    favoriteLoading: false
  },
  onLoad(options) {
    this.setData({
      rentalId: Number(options.id),
      source: options.source || 'public'
    });
  },
  onShow() {
    this.loadDetail();
    this.checkFavorite();
  },
  async loadDetail() {
    if (!this.data.rentalId) {
      return;
    }
    this.setData({ loading: true });
    try {
      const rental = await this.fetchDetail();
      this.setData({
        rental: {
          ...rental,
          statusClass: ((rental && rental.status) || '').toLowerCase(),
          statusText: buildStatusText(rental),
          typeLabel: TYPE_LABELS[rental.rentalType] || rental.rentalType,
          priceText: rental.price ? `¥${rental.price}` : '价格面议',
          locationText: [rental.city, rental.district, rental.street, rental.communityName].filter(Boolean).join(' / ')
        }
      });
    } catch (err) {
      clearAdminSessionIfUnauthorized(err);
      wx.showToast({ title: String(err), icon: 'none' });
    } finally {
      this.setData({ loading: false });
    }
  },
  fetchDetail() {
    const { rentalId, source } = this.data;
    if (source === 'admin') {
      const adminToken = getApp().globalData.adminToken || wx.getStorageSync('adminToken');
      return request({
        url: `/api/admin/rentals/${rentalId}`,
        adminToken
      });
    }
    if (source === 'mine') {
      const user = getApp().globalData.user || wx.getStorageSync('user');
      return request({
        url: `/api/rentals/user/${user.userId}/${rentalId}`
      });
    }
    return request({ url: `/api/rentals/${rentalId}` });
  },
  previewImage(e) {
    const current = e.currentTarget.dataset.url;
    const urls = (this.data.rental && this.data.rental.imageUrls) || [];
    if (!current) {
      return;
    }
    wx.previewImage({
      current,
      urls
    });
  },
  callContact() {
    const phone = this.data.rental && this.data.rental.contactPhone;
    if (!phone) {
      wx.showToast({ title: '暂无联系电话', icon: 'none' });
      return;
    }
    wx.makePhoneCall({ phoneNumber: phone });
  },
  async review(e) {
    const approved = e.currentTarget.dataset.approved;
    const rental = this.data.rental;
    if (!rental) {
      return;
    }
    const adminToken = getApp().globalData.adminToken || wx.getStorageSync('adminToken');
    try {
      await request({
        url: `/api/admin/rentals/${rental.id}/review`,
        method: 'POST',
        adminToken,
        data: {
          action: approved ? 'APPROVE' : 'REJECT',
          approved,
          reason: approved ? '' : '信息不完整，请补充后重新提交'
        }
      });
      wx.showToast({ title: '处理完成', icon: 'success' });
      this.loadDetail();
    } catch (err) {
      clearAdminSessionIfUnauthorized(err);
      wx.showToast({ title: String(err), icon: 'none' });
    }
  },
  async offline() {
    const rental = this.data.rental;
    if (!rental) {
      return;
    }
    const adminToken = getApp().globalData.adminToken || wx.getStorageSync('adminToken');
    try {
      await request({
        url: `/api/admin/rentals/${rental.id}/offline`,
        method: 'POST',
        adminToken,
        data: {
          action: 'OFFLINE',
          approved: false,
          reason: '管理员下架'
        }
      });
      wx.showToast({ title: '已下架', icon: 'success' });
      this.loadDetail();
    } catch (err) {
      clearAdminSessionIfUnauthorized(err);
      wx.showToast({ title: String(err), icon: 'none' });
    }
  },
  async rentNow() {
    const rental = this.data.rental;
    const currentUser = getApp().getCurrentUser();
    if (!currentUser || !currentUser.userId) {
      wx.navigateTo({ url: '/pages/login/index' });
      return;
    }
    if (!rental || this.data.openingConversation) {
      return;
    }
    if (Number(rental.publisherUserId) === Number(currentUser.userId)) {
      wx.showToast({ title: '不能给自己的房源发起租赁沟通', icon: 'none' });
      return;
    }
    if (rental.rentalType === 'ITEM') {
      wx.showToast({ title: '闲置物品暂不支持租赁订单', icon: 'none' });
      return;
    }
    try {
      this.setData({ openingConversation: true });
      const conversationId = await request({
        url: `/api/rentals/${rental.id}/conversation`,
        method: 'POST',
        data: {
          userId: currentUser.userId
        }
      });
      wx.navigateTo({ url: `/pages/conversation-detail/index?id=${conversationId}` });
    } catch (err) {
      wx.showToast({ title: String(err), icon: 'none' });
    } finally {
      this.setData({ openingConversation: false });
    }
  },
  goConversationList() {
    const rental = this.data.rental;
    const currentUser = getApp().getCurrentUser();
    if (!rental || !currentUser || !currentUser.userId) {
      wx.navigateTo({ url: '/pages/login/index' });
      return;
    }
    wx.navigateTo({
      url: `/pages/conversation-list/index?rentalId=${rental.id}&title=${encodeURIComponent(rental.title)}`
    });
  },
  reportComplaint() {
    const rental = this.data.rental;
    const currentUser = getApp().getCurrentUser();
    if (!currentUser || !currentUser.userId) {
      wx.navigateTo({ url: '/pages/login/index' });
      return;
    }
    if (!rental) {
      return;
    }
    if (Number(rental.publisherUserId) === Number(currentUser.userId)) {
      wx.showToast({ title: '不能投诉自己的信息', icon: 'none' });
      return;
    }
    wx.navigateTo({
      url: `/pages/complaint/index?rentalId=${rental.id}`
    });
  },
  async checkFavorite() {
    const currentUser = getApp().getCurrentUser();
    if (!currentUser || !currentUser.userId || !this.data.rentalId) {
      return;
    }
    try {
      const isFavorite = await request({
        url: '/api/favorites/check',
        method: 'POST',
        data: {
          userId: currentUser.userId,
          rentalInfoId: this.data.rentalId
        }
      });
      this.setData({ isFavorite });
    } catch (err) {
      console.error('Check favorite failed:', err);
    }
  },
  async toggleFavorite() {
    const currentUser = getApp().getCurrentUser();
    if (!currentUser || !currentUser.userId) {
      wx.navigateTo({ url: '/pages/login/index' });
      return;
    }
    if (this.data.favoriteLoading) {
      return;
    }
    this.setData({ favoriteLoading: true });
    try {
      const url = this.data.isFavorite ? '/api/favorites/remove' : '/api/favorites/add';
      await request({
        url,
        method: 'POST',
        data: {
          userId: currentUser.userId,
          rentalInfoId: this.data.rentalId
        }
      });
      this.setData({ isFavorite: !this.data.isFavorite });
      wx.showToast({
        title: this.data.isFavorite ? '已收藏' : '已取消收藏',
        icon: 'success'
      });
    } catch (err) {
      wx.showToast({ title: String(err), icon: 'none' });
    } finally {
      this.setData({ favoriteLoading: false });
    }
  }
});
