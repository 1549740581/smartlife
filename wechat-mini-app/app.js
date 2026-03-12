const { reviewerUserIds } = require('./utils/config');

App({
  globalData: {
    user: null,
    adminToken: '',
    adminSession: null
  },
  onLaunch() {
    const user = wx.getStorageSync('user');
    const adminSession = wx.getStorageSync('adminSession');
    const adminToken = adminSession && adminSession.adminToken ? adminSession.adminToken : wx.getStorageSync('adminToken');
    if (user) {
      this.setUser(user);
    }
    if (adminSession && adminSession.adminToken) {
      this.globalData.adminSession = adminSession;
      this.globalData.adminToken = adminSession.adminToken;
    } else if (adminToken) {
      this.globalData.adminToken = adminToken;
    }
  },
  setUser(user) {
    this.globalData.user = user || null;
    if (user) {
      wx.setStorageSync('user', user);
      return;
    }
    this.clearUser();
  },
  clearUser() {
    this.globalData.user = null;
    wx.removeStorageSync('user');
  },
  getCurrentUser() {
    return this.globalData.user || wx.getStorageSync('user') || null;
  },
  isReviewerUser(user) {
    const currentUser = user || this.getCurrentUser();
    if (!currentUser || !currentUser.userId) {
      return false;
    }
    return reviewerUserIds.includes(Number(currentUser.userId));
  },
  getUserRoleLabel(user) {
    const currentUser = user || this.getCurrentUser();
    if (!currentUser || !currentUser.userId) {
      return '访客';
    }
    return this.isReviewerUser(currentUser) ? '审核员' : '普通用户';
  },
  setAdminSession(session) {
    this.globalData.adminSession = session;
    this.globalData.adminToken = session ? session.adminToken : '';
    if (session) {
      wx.setStorageSync('adminSession', session);
      wx.setStorageSync('adminToken', session.adminToken);
      return;
    }
    this.clearAdminSession();
  },
  clearAdminSession() {
    this.globalData.adminSession = null;
    this.globalData.adminToken = '';
    wx.removeStorageSync('adminSession');
    wx.removeStorageSync('adminToken');
  }
});
