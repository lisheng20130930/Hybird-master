#include "stdbool.h"
#include "string.h"
#include "libos.h"
#include "JNIHelper.h"


#ifdef __ANDROID__
JavaVM *g_vm = NULL;
jint JNI_OnLoad(JavaVM *vm, void *reserved)
{
    g_vm = vm;
    return JNI_VERSION_1_4;
}

bool getEnv(JNIEnv **env)
{
    bool bRet = false;    
    do {
    	  /* attach main thread */
        if((*g_vm)->GetEnv(g_vm,(void**)env, JNI_VERSION_1_4)==JNI_OK){  
        	  bRet = true;     
            break;
        }       
        /* attach thread */
        if ((*g_vm)->AttachCurrentThread(g_vm, env, 0) < 0){   
        	  /* error */         
            break;
        }
        bRet = true;
    }while(0);
    return bRet;
}

jclass getClassID_(const char *className, JNIEnv *env)
{
    JNIEnv *pEnv = env;
    jclass ret = 0;    
    do {
        if (! pEnv){
            if (! getEnv(&pEnv)){
                break;
            }
        }        
        ret = (*pEnv)->FindClass(pEnv,className);
        if (! ret){
            break;
        }
    }while(0);    
    return ret;
}

bool getStaticMethodInfo_(JniMethodInfo *methodinfo, const char *className, const char *methodName, const char *paramCode)
{
    jmethodID methodID = 0;
    JNIEnv *pEnv = 0;
    bool bRet = false;
    
    do {
        jclass clsid;        
        if (! getEnv(&pEnv)){
            break;
        }
        
        clsid = getClassID_(className, pEnv);        
        methodID = (*pEnv)->GetStaticMethodID(pEnv,clsid, methodName, paramCode);
        if (! methodID){
            break;
        }        
        methodinfo->classID = clsid;
        methodinfo->env = pEnv;
        methodinfo->methodID = methodID;        
        bRet = true;
    }while(0);
    
    return bRet;
}

bool getMethodInfo_(JniMethodInfo *methodinfo, const char *className, const char *methodName, const char *paramCode)
{
    jmethodID methodID = 0;
    JNIEnv *pEnv = 0;
    bool bRet = false;    
    do {
        jclass classID;        
        if (! getEnv(&pEnv)){
            break;
        }
        
        classID = getClassID_(className, pEnv);        
        methodID = (*pEnv)->GetMethodID(pEnv,classID, methodName, paramCode);
        if (! methodID){            
            break;
        }        
        methodinfo->classID = classID;
        methodinfo->env = pEnv;
        methodinfo->methodID = methodID;        
        bRet = true;
    }while(0);    
    return bRet;
}
#endif