const { request } = require('../../utils/request');

function formatTime(dateStr) {
  if (!dateStr) return '';
  const date = new Date(dateStr);
  const now = new Date();
  const diff = now - date;
  const oneDay = 24 * 60 * 60 * 1000;
  
  if (diff < oneDay && date.getDate() === now.getDate()) {
    return `${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`;
  } else if (diff < 2 * oneDay) {
    return '昨天';
  } else if (diff < 7 * oneDay) {
    const days = ['周日', '周一', '周二', '周三', '周四', '周五', '周六'];
    return days[date.getDay()];
  } else {
    return `${date.getMonth() + 1}/${date.getDate()}`;
  }
}

Page({
  data: {
    conversations: [],
    loading: true
  },
  onLoad() {
    this.loadConversations();
  },
  onShow() {
    this.loadConversations();
    this.updateTabBarBadge();
  },
  async loadConversations() {
    const currentUser = getApp().getCurrentUser();
    if (!currentUser || !currentUser.userId) {
      this.setData({ loading: false, conversations: [] });
      return;
    }
    this.setData({ loading: true });
    try {
      const conversations = await request({
        url: '/api/rental-conversations/list',
        method: 'POST',
        data: { userId: currentUser.userId }
      });
      this.setData({
        conversations: (conversations || []).map(c => ({
          ...c,
          lastMessageAtText: formatTime(c.lastMessageAt)
        }))
      });
    } catch (err) {
      console.error('Load conversations failed:', err);
      wx.showToast({ title: '加载失败', icon: 'none' });
    } finally {
      this.setData({ loading: false });
    }
  },
  async updateTabBarBadge() {
    const currentUser = getApp().getCurrentUser();
    if (!currentUser || !currentUser.userId) {
      wx.removeTabBarBadge({ index: 1 });
      return;
    }
    try {
      const count = await request({
        url: '/api/messages/unread-count',
        method: 'POST',
        data: { userId: currentUser.userId }
      });
      if (count > 0) {
        wx.setTabBarBadge({
          index: 1,
          text: count > 99 ? '99+' : String(count)
        });
      } else {
        wx.removeTabBarBadge({ index: 1 });
      }
    } catch (err) {
      console.error('Update badge failed:', err);
    }
  },
  goConversation(e) {
    const { id } = e.currentTarget.dataset;
    wx.navigateTo({
      url: `/pages/conversation-detail/index?id=${id}`
    });
  }
});
