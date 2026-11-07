import { useCallback, useRef, useState } from 'react';

// "Lưu vào session" / "Lấy từ session" trong biểu đồ hoạt động, lưu ở sessionStorage.
// Mọi truy cập storage đều bọc try/catch để trang vẫn chạy khi trình duyệt chặn storage.

function read(key) {
  try {
    const raw = sessionStorage.getItem(key);
    return raw ? JSON.parse(raw) : null;
  } catch {
    return null;
  }
}

function write(key, value) {
  try {
    if (value === null || value === undefined) sessionStorage.removeItem(key);
    else sessionStorage.setItem(key, JSON.stringify(value));
  } catch {
    /* bỏ qua */
  }
}

/** Dùng ngoài component (route guard, đăng xuất...). */
export const session = {
  get: read,
  set: write,
  remove: (...keys) => keys.forEach((k) => write(k, null)),
  clear: () => {
    try {
      sessionStorage.clear();
    } catch {
      /* bỏ qua */
    }
  },
};

/**
 * State React đồng bộ với một key trong session.
 * const [season, setSeason] = useSession('season');   // setSeason(null) để xóa khỏi session
 */
export default function useSession(key, initialValue = null) {
  const [value, setValue] = useState(() => {
    const stored = read(key);
    if (stored !== null) return stored;
    return typeof initialValue === 'function' ? initialValue() : initialValue;
  });

  // Ghi vào session NGAY khi gọi setter (không đợi React render), vì trang thường
  // navigate() sang trang khác ngay sau đó và trang mới đọc session lập tức.
  const current = useRef(value);
  const update = useCallback(
    (next) => {
      const resolved = typeof next === 'function' ? next(current.current) : next;
      current.current = resolved;
      write(key, resolved);
      setValue(resolved);
    },
    [key],
  );

  return [value, update];
}
