import { useCallback } from 'react';
import useSession, { session } from './useSession.js';
import { todayISO } from '../utils/format.js';

const KEY = 'contract';

function emptyContract(organizer) {
  return {
    id: null,
    barcode: null,
    status: 'Chờ ký',
    createdDate: todayISO(),
    signedDate: null,
    sponsor: null,
    season: null,
    value: '',
    effectiveFrom: '',
    effectiveTo: '',
    sponsorRepresentative: '',
    organizerRepresentative: organizer?.fullName || '',
    listContractTerm: [],
    listContractBenefit: [],
  };
}

/**
 * Hợp đồng đang soạn (thuộc tính ẩn "contract" của ContractSigning).
 * Giữ trong session nên không mất dữ liệu khi qua NewSponsor / NewSeason / NewTerm / NewBenefit.
 */
export default function useContractDraft() {
  const [contract, setContract] = useSession(KEY, () => emptyContract(session.get('organizer')));

  /** patchContract({ sponsor }) hoặc patchContract(c => ({ ...c, listContractTerm: [...] })) */
  const patchContract = useCallback(
    (patch) => setContract((prev) => (typeof patch === 'function' ? patch(prev) : { ...prev, ...patch })),
    [setContract],
  );

  /** Bắt đầu soạn hợp đồng mới. */
  const resetContract = useCallback(
    () => setContract(emptyContract(session.get('organizer'))),
    [setContract],
  );

  /** Bỏ hợp đồng đang soạn khỏi session (dùng khi rời trang bằng nút Hủy). */
  const discardContract = useCallback(() => session.remove(KEY), []);

  return { contract, patchContract, resetContract, discardContract };
}
