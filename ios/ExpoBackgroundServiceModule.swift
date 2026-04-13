import ExpoModulesCore
import CoreMotion

public class ExpoBackgroundServiceModule: Module {
  private let pedometer = CMPedometer()

  public func definition() -> ModuleDefinition {
    Name("ExpoBackgroundService")

    // Android ile aynı event isimlerini kullanıyoruz
    Events("onStepUpdate", "onTimerTick")

    Function("hello") {
      return "Sistem Hazır (iOS) - Şerif Çiçek"
    }

    // Donanım sayacından o anki adımı çeker (Delta motoru için)
    AsyncFunction("getStepCount") { (promise: Promise) in
      guard CMPedometer.isStepCountingAvailable() else {
        promise.resolve(0)
        return
      }
      
      // Bugünün başlangıcından şu ana kadar olan adımları çekiyoruz
      let calendar = Calendar.current
      let now = Date()
      let startOfDay = calendar.startOfDay(for: now)

      pedometer.queryPedometerData(from: startOfDay, to: now) { data, error in
        if let steps = data?.numberOfSteps {
          promise.resolve(steps.intValue)
        } else {
          promise.resolve(0)
        }
      }
    }

    Function("startService") { (title: String, body: String, initialSteps: Int) in
      // iOS'ta bildirimli servis yok, ama takibi başlatıyoruz
      self.startPedometerUpdates()
      return "iOS Takibi Başlatıldı"
    }

    Function("stopService") {
      pedometer.stopUpdates()
      return "iOS Takibi Durduruldu"
    }
  }

  private func startPedometerUpdates() {
    guard CMPedometer.isStepCountingAvailable() else { return }

    pedometer.startUpdates(from: Date()) { [weak self] data, error in
      guard let self = self, let steps = data?.numberOfSteps else { return }
      
      // JS tarafına anlık veri gönder (Android'deki onStepUpdate ile aynı)
      self.sendEvent("onStepUpdate", [
        "steps": steps.intValue
      ])
    }
  }
}