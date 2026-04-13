import ExpoModulesCore
import CoreMotion

public class ExpoBackgroundServiceModule: Module {
  // iOS'un adım sayar motoru
  private let pedometer = CMPedometer()

  public func definition() -> ModuleDefinition {
    // Paket ismi JS tarafıyla aynı olmalı
    Name("ExpoBackgroundService")

    // Android'deki EventEmitter ile aynı isimde kanal açıyoruz
    Events("onStepUpdate", "onTimerTick")

    Function("hello") {
      return "iOS Sistem Hazır - Şerif Çiçek"
    }

    // Android'deki getStepCount ile aynı işi yapar
    AsyncFunction("getStepCount") { (promise: Promise) in
      guard CMPedometer.isStepCountingAvailable() else {
        promise.resolve(0)
        return
      }

      let now = Date()
      let startOfDay = Calendar.current.startOfDay(for: now)

      // Bugünden itibaren olan adımları sorgula
      pedometer.queryPedometerData(from: startOfDay, to: now) { data, error in
        if let data = data {
          promise.resolve(data.numberOfSteps.intValue)
        } else {
          promise.resolve(0)
        }
      }
    }

    Function("startService") {
      self.startStepUpdates()
      return "iOS Adım Takibi Başlatıldı"
    }

    Function("stopService") {
      self.pedometer.stopUpdates()
      return "iOS Adım Takibi Durduruldu"
    }
    
    // iOS'ta servis durumu kontrolü (Sensör aktif mi?)
    Function("isServiceRunning") {
        return true // iOS'ta sensör erişimi genellikle sistem seviyesindedir
    }
  }

  private func startStepUpdates() {
    guard CMPedometer.isStepCountingAvailable() else { return }

    pedometer.startUpdates(from: Date()) { [weak self] data, error in
      guard let data = data else { return }
      
      // JS tarafındaki addStepListener'ı tetikler
      self?.sendEvent("onStepUpdate", [
        "steps": data.numberOfSteps.intValue
      ])
    }
  }
}