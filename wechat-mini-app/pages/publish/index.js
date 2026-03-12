const { request, uploadFile } = require('../../utils/request');

const PHONE_PATTERN = /^[0-9\-+\s]{6,20}$/;

function chooseImageFiles(count) {
  return new Promise((resolve, reject) => {
    wx.chooseImage({
      count,
      sizeType: ['compressed'],
      sourceType: ['album', 'camera'],
      success: resolve,
      fail: reject
    });
  });
}

function trimForm(form) {
  return {
    ...form,
    title: (form.title || '').trim(),
    description: (form.description || '').trim(),
    price: (form.price || '').trim(),
    contactName: (form.contactName || '').trim(),
    contactPhone: (form.contactPhone || '').trim(),
    communityName: (form.communityName || '').trim()
  };
}

function validateForm(form) {
  if (!form.title) {
    return '请填写标题';
  }
  if (form.title.length < 4) {
    return '标题至少 4 个字';
  }
  if (!form.description) {
    return '请填写描述';
  }
  if (form.description.length < 10) {
    return '描述至少 10 个字';
  }
  if (!form.price) {
    return '请填写价格';
  }
  if (!Number(form.price) || Number(form.price) <= 0) {
    return '价格必须大于 0';
  }
  if (!form.contactName) {
    return '请填写联系人';
  }
  if (!form.contactPhone) {
    return '请填写联系电话';
  }
  if (!PHONE_PATTERN.test(form.contactPhone)) {
    return '联系电话格式不正确';
  }
  return '';
}

Page({
  data: {
    typeOptions: [
      { value: 'HOUSE', label: '房屋' },
      { value: 'PARKING', label: '车位' },
      { value: 'ITEM', label: '闲置物品' }
    ],
    typeIndex: 0,
    uploading: false,
    submitting: false,
    form: {
      title: '',
      description: '',
      price: '',
      contactName: '',
      contactPhone: '',
      communityName: '',
      imageUrls: []
    }
  },
  onShow() {
    const user = getApp().getCurrentUser();
    if (!user || !user.userId) {
      wx.showToast({ title: '请先登录后发布', icon: 'none' });
      wx.navigateTo({ url: '/pages/login/index' });
      return;
    }
    if (!this.data.form.contactName && user.nickname) {
      this.setData({
        'form.contactName': user.nickname
      });
    }
  },
  bindField(e) {
    const field = e.currentTarget.dataset.field;
    this.setData({
      [`form.${field}`]: e.detail.value
    });
  },
  onTypeChange(e) {
    this.setData({ typeIndex: Number(e.detail.value) });
  },
  async chooseImages() {
    const currentCount = this.data.form.imageUrls.length;
    const remain = 6 - currentCount;
    if (remain <= 0) {
      wx.showToast({ title: '最多上传6张图', icon: 'none' });
      return;
    }
    try {
      const result = await chooseImageFiles(remain);
      this.setData({ uploading: true });
      const uploadedUrls = [];
      for (const filePath of result.tempFilePaths || []) {
        const uploaded = await uploadFile({
          url: '/api/files/images',
          filePath
        });
        uploadedUrls.push(uploaded.url);
      }
      this.setData({
        'form.imageUrls': this.data.form.imageUrls.concat(uploadedUrls)
      });
      wx.showToast({ title: '上传完成', icon: 'success' });
    } catch (err) {
      if (err && String(err).includes('cancel')) {
        return;
      }
      wx.showToast({ title: String(err), icon: 'none' });
    } finally {
      this.setData({ uploading: false });
    }
  },
  removeImage(e) {
    const index = Number(e.currentTarget.dataset.index);
    const imageUrls = this.data.form.imageUrls.filter((_, currentIndex) => currentIndex !== index);
    this.setData({
      'form.imageUrls': imageUrls
    });
  },
  previewImage(e) {
    const current = e.currentTarget.dataset.url;
    wx.previewImage({
      current,
      urls: this.data.form.imageUrls
    });
  },
  async submit() {
    const user = getApp().globalData.user || wx.getStorageSync('user');
    if (!user || !user.userId) {
      wx.showToast({ title: '请先登录', icon: 'none' });
      return;
    }
    if (this.data.submitting) {
      return;
    }
    const normalizedForm = trimForm(this.data.form);
    const validationMessage = validateForm(normalizedForm);
    if (validationMessage) {
      wx.showToast({ title: validationMessage, icon: 'none' });
      return;
    }
    try {
      this.setData({
        submitting: true,
        form: {
          ...this.data.form,
          ...normalizedForm
        }
      });
      await request({
        url: '/api/rentals',
        method: 'POST',
        data: {
          publisherUserId: user.userId,
          rentalType: this.data.typeOptions[this.data.typeIndex].value,
          title: normalizedForm.title,
          description: normalizedForm.description,
          price: Number(normalizedForm.price),
          contactName: normalizedForm.contactName,
          contactPhone: normalizedForm.contactPhone,
          communityName: normalizedForm.communityName,
          imageUrls: this.data.form.imageUrls
        }
      });
      wx.showToast({ title: '提交成功', icon: 'success' });
      this.setData({
        submitting: false,
        form: {
          title: '',
          description: '',
          price: '',
          contactName: user.nickname || '',
          contactPhone: '',
          communityName: '',
          imageUrls: []
        }
      });
      setTimeout(() => {
        wx.switchTab({ url: '/pages/mine/index' });
      }, 500);
    } catch (err) {
      this.setData({ submitting: false });
      wx.showToast({ title: String(err), icon: 'none' });
      return;
    }
    this.setData({ submitting: false });
  }
});
