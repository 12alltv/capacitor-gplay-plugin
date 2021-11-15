export interface GpayPlugin {
  echo(options: { value: string }): Promise<{ value: string }>;
}
