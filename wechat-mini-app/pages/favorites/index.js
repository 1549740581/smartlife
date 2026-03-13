const { request } = require('../../utils/request');

const TYPE_LABELS = {
  HOUSE: '房屋',
  PARKING: '车位',
  ITEM: '闲置物品'
};

Page({
  data: {
    favorites: [],
    loading: true
  },
  onLoad() {
    this.loadFavorites();
  },
  onShow() {
    this.loadFavorites();
  },
  async loadFavorites() {
    const currentUser = getApp().getCurrentUser();
    if (!currentUser || !currentUser.userId) {
      this.setData({ loading: false, favorites: [] });
      return;
    }
    this.setData({ loading: true });
    try {
      const favorites = await request({
        url: '/api/favorites/list',
        method: 'POST',
        data: { userId: currentUser.userId }
      });
      this.setData({
        favorites: (favorites || []).map(item => ({
          ...item,
          typeLabel: TYPE_LABELS[item.rentalType] || item.rentalType,
          priceText: item.price ? `¥${item.price}` : '价格面议',
          locationText: [item.city, item.district, item.street, item.communityName].filter(Boolean).join(' / ') || '未填写地址'
        }))
      });
    } catch (err) {
      console.error('Load favorites failed:', err);
      wx.showToast({ title: '加载失败', icon: 'none' });
    } finally {
      this.setData({ loading: false });
    }
  },
  goDetail(e) {
    const id = e.currentTarget.dataset.id;
    wx.navigateTo({
      url: `/pages/rental-detail/index?id=${id}&source=public`
    });
  },
  async removeFavorite(e) {
    const id = e.currentTarget.dataset.id;
    const currentUser = getApp().getCurrentUser();
    if (!currentUser || !currentUser.userId) {
      return;
    }
    try {
      await request({
        url: '/api/favorites/remove',
        method: 'POST',
        data: {
          userId: currentUser.userId,
          rentalInfoId: id
        }
      });
      wx.showToast({ title: '已取消收藏', icon: 'success' });
      this.loadFavorites();
    } catch (err) {
      wx.showToast({ title: String(err), icon: 'none' });
    }
  }
});
