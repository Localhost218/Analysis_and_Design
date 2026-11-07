import { useContext } from 'react';
import { DialogContext } from '../components/DialogProvider.jsx';

/** const dialog = useDialog(); await dialog.alert('...', { type: 'error' }); if (await dialog.confirm('...')) ... */
export default function useDialog() {
  return useContext(DialogContext);
}
