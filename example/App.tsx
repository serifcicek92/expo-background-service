import React, { useEffect, useState } from 'react';
import { SafeAreaView, Text, StyleSheet, TouchableOpacity, Alert, PermissionsAndroid, Platform, View } from 'react-native';
import { checkServiceStatus, startBackgroundService, stopBackgroundService, isBackgroundServiceRunning, getCurrentStepCount } from '@serifcicek92/expo-background-service';

export default function App() {
  const [steps, setSteps] = useState(0);
  const [isRunning, setIsRunning] = useState(false);

  useEffect(() => {
    requestPermissions();

    // Uygulama açılır açılmaz servisin durumunu kontrol et
    const serviceStatus = isBackgroundServiceRunning();
    setIsRunning(serviceStatus);

    // Son adım sayısını al
    setSteps(getCurrentStepCount());
  }, []);

  async function requestPermissions() {
    if (Platform.OS === 'android') {
      await PermissionsAndroid.request(PermissionsAndroid.PERMISSIONS.ACTIVITY_RECOGNITION);
      if (Platform.Version >= 33) {
        await PermissionsAndroid.request(PermissionsAndroid.PERMISSIONS.POST_NOTIFICATIONS);
      }
    }
  }

  const handleStart = () => {
    const res = startBackgroundService();
    setIsRunning(true);
    Alert.alert("Durum", res);
  };

  const handleStop = () => {
    const res = stopBackgroundService();
    setIsRunning(false);
    Alert.alert("Durum", res);
  };

  return (
    <SafeAreaView style={styles.container}>
      <Text style={styles.title}>Şerif Çiçek - Pro Test</Text>
      
      <Text style={styles.statusLabel}>Servis Durumu:</Text>
      <Text style={[styles.statusText, { color: isRunning ? '#4CAF50' : '#F44336' }]}>
        {isRunning ? "AKTİF (Arka Planda Çalışıyor)" : "DURDURULDU"}
      </Text>

      <Text style={[styles.statusLabel, {marginTop: 20}]}>Hafızadaki Adım:</Text>
      <Text style={styles.result}>{steps}</Text>

      {/* Eğer çalışıyorsa Durdur butonu, çalışmıyorsa Başlat butonu görünsün */}
      {!isRunning ? (
        <TouchableOpacity style={styles.buttonStart} onPress={handleStart}>
          <Text style={styles.buttonText}>Servisi Başlat</Text>
        </TouchableOpacity>
      ) : (
        <TouchableOpacity style={styles.buttonStop} onPress={handleStop}>
          <Text style={styles.buttonText}>Servisi Durdur</Text>
        </TouchableOpacity>
      )}

      <TouchableOpacity style={styles.buttonUpdate} onPress={() => setSteps(getCurrentStepCount())}>
        <Text style={styles.buttonText}>Adımı Getir / Güncelle</Text>
      </TouchableOpacity>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, justifyContent: 'center', alignItems: 'center', backgroundColor: '#1E1E1E' },
  title: { fontSize: 24, fontWeight: 'bold', color: '#FFF', marginBottom: 20 },
  statusLabel: { fontSize: 16, color: '#AAA' },
  statusText: { fontSize: 18, fontWeight: 'bold', marginBottom: 10 },
  result: { fontSize: 48, fontWeight: 'bold', color: '#4CAF50', marginBottom: 30 },
  buttonStart: { backgroundColor: '#007AFF', paddingHorizontal: 30, paddingVertical: 15, borderRadius: 8, width: 200, alignItems: 'center' },
  buttonStop: { backgroundColor: '#F44336', paddingHorizontal: 30, paddingVertical: 15, borderRadius: 8, width: 200, alignItems: 'center' },
  buttonUpdate: { backgroundColor: '#34C759', paddingHorizontal: 30, paddingVertical: 15, borderRadius: 8, width: 200, alignItems: 'center', marginTop: 20 },
  buttonText: { color: '#FFF', fontSize: 16, fontWeight: 'bold' }
});