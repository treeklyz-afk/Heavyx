#include <jni.h>
#include <string>
#include <random>
#include <sstream>

extern "C" JNIEXPORT jstring JNICALL
Java_com_drix_MainActivity_findDriverNative(JNIEnv* env, jobject thiz, jstring pickup, jstring drop) {
    
    const char* pickupChars = env->GetStringUTFChars(pickup, nullptr);
    const char* dropChars = env->GetStringUTFChars(drop, nullptr);
    
    std::string pickupLocation(pickupChars);
    std::string dropLocation(dropChars);
    
    env->ReleaseStringUTFChars(pickup, pickupChars);
    env->ReleaseStringUTFChars(drop, dropChars);
    
    std::string drivers[] = {"Vikram Singh [Rider Pro]", "Rajesh Kumar [Elite]", "Aman Verma [X-Prime]"};
    std::string vehicles[] = {"(KA-03-HA-4321)", "(DL-01-CA-9876)", "(BR-01-PC-5543)"};
    
    std::random_device rd;
    std::mt19937 gen(rd());
    std::uniform_int_distribution<> dist(0, 2);
    std::uniform_int_distribution<> timeDist(2, 7);
    
    int index = dist(gen);
    int eta = timeDist(gen);
    
    std::stringstream response;
    response << "⚡ Driver Matched Successfully!\n\n"
             << "Driver: " << drivers[index] << "\n"
             << "Vehicle: " << vehicles[index] << "\n"
             << "Route: " << pickupLocation << " -> " << dropLocation << "\n"
             << "Status: Arriving in " << eta << " mins";
             
    return env->NewStringUTF(response.str().c_str());
}
