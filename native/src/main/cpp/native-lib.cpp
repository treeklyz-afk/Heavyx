#include <jni.h>
#include <string>
#include <vector>
#include <random>
#include <sstream>
#include <cmath>
#include <algorithm>
#include <chrono>
#include <iomanip>

#ifndef M_PI
#define M_PI 3.14159265358979323846
#endif

struct Driver {
    std::string id;
    std::string name;
    std::string vehicle;
    std::string regNumber;
    double lat;
    double lng;
    double rating;
    double priceMultiplier;
    int eta;
    bool available;
    std::string status;
};

static std::vector<Driver> driverPool;

double haversine(double lat1, double lon1, double lat2, double lon2) {
    const double R = 6371.0;
    double dLat = (lat2 - lat1) * M_PI / 180.0;
    double dLon = (lon2 - lon1) * M_PI / 180.0;
    double a = sin(dLat / 2) * sin(dLat / 2) +
               cos(lat1 * M_PI / 180.0) * cos(lat2 * M_PI / 180.0) *
               sin(dLon / 2) * sin(dLon / 2);
    double c = 2 * atan2(sqrt(a), sqrt(1 - a));
    return R * c;
}

extern "C" JNIEXPORT void JNICALL
Java_com_drix_MainActivity_initDriverPoolNative(JNIEnv *env, jobject thiz) {
    driverPool.clear();
    const char *names[] = {"Vikram Singh", "Rajesh Kumar", "Aman Verma", "Priya Sharma", "Arjun Reddy",
                           "Sneha Kapoor", "Ravi Desai", "Neha Joshi", "Karan Mehta", "Pooja Nair"};
    const char *vehicles[] = {"Toyota Etios", "Maruti Dzire", "Hyundai i20", "Honda City", "Mahindra XUV"};
    const char *regs[] = {"KA-03-HA-4321", "DL-01-CA-9876", "BR-01-PC-5543", "MH-12-AB-1234", "TN-07-CD-5678"};

    for (int i = 0; i < 10; i++) {
        Driver d;
        d.id = "D" + std::to_string(i + 100);
        d.name = names[i % 10];
        d.vehicle = vehicles[i % 5];
        d.regNumber = regs[i % 5];
        d.lat = 28.6139 + ((rand() % 2000 - 1000) / 10000.0);
        d.lng = 77.2090 + ((rand() % 2000 - 1000) / 10000.0);
        d.rating = 4.0 + (rand() % 10) / 10.0;
        d.available = true;
        d.eta = 0;
        d.priceMultiplier = 1.0 + (rand() % 5) / 10.0;
        d.status = "online";
        driverPool.push_back(d);
    }
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_drix_MainActivity_findDriverNative(JNIEnv *env, jobject thiz, jstring pickup, jstring drop) {
    const char *pickupChars = env->GetStringUTFChars(pickup, nullptr);
    const char *dropChars = env->GetStringUTFChars(drop, nullptr);
    std::string pickupStr(pickupChars);
    std::string dropStr(dropChars);
    env->ReleaseStringUTFChars(pickup, pickupChars);
    env->ReleaseStringUTFChars(drop, dropChars);

    double pickupLat = 28.6139, pickupLng = 77.2090;

    double bestScore = -1.0;
    Driver bestDriver;
    bool found = false;

    for (const auto &d : driverPool) {
        if (!d.available || d.status != "online") continue;
        double dist = haversine(pickupLat, pickupLng, d.lat, d.lng);
        if (dist > 15.0) continue;
        double score = (15.0 - dist) * 1.5 + d.rating * 2.0 - d.priceMultiplier * 0.5;
        if (score > bestScore) {
            bestScore = score;
            bestDriver = d;
            found = true;
        }
    }

    std::stringstream response;
    if (!found) {
        response << "⚠️ Premium Matrix Notice:\nAll matching executive riders are currently on route. Retrying sync status...";
        return env->NewStringUTF(response.str().c_str());
    }

    double dist = haversine(pickupLat, pickupLng, bestDriver.lat, bestDriver.lng);
    int eta = std::max(2, static_cast<int>(dist / 30.0 * 60.0));

    response << "📋 iOS Executive Dispatch:\n\n"
             << "Driver Allocated: " << bestDriver.name << "\n"
             << "Vehicle Model: " << bestDriver.vehicle << " [" << bestDriver.regNumber << "]\n"
             << "Performance Star: " << std::fixed << std::setprecision(1) << bestDriver.rating << " / 5.0\n"
             << "Route Context: " << pickupStr << " -> " << dropStr << "\n"
             << "Dispatch ETA: " << eta << " mins (Distance: " << std::fixed << std::setprecision(1) << dist << " km)";

    return env->NewStringUTF(response.str().c_str());
}
