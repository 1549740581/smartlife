const { request } = require('../../utils/request');

const STATUS_LABELS = {
  PENDING: '待处理',
  ACCEPTED: '已通过',
  REJECTED: '已驳回'
};

function formatDate(dateStr) {
  if (!dateStr) return '';
  const d = new Date(dateStr);
  const year = d.getFullYear();
  const month = String(d.getMonth() + 1).padStart(2, '0');
  const day = String(d.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
}

Page({
  data: {
    complaints: [],
    loading: false
  },
  onShow() {
    this.loadComplaints();
  },
  async loadComplaints() {
    const currentUser = getApp().getCurrentUser();
    if (!currentUser || !currentUser.userId) {
      wx.navigateTo({ url: '/pages/login/index' });
      return;
    }
    this.setData({ loading: true });
    try {
      const complaints = await request({
        url: '/api/complaints/user',
        method: 'POST',
        data: { userId: currentUser.userId }
      });
      this.setData({
        complaints: (complaints || []).map(c => ({
          ...c,
          statusClass: (c.status || '').toLowerCase(),
          statusText: STATUS_LABELS[c.status] || c.status,
          createdAtText: formatDate(c.createdAt)
        }))
      });
    } catch (err) {
      wx.showToast({ title: String(err), icon: 'none' });
    } finally {
      this.setData({ loading: false });
    }
  },
  viewDetail(e) {
    const id = e.currentTarget.dataset.id;
    wx.navigateTo({ url: `/pages/complaint-detail/index?id=${id}` });
  }
});
