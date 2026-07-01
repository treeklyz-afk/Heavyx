#include <jni.h>
#include <string>
extern "C" JNIEXPORT jstring JNICALL
Java_com_drix_MainActivity_stringFromJNI(JNIEnv* env, jobject thiz) {
    return env->NewStringUTF("Hello from DriderX Online Live Patch!");
}
