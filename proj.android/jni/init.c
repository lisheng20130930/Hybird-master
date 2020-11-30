/*
 Android CLIENT.  Listen.Li 20201130
 For JNI
*/
#include <jni.h>
#include <stdio.h>
#include <stdlib.h>
#include <time.h>
#include <unistd.h>
#include "libos.h"
#include "utils.h"
#include "evfunclib.h"
#include "assetMgr.h"
#include "JNIHelper.h"


#include <android/asset_manager_jni.h>
#include <android/asset_manager.h>


#define MAXCNN   (2048)


extern void nativeSetAssetManager(JNIEnv* env, jobject thiz, jobject assetManager);
extern char *g_dataPathDir;

static unsigned int loops = 0;


char* getMoblieParamter(char *key)
{
	static char g_szBuffer[40960]={0}; //40KB
	jboolean isCopy = false;
	JniMethodInfo t;
	jstring szJStr = NULL;
	const char* str = NULL; 
	
	memset(g_szBuffer, 0x00, 40960);   
	if(getStaticMethodInfo_(&t, "com/qp/uapp/CmmnHelper", "getMoblieParamter", "(Ljava/lang/String;)Ljava/lang/String;")){
	  jstring stringArg = (*t.env)->NewStringUTF(t.env,key);
	  szJStr = (jstring)(*t.env)->CallStaticObjectMethod(t.env, t.classID, t.methodID,stringArg);
	  (*t.env)->DeleteLocalRef(t.env,stringArg);
	  (*t.env)->DeleteLocalRef(t.env,t.classID);
	  if(NULL != szJStr){
	      str = (*t.env)->GetStringUTFChars(t.env, szJStr, &isCopy);
	      if(isCopy){
	          strcpy(g_szBuffer,str);
	          (*t.env)->ReleaseStringUTFChars(t.env, szJStr, str);
	      }
	  }
	}
	return g_szBuffer;
}


unsigned int getVersionCode()
{
	DBGPRINT(EMSG,("[Trace@JNI] getVersionCode\r\n"));
	JniMethodInfo t;
	int iRet = 0;
  if(!getStaticMethodInfo_(&t, "com/qp/uapp/CmmnHelper", "getVersionCode", "()I")){			
  	DBGPRINT(EMSG,("[Trace@JNI] 333\r\n"));
    return iRet;
  }
  iRet=(*t.env)->CallStaticIntMethod(t.env,t.classID, t.methodID);
	(*t.env)->DeleteLocalRef(t.env,t.classID);
	DBGPRINT(EMSG,("[Trace@JNI] Leave...\r\n"));
	return (unsigned int)iRet;
}

void Java_com_qp_uapp_CmmnHelper_nativeInit(JNIEnv* env,jobject thiz,jobject assetManager,jstring dataDir)
{
	 jboolean isCopy = 0;
	 const char *_pszDataDir = (*env)->GetStringUTFChars(env, dataDir, &isCopy);
	 g_dataPathDir = cmmn_strdup(_pszDataDir);
	 DBGPRINT(EMSG,("[Trace@JNI] QPHelper_nativeInit\r\n"));
	 if(isCopy){
		(*env)->ReleaseStringUTFChars(env,dataDir,_pszDataDir);
	 }
	 nativeSetAssetManager(env,thiz,assetManager);
	 AssetMgr_setPerference(getMoblieParamter("channelID"),getVersionCode());
	 evnet_init(MAXCNN,0,0x0F);
	 DBGPRINT(EMSG,("[Trace@JNI] Leave....\r\n"));
}

void Java_com_qp_uapp_CmmnHelper_nativeLoop(JNIEnv* env)
{
	 evnet_loop(loops);
     loops++;
}

void Java_com_qp_uapp_CmmnHelper_nativeUint(JNIEnv* env)
{
	 DBGPRINT(EMSG,("[Trace@JNI] QPHelper_nativeUint....\r\n"));
	 evnet_uint();
	 DBGPRINT(EMSG,("[Trace@JNI] Leave....\r\n"));
}

void Java_com_qp_hybird_HBAssetMgr_assetMgrSetConfig(JNIEnv* env,jobject thiz,jstring szUrl)
{
	 DBGPRINT(EMSG,("[Trace@JNI] QPHelper_assetMgrSetConfig....\r\n"));
	 jboolean isCopy = 0;
	 const char *_szURL = (*env)->GetStringUTFChars(env, szUrl, &isCopy);
	 AssetMgr_setConfig((char*)_szURL);
	 if(isCopy){
		(*env)->ReleaseStringUTFChars(env,szUrl,_szURL);
	 }
	 DBGPRINT(EMSG,("[Trace@JNI] Leave....\r\n"));
}

void Java_com_qp_hybird_HBAssetMgr_assetMgrUpdate(JNIEnv* env)
{
	 DBGPRINT(EMSG,("[Trace@JNI] QPHelper_assetMgrUpdate\r\n"));
	 AssetMgr_update();
}

int Java_com_qp_hybird_HBAssetMgr_assetMgrGetTotalCount(JNIEnv* env)
{
	 DBGPRINT(EMSG,("[Trace@JNI] QPHelper_assetMgrGetTotalCount\r\n"));
	 return AssetMgr_totalCount();
}

int Java_com_qp_hybird_HBAssetMgr_assetMgrGetCurrCount(JNIEnv* env)
{
	 DBGPRINT(EMSG,("[Trace@JNI] QPHelper_assetMgrGetCurrCount\r\n"));
	 return AssetMgr_curCount();
}

void Java_com_qp_uapp_CmmnHelper_log(JNIEnv* env,jobject thiz,jstring traceStr)
{
	 const char *_traceStr = (*env)->GetStringUTFChars(env, traceStr, NULL);
	 DBGPRINT(EMSG,((char*)_traceStr));
	 (*env)->ReleaseStringUTFChars(env,traceStr,_traceStr);
	 return;
}

int Java_com_qp_hybird_HBAssetMgr_assetMgrGetStatus(JNIEnv* env)
{
	 DBGPRINT(EMSG,("[Trace@JNI] assetMgrGetStatus\r\n"));
	 return AssetMgr_getStatus();
}

void Java_com_qp_hybird_HBAssetMgr_assetMgrClear(JNIEnv* env)
{
	 DBGPRINT(EMSG,("[Trace@JNI] assetMgrClear\r\n"));
	 AssetMgr_clear();
}

void Java_com_qp_hybird_HBAssetMgr_assetMgrRemoveALL(JNIEnv* env)
{
	 DBGPRINT(EMSG,("[Trace@JNI] QPHelper_assetMgrRemoveALL\r\n"));
	 AssetMgr_removeALL();
}

long Java_com_qp_hybird_HBAssetMgr_assetMgrVersionCode(JNIEnv* env)
{
	 DBGPRINT(EMSG,("[Trace@JNI] assetMgrVersionCode\r\n"));
	 return (long)AssetMgr_combinedVersionCode();
}
