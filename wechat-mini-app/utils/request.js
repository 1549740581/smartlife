const { baseUrl } = require('./config');

function request({ url, method = 'GET', data, adminToken }) {
  return new Promise((resolve, reject) => {
    wx.request({
      url: `${baseUrl}${url}`,
      method,
      data,
      header: {
        'Content-Type': 'application/json',
        'X-Admin-Token': adminToken || ''
      },
      success(res) {
        const payload = res.data || {};
        if (res.statusCode >= 200 && res.statusCode < 300) {
          resolve(payload.data);
          return;
        }
        reject(payload.message || 'request failed');
      },
      fail(err) {
        reject(err.errMsg || 'network error');
      }
    });
  });
}

function uploadFile({ url, filePath, name = 'file', adminToken }) {
  return new Promise((resolve, reject) => {
    wx.uploadFile({
      url: `${baseUrl}${url}`,
      filePath,
      name,
      header: {
        'X-Admin-Token': adminToken || ''
      },
      success(res) {
        let payload = {};
        try {
          payload = JSON.parse(res.data || '{}');
        } catch (err) {
          reject('upload response parse failed');
          return;
        }
        if (res.statusCode >= 200 && res.statusCode < 300) {
          resolve(payload.data);
          return;
        }
        reject(payload.message || 'upload failed');
      },
      fail(err) {
        reject(err.errMsg || 'upload failed');
      }
    });
  });
}

module.exports = {
  request,
  uploadFile
};
