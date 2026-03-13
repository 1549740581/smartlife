const { request, uploadFile } = require('../../utils/request');

Page({
  data: {
    rentalId: null,
    rental: null,
    reason: '',
    evidenceFiles: [],
    submitting: false
  },
  onLoad(options) {
    this.setData({ rentalId: Number(options.rentalId) });
    this.loadRentalInfo();
  },
  async loadRentalInfo() {
    try {
      const rental = await request({ url: `/api/rentals/${this.data.rentalId}` });
      this.setData({ rental });
    } catch (err) {
      wx.showToast({ title: '加载失败', icon: 'none' });
    }
  },
  onReasonInput(e) {
    this.setData({ reason: e.detail.value });
  },
  chooseFile() {
    const that = this;
    wx.showActionSheet({
      itemList: ['拍摄/选择图片', '拍摄/选择视频'],
      success(res) {
        if (res.tapIndex === 0) {
          that.chooseImage();
        } else {
          that.chooseVideo();
        }
      }
    });
  },
  chooseImage() {
    const remaining = 9 - this.data.evidenceFiles.length;
    if (remaining <= 0) return;
    wx.chooseMedia({
      count: remaining,
      mediaType: ['image'],
      sourceType: ['album', 'camera'],
      success: (res) => {
        const newFiles = res.tempFiles.map(f => ({
          type: 'image',
          url: f.tempFilePath,
          path: f.tempFilePath
        }));
        this.setData({
          evidenceFiles: this.data.evidenceFiles.concat(newFiles)
        });
      }
    });
  },
  chooseVideo() {
    if (this.data.evidenceFiles.length >= 9) return;
    wx.chooseMedia({
      count: 1,
      mediaType: ['video'],
      sourceType: ['album', 'camera'],
      maxDuration: 60,
      success: (res) => {
        const file = res.tempFiles[0];
        this.setData({
          evidenceFiles: this.data.evidenceFiles.concat([{
            type: 'video',
            url: file.thumbTempFilePath || file.tempFilePath,
            path: file.tempFilePath
          }])
        });
      }
    });
  },
  removeFile(e) {
    const index = e.currentTarget.dataset.index;
    const files = this.data.evidenceFiles.slice();
    files.splice(index, 1);
    this.setData({ evidenceFiles: files });
  },
  async submit() {
    const { rentalId, reason, evidenceFiles } = this.data;
    const currentUser = getApp().getCurrentUser();
    if (!currentUser || !currentUser.userId) {
      wx.navigateTo({ url: '/pages/login/index' });
      return;
    }
    if (!reason.trim()) {
      wx.showToast({ title: '请填写投诉理由', icon: 'none' });
      return;
    }
    this.setData({ submitting: true });
    try {
      let evidenceUrls = [];
      if (evidenceFiles.length > 0) {
        wx.showLoading({ title: '上传证据中...' });
        for (const file of evidenceFiles) {
          try {
            const url = await uploadFile({
              url: '/api/files/upload',
              filePath: file.path
            });
            evidenceUrls.push(url);
          } catch (uploadErr) {
            console.error('Upload failed:', uploadErr);
          }
        }
        wx.hideLoading();
      }
      await request({
        url: '/api/complaints',
        method: 'POST',
        data: {
          complainantUserId: currentUser.userId,
          rentalInfoId: rentalId,
          reason: reason.trim(),
          evidenceUrls
        }
      });
      wx.showToast({ title: '投诉已提交', icon: 'success' });
      setTimeout(() => {
        wx.navigateBack();
      }, 1500);
    } catch (err) {
      wx.showToast({ title: String(err), icon: 'none' });
    } finally {
      this.setData({ submitting: false });
    }
  },
  cancel() {
    wx.navigateBack();
  }
});
