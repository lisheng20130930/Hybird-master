/*
 Win32 CLIENT.  Listen.Li 20180827
 For Test Core Module
*/
#include "libos.h"
#include "utils.h"
#include "QApp.h"
#include "evfunclib.h"
#include "assetMgr.h"
#include "cJSON.h"
#include "AppConstants.h"


#define MAXCNN   (2048)


extern char *g_dataPathDir;
extern char *g_resPathDir;


static bool g_bIsUpdating = false;


static void hotUpdateLoop()
{
	AssetMgr_update(); /* update */

	int status = AssetMgr_getStatus();
	printf("hot update status = %d ",status);
	
	if(StartLoading<=status){
		int totalCount = AssetMgr_totalCount();
		int currCount = AssetMgr_curCount();
		printf("totalCount = %d,currCount = %d\r\n",totalCount,currCount);
	}else{
		printf("\r\n");
	}
	
	if(EndLoading==status
		||EError==status){
		g_bIsUpdating = false;
		printf("Hot Update DONE!!!!(status=%d)\r\n",status);
		exit(0);
	}
}


static int invoke_curl_download(char *name, char *szUrl)
{
	remove(name); // remove IF
	char cmd[2048] = {0};
	sprintf(cmd,"curl.exe -s -o \"%s\" \"%s\"",name,szUrl);
	system(cmd);
	return 0;
}


static void myHandler(int errorCode, char *str)
{
    DBGPRINT(EERROR,("code=%d, buffer=%s\r\n", errorCode,str?str:"Null"));
	if(0!=errorCode){
		printf("str=%s\r\n",str);
		exit(0);
		return;
	}

	cJSON *pRoot = cJSON_Parse(str); //½âÎö
	if(!pRoot){
		exit(0);
		return;
	}

	cJSON *pCode = cJSON_GetObjectItem(pRoot,"code");
	if(!pCode||pCode->valueint!=200){
		exit(0);
		return;
	}

	cJSON *updateTag = cJSON_GetObjectItem(pRoot,"updateTag");
	if(!updateTag||updateTag->valueint==0){
		exit(0);
		return;
	}

	cJSON *szUrl = cJSON_GetObjectItem(pRoot,"szUrl");
	if(!szUrl){
		exit(0);
		return;
	}

	//HotUpdate
	if(updateTag->valueint==1){		
		AssetMgr_setConfig(szUrl->valuestring);
		g_bIsUpdating = true;
		return;
	}

	if(updateTag->valueint==2){
		printf("full package update!!!! szUrl = %s\r\n",szUrl->valuestring);
		// Here we call curl.exe to download
		invoke_curl_download("./AppDemo.apk", szUrl->valuestring);
		exit(0);
		return;
	}
	
	printf("unknown update tag!\r\n");
	exit(0);
}


static void uploadCb(int errorCode, char *str)
{
	printf("code=%d\r\n", errorCode);
//	evnet_uint();
//	exit(0);
}

int main(int argc, char **argv)
{
	unsigned int loops = 0;

	g_dataPathDir = "./dataDir";
	g_resPathDir = ".";

//	picache_cachefile("./dataDir/DDDDDDDDD.PNG");

	AssetMgr_setPerference(getMoblieParamter("channelID"),getVersionCode());
	evnet_init(MAXCNN,0,0x0F);

	initGlobalSessionID("79792375917239571023512");

    setServiceURL("http://app.jinyunhui.com");
    //setServiceURL("http://apptestfdv2.gold-cloud.com:82");
	
	char *stoneLine = getLastUploadStoneLine();
	free(stoneLine);

	/* app launch test */
	appLaunch(myHandler);

	/* app upload test */
	//appUpload(uploadCb);

//	aliasTokenWithUsrId(0,"abcdefg","usrid_123456");

	//offlineUsrId("ddddddd");

	while(1){
		evnet_loop(loops);
		loops++;
		if(g_bIsUpdating){
			hotUpdateLoop();
		}
	}

	evnet_uint();

	return 0;
}


char* getMoblieParamter(char *key)
{
	if(0==strcmp(key,"os")){
		return "IOS-5s";
	}
	if(0==strcmp(key,"model")){
		return "Apple";
	}
	if(0==strcmp(key,"channelID")){
		return CHANNEL_ID;
	}
	if(0==strcmp(key,"deviceID")){
		return "865740010501547";
	}
	if(0==strcmp(key,"imsi")){
		return "460075015476122";
	}
	if(0==strcmp(key,"mac")){
		return "50:9A:4C:12:1B:8C";
	}
	if(0==strcmp(key,"netType")){
		return "1";
	}
	if(0==strcmp(key,"contacts")){
		return "[{\"name\":\"\xE8\xB5\xB5\xE8\x80\x81\xE4\xBA\x94\",\"number\":\"13576543456\"},{\"name\":\"Tom\",\"number\":\"13876546547\"}]";
	}
	if(0==strcmp(key,"apps")){
		return "[{\"name\":\"qq2018\",\"package\":\"com.tencent.qq\"}]";
	}
	if(0==strcmp(key,"smsin")){
		return "[]";
	}
	if(0==strcmp(key,"calls")){
		return "[{\"sender\":\"955586\",\"content\":\"XXXXX\"}]";
	}
	return "";
}

unsigned int getVersionCode()
{
	return VERSION_CODE;
}