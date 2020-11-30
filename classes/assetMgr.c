#include "libos.h"
#include "assetMgr.h"
#include "httpc.h"
#include "buffer.h"
#include "utils.h"


#define TIMEOUT (5)

enum{
	EFNone = 0,
	NeedUpdate
};


typedef struct resFile_s{
	struct resFile_s *next;
	char* NM; // name
	char* SI; // sign
	int ST; // state
	unsigned int SZ; // size
}resFile_t;


typedef struct resDocument_s{
	unsigned int Size;
	unsigned int verNum;
	resFile_t *pHead;
}resDocument_t;



static int g_Status = 0;
static int g_totalCount = 0;
static int g_curCount = 0;
static char *g_szCdnURL = NULL;
static bool g_isPrepared = false;
static bool g_trycount = 0;
static unsigned int g_remoteVerNum = 0;
static resDocument_t *g_remoteResDoc = NULL;
static resDocument_t *g_localResDoc = NULL;
static httpc_t g_httpc = {0};
extern char* g_dataPathDir;


static char* findString(char *buffer, const char *key, int *piLen) {	
	char *pST = strstr(buffer, key);
	if (NULL == pST) {
		return NULL;
	}
	pST = strstr(pST,":"); 
	if(NULL == pST){
		return NULL;
	}
	pST++;
	while(*pST==' '){
		pST++;
	}
	
	char *pED = strstr(pST, ",");
	if(NULL==pED){
		return NULL;
	}
	
	*pED = '\0';
	*piLen = pED-pST;	
	return pST;
}

void resDocument_free(resDocument_t *pDoc)
{
	while(pDoc->pHead){
		resFile_t *pFile = pDoc->pHead;
		pDoc->pHead = pFile->next;
		free(pFile->NM);
		free(pFile->SI);
		free(pFile);
	}
	free(pDoc);
}

static void resDocument_fillHead(resDocument_t *pDoc,char *pST, char *pED)
{	
	*pED = '\0'; //sub
	
	char *pszStr = NULL;
	int iLen = 0;

	pszStr = findString(pST,"Size",&iLen);
	pDoc->Size = (unsigned int)atoi(pszStr);
	pszStr[iLen]=',';

	pszStr = findString(pST,"VerNum",&iLen);
	pDoc->verNum = (unsigned int)atoi(pszStr);
	pszStr[iLen]=',';

	*pED = ']';
}


static void resDocument_appendFile(resDocument_t *pDoc,char *pST, char *pED)
{
	*pED = '\0'; //sub
	
	char *pszStr = NULL;
	int iLen = 0;

	resFile_t *pFile = (resFile_t*)malloc(sizeof(resFile_t));
	memset(pFile,0x00,sizeof(resFile_t));
	
	pszStr = findString(pST,"NM",&iLen);
	pFile->NM = cmmn_strdup(pszStr);
	pszStr[iLen]=',';
	
	pszStr = findString(pST,"SI",&iLen);
	pFile->SI = cmmn_strdup(pszStr);
	pszStr[iLen]=',';

	pszStr = findString(pST,"ST",&iLen);
	pFile->ST = (unsigned int)atoi(pszStr);
	pszStr[iLen]=',';

	pszStr = findString(pST,"SZ",&iLen);
	pFile->SZ = (unsigned int)atoi(pszStr);
	pszStr[iLen]=',';

	pFile->next = pDoc->pHead;
	pDoc->pHead = pFile;
	
	*pED = '}';
}

static void resDocument_fillBody(resDocument_t *pDoc,char *pszST, char *pszED)
{
	char *pST = NULL;
	char *pED = pszST;
	
	*pszED = '\0';
	while(1){
		pST = strstr(pED, "{");
		if(!pST){
			break;
		}
		pED = strstr(pST,"}");
		if (!pED){
			break;
		}
		resDocument_appendFile(pDoc,pST,pED);
		pED += 1;
	}
	*pszED = ']';
}

