import { requireNativeView } from 'expo';
import * as React from 'react';

import { ExpoBackgroundServiceViewProps } from './ExpoBackgroundService.types';

const NativeView: React.ComponentType<ExpoBackgroundServiceViewProps> =
  requireNativeView('ExpoBackgroundService');

export default function ExpoBackgroundServiceView(props: ExpoBackgroundServiceViewProps) {
  return <NativeView {...props} />;
}
