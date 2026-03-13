const { request } = require('../../utils/request');
const { buildPickerRange, buildNextPickerState, findPickerValue, resolveSelection } = require('../../utils/address');

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
    priceText: item.price ? `¥${item.price}` : '价格面议',
    locationText: [item.city, item.district, item.street, item.communityName].filter(Boolean).join(' / ')
  }));
}

Page({
  data: {
    activeType: 'ALL',
    keyword: '',
    rentals: [],
    favoriteIds: [],
    roleLabel: '访客',
    currentUser: null,
    identityText: '访客',
    addressTree: [],
    addressRange: [[], [], [], []],
    addressValue: [0, 0, 0, 0],
    selectedAddressText: '全部区域',
    hasAddressFilter: false,
    city: '',
    district: '',
    street: '',
    communityName: ''
  },
  onLoad() {
    this.loadAddressTree();
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
    if (this.data.addressTree.length) {
      this.loadRentals();
    }
  },
  async loadAddressTree() {
    try {
      const tree = await request({ url: '/api/addresses/tree' });
      const nextValue = findPickerValue(tree || [], {
        city: this.data.city,
        district: this.data.district,
        street: this.data.street,
        communityName: this.data.communityName
      });
      const pickerState = buildPickerRange(tree || [], nextValue);
      const selection = resolveSelection(tree || [], pickerState.value);
      this.setData({
        addressTree: tree || [],
        addressRange: pickerState.range,
        addressValue: pickerState.value,
        selectedAddressText: selection.text || '全部区域',
        hasAddressFilter: !!selection.communityName,
        city: selection.city || '',
        district: selection.district || '',
        street: selection.street || '',
        communityName: selection.communityName || ''
      }, () => this.loadRentals());
    } catch (err) {
      wx.showToast({ title: String(err), icon: 'none' });
      this.loadRentals();
    }
  },
  async loadRentals() {
    try {
      const requestData = {};
      const keyword = (this.data.keyword || '').trim();
      if (this.data.activeType !== 'ALL') {
        requestData.type = this.data.activeType;
      }
      if (keyword) {
        requestData.keyword = keyword;
      }
      if (this.data.hasAddressFilter) {
        requestData.city = this.data.city;
        requestData.district = this.data.district;
        requestData.street = this.data.street;
        requestData.communityName = this.data.communityName;
      }
      const rentals = await request({
        url: '/api/rentals',
        data: requestData
      });
      const formattedRentals = formatRentals(rentals);
      this.setData({ rentals: formattedRentals });
      this.loadFavoriteStatus(formattedRentals.map(r => r.id));
    } catch (err) {
      wx.showToast({ title: String(err), icon: 'none' });
    }
  },
  async loadFavoriteStatus(rentalIds) {
    const currentUser = getApp().getCurrentUser();
    if (!currentUser || !currentUser.userId || !rentalIds.length) {
      this.setData({ favoriteIds: [] });
      const rentals = (this.data.rentals || []).map(r => ({ ...r, isFavorited: false }));
      this.setData({ rentals });
      return;
    }
    try {
      const favoriteIds = await request({
        url: '/api/favorites/filter-ids',
        method: 'POST',
        data: {
          userId: currentUser.userId,
          rentalInfoIds: rentalIds
        }
      });
      const idSet = new Set((favoriteIds || []).map(Number));
      const rentals = (this.data.rentals || []).map(r => ({
        ...r,
        isFavorited: idSet.has(Number(r.id))
      }));
      this.setData({ favoriteIds: Array.from(idSet), rentals });
    } catch (err) {
      console.error('Load favorite status failed:', err);
    }
  },
  onKeywordInput(e) {
    this.setData({ keyword: e.detail.value });
  },
  onKeywordConfirm() {
    this.loadRentals();
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
      selectedAddressText: selection.text || '全部区域',
      hasAddressFilter: !!selection.communityName,
      city: selection.city || '',
      district: selection.district || '',
      street: selection.street || '',
      communityName: selection.communityName || ''
    }, () => this.loadRentals());
  },
  search() {
    this.loadRentals();
  },
  clearFilters() {
    const selection = resolveSelection(this.data.addressTree, [0, 0, 0, 0]);
    this.setData({
      keyword: '',
      addressValue: selection.value,
      selectedAddressText: selection.text || '全部区域',
      hasAddressFilter: !!selection.communityName,
      city: selection.city || '',
      district: selection.district || '',
      street: selection.street || '',
      communityName: selection.communityName || ''
    }, () => this.loadRentals());
  },
  switchType(e) {
    this.setData({ activeType: e.currentTarget.dataset.type }, () => this.loadRentals());
  },
  goDetail(e) {
    const { id } = e.currentTarget.dataset;
    wx.navigateTo({ url: `/pages/rental-detail/index?id=${id}&source=public` });
  }
});
