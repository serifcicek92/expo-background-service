import * as React from 'react';

import { ExpoBackgroundServiceViewProps } from './ExpoBackgroundService.types';

export default function ExpoBackgroundServiceView(props: ExpoBackgroundServiceViewProps) {
  return (
    <div>
      <iframe
        style={{ flex: 1 }}
        src={props.url}
        onLoad={() => props.onLoad({ nativeEvent: { url: props.url } })}
      />
    </div>
  );
}
