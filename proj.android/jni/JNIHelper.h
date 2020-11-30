#ifndef __JNIHELPER_H__
#define __JNIHELPER_H__

#ifdef __cplusplus
extern "C"{
#endif /* __cplusplus */

#ifdef __ANDROID__
#include "jni.h"
typedef struct JniMethodInfo_
{
    JNIEnv *    env;
    jclass      classID;
    jmethodID   methodID;
}JniMethodInfo;

bool getEnv(JNIEnv **env);
bool getStaticMethodInfo_(JniMethodInfo *methodinfo, const char *className, const char *methodName, const char *paramCode);
bool getMethodInfo_(JniMethodInfo *methodinfo, const char *className, const char *methodName, const char *paramCode);
#endif

#ifdef __cplusplus
}
#endif /* __cplusplus */
#endif