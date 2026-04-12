import { registerWebModule, NativeModule } from 'expo';

import { ExpoBackgroundServiceModuleEvents } from './ExpoBackgroundService.types';

class ExpoBackgroundServiceModule extends NativeModule<ExpoBackgroundServiceModuleEvents> {
  PI = Math.PI;
  async setValueAsync(value: string): Promise<void> {
    this.emit('onChange', { value });
  }
  hello() {
    return 'Hello world! 👋';
  }
}

export default registerWebModule(ExpoBackgroundServiceModule, 'ExpoBackgroundServiceModule');
