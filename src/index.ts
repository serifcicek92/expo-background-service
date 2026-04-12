// Reexport the native module. On web, it will be resolved to ExpoBackgroundServiceModule.web.ts
// and on native platforms to ExpoBackgroundServiceModule.ts
export { default } from './ExpoBackgroundServiceModule';
export { default as ExpoBackgroundServiceView } from './ExpoBackgroundServiceView';
export * from  './ExpoBackgroundService.types';
