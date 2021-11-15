import { WebPlugin } from '@capacitor/core';

import type { GpayPlugin } from './definitions';

export class GpayWeb extends WebPlugin implements GpayPlugin {
  async echo(options: { value: string }): Promise<{ value: string }> {
    console.log('ECHO', options);
    return options;
  }
}
