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

// Şerif abi, işte buraya (title ve body) parametrelerini ekledik:
export function startBackgroundService(title: string, body: string, initialSteps: number = 0): string {
  return (ExpoBackgroundServiceModule as any).startService(title, body, initialSteps);
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

// ====================================================================
// --- YENİ EKLENEN ÖZEL SENSÖR METODLARI (ŞERİF ABİ'NİN KONTROLÜNDE) ---
// ====================================================================

// 1. Donanım Raporu: Sensörler telefonda var mı diye bakar.
export async function getSensorStatus(): Promise<any> {
  return await (ExpoBackgroundServiceModule as any).getSensorStatus();
}

// 2. Sayaç (Getter): O anki ham toplam sayacı çeker (Pull mantığı).
export async function getStepCounterValue(): Promise<number> {
  return await (ExpoBackgroundServiceModule as any).getStepCounterValue();
}

// 3. Dedektör Başlat (Trigger): Anlık adım tıklarını dinlemeye başlar.
export async function startStepDetection(): Promise<boolean> {
  return await (ExpoBackgroundServiceModule as any).startStepDetection();
}

// 4. Dedektör Dinleyici (Listener): Her adımda tetiklenir (Push mantığı).
export function addStepDetectedListener(listener: (event: { detected: boolean }) => void): any {
  return (emitter as any).addListener('onStepDetected', listener);
}