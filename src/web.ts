import {WebPlugin} from '@capacitor/core';

import type {GpayPlugin, IsReadyToPayResponse, PaymentData} from './definitions';

export class GpayWeb extends WebPlugin implements GpayPlugin {
  createClient(): Promise<void> {
    throw new Error('Method not implemented');
  }

  isReadyToPay(): Promise<IsReadyToPayResponse> {
    throw new Error('Method not implemented');
  }

  loadPaymentData(): Promise<PaymentData> {
    throw new Error('Method not implemented');
  }
}
