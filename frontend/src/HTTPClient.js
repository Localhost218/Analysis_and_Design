// Lớp phụ trợ HTTPClient: mọi lời gọi từ giao diện tới backend đều đi qua fetch()
const BASE = '/api';

const HTTPClient = {
  async fetch(method, path, body) {
    const options = { method, headers: {} };
    if (body !== undefined) {
      options.headers['Content-Type'] = 'application/json';
      options.body = JSON.stringify(body);
    }
    let res;
    try {
      res = await window.fetch(BASE + path, options);
    } catch {
      throw new Error('Không kết nối được tới máy chủ');
    }
    const text = await res.text();
    let data = null;
    if (text) {
      try {
        data = JSON.parse(text);
      } catch {
        data = null;
      }
    }
    if (!res.ok) {
      throw new Error(data?.message || `Lỗi máy chủ (${res.status})`);
    }
    return data;
  },
};

export default HTTPClient;
