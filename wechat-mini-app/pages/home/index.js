const { request } = require('../../utils/request');

const TYPE_LABELS = {
  ALL: '全部',
  HOUSE: '房屋',
  PARKING: '车位',
  ITEM: '闲置物品'
};

function formatRentals(rentals) {
  return (rentals || []).map((item) => ({
    ...item,
    typeLabel: TYPE_LABELS[item.rentalType] || item.rentalType,
    priceText: item.price ? `¥${item.price}` : '价格面议'
  }));
}

Page({
  data: {
    activeType: 'ALL',
    rentals: [],
    roleLabel: '访客',
    currentUser: null,
    identityText: '访客'
  },
  onShow() {
    const app = getApp();
    const currentUser = app.getCurrentUser();
    const roleLabel = app.getUserRoleLabel(currentUser);
    this.setData({
      currentUser,
      roleLabel,
      identityText: currentUser && currentUser.nickname ? `${roleLabel} · ${currentUser.nickname}` : roleLabel
    });
    this.loadRentals();
  },
  async loadRentals() {
    try {
      const url = this.data.activeType === 'ALL'
        ? '/api/rentals'
        : `/api/rentals/type/${this.data.activeType}`;
      const rentals = await request({ url });
      this.setData({ rentals: formatRentals(rentals) });
    } catch (err) {
      wx.showToast({ title: String(err), icon: 'none' });
    }
  },
  switchType(e) {
    this.setData({ activeType: e.currentTarget.dataset.type }, () => this.loadRentals());
  },
  goDetail(e) {
    const { id } = e.currentTarget.dataset;
    wx.navigateTo({ url: `/pages/rental-detail/index?id=${id}&source=public` });
  }
});
