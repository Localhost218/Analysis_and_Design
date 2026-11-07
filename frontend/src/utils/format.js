// Định dạng hiển thị: ngày dd/mm/yyyy, ngày giờ dd/mm/yyyy hh:mm, tiền 1.000.000 VNĐ
const pad = (n) => String(n).padStart(2, '0');

export function formatDate(value) {
  if (!value) return '';
  const [y, m, d] = String(value).slice(0, 10).split('-');
  return `${d}/${m}/${y}`;
}

export function formatDateTime(value) {
  if (!value) return '';
  const d = new Date(value);
  return `${pad(d.getDate())}/${pad(d.getMonth() + 1)}/${d.getFullYear()} ${pad(d.getHours())}:${pad(d.getMinutes())}`;
}

export function formatMoney(value) {
  return `${Number(value || 0).toLocaleString('vi-VN')} VNĐ`;
}

export function todayISO() {
  const d = new Date();
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`;
}

/** Mã nhà tài trợ hiển thị, vd: S004 */
export function sponsorCode(id) {
  return `S${String(id).padStart(3, '0')}`;
}

/** Mã chặng từ tên "C03 – Australian" -> "C03" */
export function raceCode(name) {
  return String(name || '').split(' – ')[0];
}

/** "2025 – C03" */
export function seasonRaceLabel(race) {
  return `${race?.season?.year ?? ''} – ${raceCode(race?.name)}`;
}

/** Thời gian thi đấu từ lúc xuất phát tới lúc về đích, dạng h:mm:ss */
export function duration(startTime, finishedTime) {
  if (!startTime || !finishedTime) return '—';
  const s = Math.round((new Date(finishedTime) - new Date(startTime)) / 1000);
  if (s < 0) return '—';
  return `${Math.floor(s / 3600)}:${pad(Math.floor((s % 3600) / 60))}:${pad(s % 60)}`;
}

export function isRaceEnded(race) {
  return !!race?.endTime && new Date(race.endTime) < new Date();
}
