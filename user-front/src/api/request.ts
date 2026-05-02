const BASE_URL = 'https://api-mock.example.com'; // 预留接口地址

export const request = <T = any>(options: uni.RequestOptions): Promise<T> => {
  return new Promise((resolve, reject) => {
    uni.request({
      ...options,
      url: options.url.startsWith('http') ? options.url : `${BASE_URL}${options.url}`,
      header: {
        ...options.header,
        'Authorization': uni.getStorageSync('token') || '',
      },
      success: (res) => {
        if (res.statusCode >= 200 && res.statusCode < 300) {
          resolve(res.data as T);
        } else {
          uni.showToast({
            title: '请求失败',
            icon: 'none'
          });
          reject(res);
        }
      },
      fail: (err) => {
        uni.showToast({
          title: '网络错误',
          icon: 'none'
        });
        reject(err);
      }
    });
  });
};
