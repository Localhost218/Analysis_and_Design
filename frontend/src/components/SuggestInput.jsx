import { useEffect, useRef, useState } from 'react';

/**
 * Ô nhập có danh sách gợi ý: gõ từ khóa -> gọi search(keyword) -> chọn một mục.
 * notFoundText hiện khi không có kết quả (vd: "Không tìm thấy nhà tài trợ").
 */
export default function SuggestInput({
  id, value, onChange, search, renderItem, onSelect, placeholder, notFoundText, disabled,
}) {
  const [items, setItems] = useState([]);
  const [open, setOpen] = useState(false);
  const [searched, setSearched] = useState(false);
  const [error, setError] = useState('');
  const typed = useRef(false);

  useEffect(() => {
    if (!typed.current) return undefined;
    const keyword = value.trim();
    if (!keyword) {
      setItems([]);
      setSearched(false);
      return undefined;
    }
    const timer = setTimeout(async () => {
      try {
        setItems(await search(keyword));
        setError('');
      } catch (e) {
        setItems([]);
        setError(e.message);
      }
      setSearched(true);
      setOpen(true);
    }, 300);
    return () => clearTimeout(timer);
  }, [value, search]);

  const choose = (item) => {
    typed.current = false;
    setOpen(false);
    setItems([]);
    setSearched(false);
    onSelect(item);
  };

  return (
    <div className="suggest">
      <input
        id={id}
        value={value}
        placeholder={placeholder}
        disabled={disabled}
        autoComplete="off"
        onChange={(e) => {
          typed.current = true;
          onChange(e.target.value);
        }}
        onFocus={() => searched && setOpen(true)}
        onBlur={() => setTimeout(() => setOpen(false), 150)}
      />
      {open && searched && (
        <ul className="suggest-list" role="listbox">
          {error && <li className="suggest-empty">{error}</li>}
          {!error && items.length === 0 && <li className="suggest-empty">{notFoundText}</li>}
          {items.map((item) => (
            <li key={item.id} role="option" aria-selected="false" onMouseDown={() => choose(item)}>
              {renderItem(item)}
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}
