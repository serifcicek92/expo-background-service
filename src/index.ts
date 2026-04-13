import { EventEmitter } from 'expo-modules-core';
import ExpoBackgroundServiceModule from './ExpoBackgroundServiceModule';

// Emitter'ı 'any' üzerinden kurarak tip kontrolünü devre dışı bırakıyoruz
const emitter = new EventEmitter(ExpoBackgroundServiceModule as any);

export function checkServiceStatus(): string {
  return ExpoBackgroundServiceModule.hello();
}

export function getCurrentStepCount(): number {
  return (ExpoBackgroundServiceModule as any).getStepCount();
}

export function startBackgroundService(): string {
  return (ExpoBackgroundServiceModule as any).startService();
}

export function stopBackgroundService(): string {
  return (ExpoBackgroundServiceModule as any).stopService();
}

export function isBackgroundServiceRunning(): boolean {
  return (ExpoBackgroundServiceModule as any).isServiceRunning();
}

/** * Dinleyiciler: 
 * Geriye 'any' dönüyoruz ki Expo sürümünden bağımsız her türlü Subscription tipini kabul etsin.
 */
export function addStepListener(listener: (event: { steps: number }) => void): any {
  return (emitter as any).addListener('onStepUpdate', listener);
}

export function addTimerListener(listener: (event: { steps: number, message: string }) => void): any {
  return (emitter as any).addListener('onTimerTick', listener);
}