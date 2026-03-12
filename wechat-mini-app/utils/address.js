function clampIndex(index, length) {
  if (!length) {
    return 0;
  }
  if (index < 0) {
    return 0;
  }
  if (index >= length) {
    return length - 1;
  }
  return index;
}

function normalizePickerValue(tree, pickerValue = [0, 0, 0, 0]) {
  const nextValue = [...pickerValue];
  nextValue[0] = clampIndex(nextValue[0] || 0, tree.length);

  const city = tree[nextValue[0]] || {};
  const districts = city.children || [];
  nextValue[1] = clampIndex(nextValue[1] || 0, districts.length);

  const district = districts[nextValue[1]] || {};
  const streets = district.children || [];
  nextValue[2] = clampIndex(nextValue[2] || 0, streets.length);

  const street = streets[nextValue[2]] || {};
  const communities = street.children || [];
  nextValue[3] = clampIndex(nextValue[3] || 0, communities.length);

  return nextValue;
}

function buildPickerRange(tree, pickerValue = [0, 0, 0, 0]) {
  const value = normalizePickerValue(tree, pickerValue);
  const city = tree[value[0]] || {};
  const districts = city.children || [];
  const district = districts[value[1]] || {};
  const streets = district.children || [];
  const street = streets[value[2]] || {};
  const communities = street.children || [];

  return {
    value,
    range: [
      tree.map((item) => item.label),
      districts.map((item) => item.label),
      streets.map((item) => item.label),
      communities.map((item) => item.label)
    ]
  };
}

function findPickerValue(tree, address = {}) {
  const cityIndex = tree.findIndex((item) => item.value === address.city);
  const safeCityIndex = cityIndex >= 0 ? cityIndex : 0;
  const city = tree[safeCityIndex] || {};
  const districts = city.children || [];

  const districtIndex = districts.findIndex((item) => item.value === address.district);
  const safeDistrictIndex = districtIndex >= 0 ? districtIndex : 0;
  const district = districts[safeDistrictIndex] || {};
  const streets = district.children || [];

  const streetIndex = streets.findIndex((item) => item.value === address.street);
  const safeStreetIndex = streetIndex >= 0 ? streetIndex : 0;
  const street = streets[safeStreetIndex] || {};
  const communities = street.children || [];

  const communityIndex = communities.findIndex((item) => item.value === address.communityName);
  const safeCommunityIndex = communityIndex >= 0 ? communityIndex : 0;

  return normalizePickerValue(tree, [
    safeCityIndex,
    safeDistrictIndex,
    safeStreetIndex,
    safeCommunityIndex
  ]);
}

function resolveSelection(tree, pickerValue = [0, 0, 0, 0]) {
  const value = normalizePickerValue(tree, pickerValue);
  const city = tree[value[0]] || {};
  const district = (city.children || [])[value[1]] || {};
  const street = (district.children || [])[value[2]] || {};
  const community = (street.children || [])[value[3]] || {};

  return {
    value,
    city: city.value || '',
    district: district.value || '',
    street: street.value || '',
    communityName: community.value || '',
    text: [city.label, district.label, street.label, community.label].filter(Boolean).join(' / ')
  };
}

function buildNextPickerState(tree, pickerValue, column, selectedIndex) {
  const nextValue = [...pickerValue];
  nextValue[column] = selectedIndex;
  for (let i = column + 1; i < nextValue.length; i += 1) {
    nextValue[i] = 0;
  }
  return buildPickerRange(tree, nextValue);
}

module.exports = {
  buildPickerRange,
  buildNextPickerState,
  findPickerValue,
  resolveSelection
};
