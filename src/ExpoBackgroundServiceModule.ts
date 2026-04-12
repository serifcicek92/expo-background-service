import { NativeModule, requireNativeModule } from 'expo';

import { ExpoBackgroundServiceModuleEvents } from './ExpoBackgroundService.types';

declare class ExpoBackgroundServiceModule extends NativeModule<ExpoBackgroundServiceModuleEvents> {
  PI: number;
  hello(): string;
  setValueAsync(value: string): Promise<void>;
}

// This call loads the native module object from the JSI.
export default requireNativeModule<ExpoBackgroundServiceModule>('ExpoBackgroundService');
