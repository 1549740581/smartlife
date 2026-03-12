const { demoLoginCodes } = require('../../utils/config');
const { request } = require('../../utils/request');

const DEMO_NICKNAME_MAP = {
  'wx-reviewer-10001': '审核员小杨',
  'wx-reviewer-10002': '审核员小李',
  'wx-user-10003': '房东陈姐',
  'wx-user-10004': '车位王哥'
};

Page({
  data: {
    code: '',
    nickname: '',
    reviewerCodes: demoLoginCodes.reviewerCodes,
    normalCodes: demoLoginCodes.normalCodes
  },
  onCodeInput(e) {
    this.setData({ code: e.detail.value });
  },
  onNicknameInput(e) {
    this.setData({ nickname: e.detail.value });
  },
  async login() {
    const code = (this.data.code || '').trim();
    const nickname = (this.data.nickname || '').trim();
    if (!code) {
      wx.showToast({ title: '请输入 code', icon: 'none' });
      return;
    }
    try {
      const user = await request({
        url: '/api/wechat/login',
        method: 'POST',
        data: {
          code,
          nickname
        }
      });
      getApp().setUser(user);
      wx.switchTab({ url: '/pages/home/index' });
    } catch (err) {
      wx.showToast({ title: String(err), icon: 'none' });
    }
  },
  useDemoCode(e) {
    const code = e.currentTarget.dataset.code;
    this.setData({
      code,
      nickname: DEMO_NICKNAME_MAP[code] || this.data.nickname
    });
  }
});
