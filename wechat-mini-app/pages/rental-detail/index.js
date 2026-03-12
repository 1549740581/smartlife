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
  OFFLINE: '已下架'
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
    loading: false
  },
  onLoad(options) {
    this.setData({
      rentalId: Number(options.id),
      source: options.source || 'public'
    });
  },
  onShow() {
    this.loadDetail();
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
          priceText: rental.price ? `¥${rental.price}` : '价格面议'
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
  }
});
