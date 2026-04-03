const { request, uploadFile } = require('../../utils/request');
const { buildPickerRange, buildNextPickerState, findPickerValue, resolveSelection } = require('../../utils/address');

const PHONE_PATTERN = /^[0-9\-+\s]{6,20}$/;
const DEFAULT_CITY = '杭州';

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
    contactPhone: (form.contactPhone || '').trim()
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
  if (!form.city || !form.district || !form.street || !form.communityName) {
    return '请选择完整的小区地址';
  }
  return '';
}

function validateHouseDetail(houseDetail, applianceOptions) {
  const floor = Number(houseDetail.floor);
  if (houseDetail.floor === '' || isNaN(floor) || floor < 0 || floor > 40) {
    return '楼层必须在 0~40 之间';
  }
  if (houseDetail.bedroomCount === '' || Number(houseDetail.bedroomCount) < 0) {
    return '请填写卧室数量';
  }
  if (houseDetail.livingRoomCount === '' || Number(houseDetail.livingRoomCount) < 0) {
    return '请填写客厅数量';
  }
  if (houseDetail.kitchenCount === '' || Number(houseDetail.kitchenCount) < 0) {
    return '请填写厨房数量';
  }
  if (houseDetail.bathroomCount === '' || Number(houseDetail.bathroomCount) < 0) {
    return '请填写卫生间数量';
  }
  const selectedAppliances = applianceOptions.filter(item => item.checked);
  if (selectedAppliances.length === 0) {
    return '请选择家电家具';
  }
  if (houseDetail.propertyFee === '' || Number(houseDetail.propertyFee) < 0) {
    return '请填写物业费';
  }
  if (houseDetail.waterFee === '' || isNaN(Number(houseDetail.waterFee))) {
    return '请填写水费';
  }
  if (houseDetail.electricityFee === '' || isNaN(Number(houseDetail.electricityFee))) {
    return '请填写电费';
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
    addressTree: [],
    addressRange: [[], [], [], []],
    addressValue: [0, 0, 0, 0],
    selectedAddressText: '请选择杭州的小区地址',
    orientationOptions: [
      { value: 'EAST', label: '东' },
      { value: 'SOUTH', label: '南' },
      { value: 'WEST', label: '西' },
      { value: 'NORTH', label: '北' },
      { value: 'SOUTHEAST', label: '东南' },
      { value: 'SOUTHWEST', label: '西南' },
      { value: 'NORTHEAST', label: '东北' },
      { value: 'NORTHWEST', label: '西北' }
    ],
    orientationIndex: 0,
    balconyOptions: [
      { value: true, label: '有' },
      { value: false, label: '无' }
    ],
    balconyIndex: 0,
    elevatorOptions: [
      { value: true, label: '有' },
      { value: false, label: '无' }
    ],
    elevatorIndex: 0,
    applianceOptions: [
      { value: 'REFRIGERATOR', label: '冰箱', checked: false },
      { value: 'TV', label: '电视', checked: false },
      { value: 'AIR_CONDITIONER', label: '空调', checked: false },
      { value: 'WASHING_MACHINE', label: '洗衣机', checked: false },
      { value: 'WARDROBE', label: '衣柜', checked: false },
      { value: 'NONE', label: '无', checked: false }
    ],
    form: {
      city: DEFAULT_CITY,
      district: '',
      street: '',
      communityName: '',
      title: '',
      description: '',
      price: '',
      contactName: '',
      contactPhone: '',
      imageUrls: []
    },
    houseDetail: {
      floor: '',
      bedroomCount: '',
      livingRoomCount: '',
      kitchenCount: '',
      bathroomCount: '',
      propertyFee: '',
      waterFee: '',
      electricityFee: '',
      extraInfo: ''
    },
    isEditMode: false,
    rentalId: null,
    canEdit: false,
    savingDraft: false
  },
  onLoad(options) {
    if (options.id) {
      this.setData({
        isEditMode: true,
        rentalId: Number(options.id)
      });
    }
    this.loadAddressTree();
  },
  onShow() {
    const user = getApp().getCurrentUser();
    if (!user || !user.userId) {
      wx.showToast({ title: '请先登录后发布', icon: 'none' });
      wx.navigateTo({ url: '/pages/login/index' });
      return;
    }
    if (this.data.isEditMode && this.data.rentalId) {
      this.loadRentalDetail();
    } else if (!this.data.form.contactName && user.nickname) {
      this.setData({
        'form.contactName': user.nickname
      });
    }
  },
  async loadRentalDetail() {
    const user = getApp().getCurrentUser();
    try {
      const rental = await request({
        url: `/api/rentals/user/${user.userId}/${this.data.rentalId}`
      });
      const canEdit = rental.status === 'DRAFT' || rental.status === 'REJECTED';
      if (!canEdit) {
        wx.showToast({ title: '该信息不可编辑', icon: 'none' });
        wx.navigateBack();
        return;
      }
      const typeIndex = this.data.typeOptions.findIndex(t => t.value === rental.rentalType);
      this.setData({
        canEdit: true,
        typeIndex: typeIndex >= 0 ? typeIndex : 0,
        form: {
          city: rental.city || DEFAULT_CITY,
          district: rental.district || '',
          street: rental.street || '',
          communityName: rental.communityName || '',
          title: rental.title || '',
          description: rental.description || '',
          price: rental.price ? String(rental.price) : '',
          contactName: rental.contactName || '',
          contactPhone: rental.contactPhone || '',
          imageUrls: rental.imageUrls || []
        }
      });
      if (rental.rentalType === 'HOUSE' && rental.houseDetail) {
        const hd = rental.houseDetail;
        const orientationIndex = this.data.orientationOptions.findIndex(o => o.value === hd.orientation);
        const balconyIndex = hd.hasBalcony ? 0 : 1;
        const elevatorIndex = hd.hasElevator ? 0 : 1;
        const applianceOptions = this.data.applianceOptions.map(opt => ({
          ...opt,
          checked: (hd.appliances || []).includes(opt.value)
        }));
        this.setData({
          orientationIndex: orientationIndex >= 0 ? orientationIndex : 0,
          balconyIndex,
          elevatorIndex,
          applianceOptions,
          houseDetail: {
            floor: hd.floor != null ? String(hd.floor) : '',
            bedroomCount: hd.bedroomCount != null ? String(hd.bedroomCount) : '',
            livingRoomCount: hd.livingRoomCount != null ? String(hd.livingRoomCount) : '',
            kitchenCount: hd.kitchenCount != null ? String(hd.kitchenCount) : '',
            bathroomCount: hd.bathroomCount != null ? String(hd.bathroomCount) : '',
            propertyFee: hd.propertyFee != null ? String(hd.propertyFee) : '',
            waterFee: hd.waterFee != null ? String(hd.waterFee) : '',
            electricityFee: hd.electricityFee != null ? String(hd.electricityFee) : '',
            extraInfo: hd.extraInfo || ''
          }
        });
      }
      wx.setNavigationBarTitle({ title: '编辑信息' });
    } catch (err) {
      wx.showToast({ title: String(err), icon: 'none' });
      wx.navigateBack();
    }
  },
  async loadAddressTree() {
    try {
      const tree = await request({ url: '/api/addresses/tree' });
      const nextValue = findPickerValue(tree || [], {
        city: this.data.form.city,
        district: this.data.form.district,
        street: this.data.form.street,
        communityName: this.data.form.communityName
      });
      const pickerState = buildPickerRange(tree || [], nextValue);
      const selection = resolveSelection(tree || [], pickerState.value);
      this.setData({
        addressTree: tree || [],
        addressRange: pickerState.range,
        addressValue: pickerState.value,
        selectedAddressText: selection.text || '请选择杭州的小区地址',
        'form.city': selection.city || DEFAULT_CITY,
        'form.district': selection.district || '',
        'form.street': selection.street || '',
        'form.communityName': selection.communityName || ''
      });
    } catch (err) {
      wx.showToast({ title: String(err), icon: 'none' });
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
  onAddressColumnChange(e) {
    const pickerState = buildNextPickerState(
      this.data.addressTree,
      this.data.addressValue,
      Number(e.detail.column),
      Number(e.detail.value)
    );
    this.setData({
      addressRange: pickerState.range,
      addressValue: pickerState.value
    });
  },
  onAddressChange(e) {
    const selection = resolveSelection(this.data.addressTree, e.detail.value);
    this.setData({
      addressValue: selection.value,
      selectedAddressText: selection.text || '请选择杭州的小区地址',
      'form.city': selection.city || DEFAULT_CITY,
      'form.district': selection.district || '',
      'form.street': selection.street || '',
      'form.communityName': selection.communityName || ''
    });
  },
  bindHouseField(e) {
    const field = e.currentTarget.dataset.field;
    this.setData({
      [`houseDetail.${field}`]: e.detail.value
    });
  },
  onOrientationChange(e) {
    this.setData({ orientationIndex: Number(e.detail.value) });
  },
  onBalconyChange(e) {
    this.setData({ balconyIndex: Number(e.detail.value) });
  },
  onElevatorChange(e) {
    this.setData({ elevatorIndex: Number(e.detail.value) });
  },
  toggleAppliance(e) {
    const value = e.currentTarget.dataset.value;
    const options = this.data.applianceOptions.map(item => {
      if (value === 'NONE') {
        return { ...item, checked: item.value === 'NONE' ? !item.checked : false };
      }
      if (item.value === 'NONE') {
        return { ...item, checked: false };
      }
      if (item.value === value) {
        return { ...item, checked: !item.checked };
      }
      return item;
    });
    this.setData({ applianceOptions: options });
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
  async saveDraft() {
    await this.doSubmit(true);
  },
  async submit() {
    await this.doSubmit(false);
  },
  async doSubmit(isDraft) {
    const user = getApp().globalData.user || wx.getStorageSync('user');
    if (!user || !user.userId) {
      wx.showToast({ title: '请先登录', icon: 'none' });
      return;
    }
    if (this.data.submitting || this.data.savingDraft) {
      return;
    }

    const normalizedForm = {
      ...this.data.form,
      ...trimForm(this.data.form)
    };
    const validationMessage = validateForm(normalizedForm);
    if (validationMessage) {
      wx.showToast({ title: validationMessage, icon: 'none' });
      return;
    }

    const rentalType = this.data.typeOptions[this.data.typeIndex].value;
    if (rentalType === 'HOUSE') {
      const houseValidation = validateHouseDetail(this.data.houseDetail, this.data.applianceOptions);
      if (houseValidation) {
        wx.showToast({ title: houseValidation, icon: 'none' });
        return;
      }
    }

    try {
      this.setData({
        submitting: !isDraft,
        savingDraft: isDraft,
        form: normalizedForm
      });

      const requestData = {
        userId: user.userId,
        publisherUserId: user.userId,
        rentalType: rentalType,
        title: normalizedForm.title,
        description: normalizedForm.description,
        price: Number(normalizedForm.price),
        contactName: normalizedForm.contactName,
        contactPhone: normalizedForm.contactPhone,
        city: normalizedForm.city,
        district: normalizedForm.district,
        street: normalizedForm.street,
        communityName: normalizedForm.communityName,
        imageUrls: normalizedForm.imageUrls,
        isDraft: isDraft
      };

      if (rentalType === 'HOUSE') {
        const selectedAppliances = this.data.applianceOptions
          .filter(item => item.checked)
          .map(item => item.value);
        requestData.houseDetail = {
          floor: Number(this.data.houseDetail.floor),
          bedroomCount: Number(this.data.houseDetail.bedroomCount),
          livingRoomCount: Number(this.data.houseDetail.livingRoomCount),
          kitchenCount: Number(this.data.houseDetail.kitchenCount),
          bathroomCount: Number(this.data.houseDetail.bathroomCount),
          orientation: this.data.orientationOptions[this.data.orientationIndex].value,
          hasBalcony: this.data.balconyOptions[this.data.balconyIndex].value,
          appliances: selectedAppliances,
          hasElevator: this.data.elevatorOptions[this.data.elevatorIndex].value,
          propertyFee: Number(this.data.houseDetail.propertyFee),
          waterFee: Number(this.data.houseDetail.waterFee),
          electricityFee: Number(this.data.houseDetail.electricityFee),
          extraInfo: this.data.houseDetail.extraInfo || null
        };
      }

      if (this.data.isEditMode && this.data.rentalId) {
        await request({
          url: `/api/rentals/${this.data.rentalId}`,
          method: 'PUT',
          data: requestData
        });
      } else {
        await request({
          url: '/api/rentals',
          method: 'POST',
          data: requestData
        });
      }

      const successMsg = isDraft ? '草稿已保存' : '提交成功';
      wx.showToast({ title: successMsg, icon: 'success' });

      this.setData({
        submitting: false,
        savingDraft: false
      });

      setTimeout(() => {
        if (this.data.isEditMode) {
          wx.navigateBack();
        } else {
          this.resetForm();
          wx.switchTab({ url: '/pages/mine/index' });
        }
      }, 500);
    } catch (err) {
      this.setData({ submitting: false, savingDraft: false });
      wx.showToast({ title: String(err), icon: 'none' });
    }
  },
  resetForm() {
    const user = getApp().getCurrentUser();
    const resetSelection = resolveSelection(this.data.addressTree, [0, 0, 0, 0]);
    this.setData({
      isEditMode: false,
      rentalId: null,
      canEdit: false,
      addressValue: resetSelection.value,
      selectedAddressText: resetSelection.text || '请选择杭州的小区地址',
      orientationIndex: 0,
      balconyIndex: 0,
      elevatorIndex: 0,
      applianceOptions: [
        { value: 'REFRIGERATOR', label: '冰箱', checked: false },
        { value: 'TV', label: '电视', checked: false },
        { value: 'AIR_CONDITIONER', label: '空调', checked: false },
        { value: 'WASHING_MACHINE', label: '洗衣机', checked: false },
        { value: 'WARDROBE', label: '衣柜', checked: false },
        { value: 'NONE', label: '无', checked: false }
      ],
      form: {
        city: resetSelection.city || DEFAULT_CITY,
        district: resetSelection.district || '',
        street: resetSelection.street || '',
        communityName: resetSelection.communityName || '',
        title: '',
        description: '',
        price: '',
        contactName: (user && user.nickname) || '',
        contactPhone: '',
        imageUrls: []
      },
      houseDetail: {
        floor: '',
        bedroomCount: '',
        livingRoomCount: '',
        kitchenCount: '',
        bathroomCount: '',
        propertyFee: '',
        waterFee: '',
        electricityFee: '',
        extraInfo: ''
      }
    });
  }
});