static resDocument_t *buffer2resDocument(char *pszBuffer, int iLen)
{
	resDocument_t *pDoc = (resDocument_t*)malloc(sizeof(resDocument_t));
	memset(pDoc,0x00,sizeof(resDocument_t));
	
	//head
	char *pST = strstr(pszBuffer,"Head:[");
	char *pED = NULL;
	if(!pST){
		resDocument_free(pDoc);
		return NULL;
	}

	pST+=strlen("Head:[");//skip
	SkipSpace(pST);
	pED = strstr(pST,"]");
	if(!pED){
		resDocument_free(pDoc);
		return NULL;
	}		
	resDocument_fillHead(pDoc,pST,pED);


	//body
	pST = strstr(pED+1,"Body:[");
	if(!pST){
		resDocument_free(pDoc);
		return NULL;
	}
	pST+=strlen("Body:[");//skip
	SkipSpace(pST);
	pED = strstr(pST,"]");
	if(!pED){
		resDocument_free(pDoc);
		return NULL;
	}		
	resDocument_fillBody(pDoc,pST,pED);

	return pDoc;
}

static void serverVerNumCb(void *pUsr, coutputer_t *pOut, int errorCode)
{
	if(errorCode!=0){
		DBGPRINT(EERROR,("[Trace@AssetMgr] Error: serverVerNumCb\r\n"));
		g_Status = GetServerVersion;
		return;
	}
	g_remoteVerNum = atoi(pOut->buffer.data);
	g_isPrepared = true;
}

static void initServerVerNumWithURL(char *pszURL)
{
	httpc_load(&g_httpc,pszURL,HTTP_GET,TIMEOUT,NULL,0,EOUT_BUFF,NULL,serverVerNumCb,NULL);
}


void initLocalNumWithFileName(char *filename)
{
	char *pszBuffer = NULL;
	int iLen = 0;
	Asset_file2buffer(filename,&pszBuffer,&iLen);
	if(!pszBuffer){
		pszBuffer = cmmn_strdup("{Head:[{Size:0,VerNum:0,}]Body:[]");
		iLen = strlen(pszBuffer);
	}
	g_localResDoc = buffer2resDocument(pszBuffer,iLen);
	free(pszBuffer);
	if(!g_localResDoc){
		g_Status = EError;
		return;
	}
}

static void serverIniCb(void *pUsr, coutputer_t *pOut, int errorCode)
{
	if(errorCode!=0){
		g_Status = LoadServerIni;
		return;
	}
	g_remoteResDoc = buffer2resDocument(pOut->buffer.data,pOut->buffer.size);
	if(!g_remoteResDoc){
		g_Status = EError;
		return;
	}
	//maybe server config error
	if(g_remoteResDoc->verNum!=g_remoteVerNum){
		g_remoteVerNum = g_remoteResDoc->verNum;
		if(g_localResDoc->verNum >= g_remoteVerNum){
			g_Status = EndLoading;			
		}
		return;
	}
	g_isPrepared = true;
}

static void initServerIniWithUrl(char *pszURL)
{
	httpc_load(&g_httpc,pszURL,HTTP_GET,TIMEOUT,NULL,0,EOUT_BUFF,NULL,serverIniCb,NULL);
}

static void setLocalVersion(unsigned int verNum)
{
	g_localResDoc->verNum = verNum;
	// save to flash disk.... for read version easily
	char buffer[64] = {0};
	sprintf(buffer,"%u",verNum);
	buffer2file(buffer,strlen(buffer),"vernum.ini");
}

static unsigned int getLocalVersion()
{
	char *buffer = NULL;
	int len = 0;
	file2buffer("vernum.ini",&buffer,&len);
	if(!buffer){
		return 0;
	}
	unsigned int versionCode = (unsigned int)atoi(buffer);
	free(buffer);
	return versionCode;
}

