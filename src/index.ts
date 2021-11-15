import { registerPlugin } from '@capacitor/core';

import type { GpayPlugin } from './definitions';

const Gpay = registerPlugin<GpayPlugin>('Gpay', {
  web: () => import('./web').then(m => new m.GpayWeb()),
});

export * from './definitions';
export { Gpay };
