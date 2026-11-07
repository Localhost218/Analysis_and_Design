const TONES = {
  'Đang hợp tác': 'success',
  'Ngừng hợp tác': 'danger',
  'Đã ký': 'success',
  'Chờ ký': 'warning',
  'Đã trả': 'success',
  'Chưa trả': '',
  'Thanh toán thành công': 'success',
  'Thanh toán thất bại': 'danger',
};

export function StatusBadge({ status, id }) {
  return <span id={id} className={`badge ${TONES[status] ?? ''}`}>{status}</span>;
}

/** Hạng về đích: huy chương cho 1–3, trạng thái không hoàn thành hiển thị chữ. */
export function RankBadge({ rank, completedStatus }) {
  if (rank == null) return <span className="rank out">{completedStatus}</span>;
  const cls = rank <= 3 ? ` p${rank}` : '';
  return <span className={`rank${cls}`}>{rank}</span>;
}