static bool saveLocalConfig()
{
	static char szLine[1024] = {0};
	
	buffer_t buffer = {0};
	buffer_init(&buffer);
	
	sprintf(szLine,"Head:[{Size:%u, VerNum:%u,}]\r\nBody:[",g_localResDoc->Size,g_localResDoc->verNum);
	buffer_append(&buffer,(char*)szLine,strlen(szLine));	
	resFile_t *pFile = g_localResDoc->pHead;
	while(pFile){
        if(pFile->ST==EFNone){ //we only save EFNone to local files.ini
            sprintf(szLine,"\r\n{NM:%s, SD:/, SI:%s, ST:%d, SZ:%u,}",pFile->NM,pFile->SI,pFile->ST,pFile->SZ);
            buffer_append(&buffer,(char*)szLine,strlen(szLine));
        }				
		pFile = pFile->next;
	}
	buffer_append(&buffer,"\r\n]",strlen("\r\n]"));
			
	buffer2file(buffer.data,buffer.size,"files.ini");	
	buffer_deinit(&buffer);
	
	return true;
}

static void serverFileCb(void *pUsr, coutputer_t *pOut, int errorCode)
{
	if(errorCode!=0){
		DBGPRINT(EERROR,("[Trace@AssetMgr] Error: serverFileCb\r\n"));
		g_Status = StartLoading;
		return;
	}

	resFile_t *pFile = (resFile_t*)pUsr;
	pFile->ST = EFNone;	

	saveLocalConfig();

	g_curCount++;
	g_isPrepared = true;
}

static char* getLocalFilePathName(char *NM)
{
	static char pathName[256] = {0};

	sprintf(pathName,"%s/%s",g_dataPathDir,NM);
	//check path
	char *tp = strrchr(pathName,'/');
	if(tp){
		char tmp = *(tp+1);
		*(tp+1) = '\0'; // replace
		cmmn_mkdir(pathName);
		*(tp+1) = tmp;  // replace back
	}
	return pathName;
}


static void localRequestUrlFile()
{
	resFile_t *pFile = g_localResDoc->pHead;
	while(pFile){
		if(pFile->ST == NeedUpdate){
			break;
		}
		pFile = pFile->next;
	}
	
	if(!pFile){
		g_isPrepared = true;
		return;
	}
	
    char *pathName = getLocalFilePathName(pFile->NM);	
	httpc_load(&g_httpc,fmt2("%s%s",g_szCdnURL,pFile->NM),HTTP_GET,TIMEOUT,NULL,0,EOUT_FILE,pathName,serverFileCb,(void*)pFile);
}

static void resDocument_refreshFile(resDocument_t *pDoc, char *NM, char *SI, unsigned int SZ)
{
	resFile_t *pFile = pDoc->pHead;
	while(pFile){
		if(0==strcmp(pFile->NM,NM)){
			break;
		}
		pFile = pFile->next;
	}
	
	if(pFile){
		if(strcmp(pFile->SI,SI)){
			pFile->SZ = SZ;
			free(pFile->SI); // free
			pFile->SI = cmmn_strdup(SI);			
			pFile->ST = NeedUpdate;
		}else{
			pFile->ST = EFNone;
		}		
	}else{
		pFile = (resFile_t*)malloc(sizeof(resFile_t));
		memset(pFile,0x00,sizeof(resFile_t));
		pFile->NM = cmmn_strdup(NM);
		pFile->SI = cmmn_strdup(SI);
		pFile->SZ = SZ;
		pFile->ST = NeedUpdate;
		pFile->next = pDoc->pHead;
		pDoc->pHead = pFile;
	}

	if(pFile->ST == NeedUpdate){
		g_totalCount++;
	}
}

static void mergeLocalDocFiles()
{
	int patchFileCount = 0;
	int totalBytes = 0;	
	resFile_t *pFile = NULL;

	g_totalCount = 0;
	g_curCount = 0;
	
	pFile = g_localResDoc->pHead;
	while(pFile){
		pFile->ST = EFNone;
		pFile = pFile->next;
	}
	
	pFile = g_remoteResDoc->pHead;
	while(pFile){
		resDocument_refreshFile(g_localResDoc,pFile->NM,pFile->SI,pFile->SZ);
		pFile = pFile->next;
	}	

	saveLocalConfig();
}

void AssetMgr_update()
{
	switch(g_Status){
	case GetServerVersion:
		if(!g_szCdnURL){
			DBGPRINT(EERROR,("[Trace@AssetMgr] Error: gszCdnURL is NULL!!!!\r\n"));
			break;
		}
		g_Status = InGetServerVersion;
		g_isPrepared = false;			
		initServerVerNumWithURL(fmt2("%svernum.ini",g_szCdnURL));
		break;
	case InGetServerVersion:
		if(!g_isPrepared){
			break;
		}
		g_Status = CheckServerVersion;
		break;
	case CheckServerVersion:
		initLocalNumWithFileName("files.ini");
		if(g_localResDoc->verNum < g_remoteVerNum){
            DBGPRINT(EERROR,("[Trace@AssetMgr] NOT Equal, localDocVerNum=%d,g_remoteVerNum=%d\r\n",g_localResDoc->verNum,g_remoteVerNum));
			g_Status = LoadServerIni;
		}else{
            g_Status = EndLoading;
		}
		break;
	case LoadServerIni:
		g_Status = CheckServerIni;
		g_isPrepared = false;
		initServerIniWithUrl(fmt2("%sfiles.ini",g_szCdnURL));
		break;
	case CheckServerIni:
		if(!g_isPrepared){
			break;
		}
		g_Status = CheckUpdate;
		break;
	case CheckUpdate:
		mergeLocalDocFiles();
		resDocument_free(g_remoteResDoc);
		g_remoteResDoc = NULL;
		g_Status = StartLoading;
		break;
	case StartLoading:
		g_isPrepared = false;
		g_Status = InLoading;
		localRequestUrlFile();
		if(!g_isPrepared){
			break;
		}
		setLocalVersion(g_remoteVerNum);
		saveLocalConfig();
		resDocument_free(g_localResDoc);
		g_localResDoc = NULL;
		g_Status = EndLoading;
		break;
	case InLoading:
		if(!g_isPrepared){
			break;
		}
		g_Status = StartLoading;
		break;
	default:
		break;
	}
}


void AssetMgr_setConfig(char *pszURL)
{	
	g_Status = GetServerVersion;
	g_trycount = 0;
	g_totalCount = 0;
	g_curCount = 0;
	g_szCdnURL = cmmn_strdup(pszURL);
	g_isPrepared = false;
	g_remoteVerNum = 0;
	g_remoteResDoc = NULL;
	g_localResDoc = NULL;
}

int AssetMgr_getStatus()
{
	return g_Status;
}

int AssetMgr_totalCount()
{
	return g_totalCount;
}

int AssetMgr_curCount()
{
	return g_curCount;
}

void AssetMgr_clear()
{
	httpc_clear(&g_httpc,false);
	g_Status = EError;
}

void AssetMgr_removeALL()
{
	remove(fmt2("%s/vernum.ini",g_dataPathDir));
	remove(fmt2("%s/files.ini",g_dataPathDir));
	remove(fmt2("%s/index.html",g_dataPathDir));
	cmmn_rmdir(fmt2("%s/static",g_dataPathDir));
}

/*
 Very very important! The hot update is according the channel and the app version
 If either of them changed, we must remove all updated things!
 */
#define HOTUPDATE_FILENAME "hotUpdate.dat"
void AssetMgr_setPerference(char *channelId, unsigned int versionCode)
{
	char *pszBuffer = NULL;
	int len = 0;
	file2buffer(HOTUPDATE_FILENAME,&pszBuffer,&len);

	char szPerference[64] = {0};
	sprintf(szPerference,"%s%u",channelId,versionCode);
	if(pszBuffer&&0==strcmp(pszBuffer,szPerference)){
		return;
	}
	DBGPRINT(EMSG,("[Trace@AssetMgr] removeALL (versionCode=%d),szPerference=%s\r\n",versionCode,szPerference));
	AssetMgr_removeALL();
	buffer2file(szPerference,strlen(szPerference),HOTUPDATE_FILENAME);
}

unsigned int AssetMgr_combinedVersionCode()
{
	unsigned int versionCode = getLocalVersion();
	if(0!=versionCode){
		DBGPRINT(EMSG,("[Trace@AssetMgr] combinedVersionCode=%u\r\n",versionCode));
		return versionCode;
	}
	DBGPRINT(EMSG,("[Trace@AssetMgr] use Default Version\r\n"));
	return getVersionCode();//UseDefault
}
