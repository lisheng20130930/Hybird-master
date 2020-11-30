#include "libos.h"
#include "listlib.h"
#include "utils.h"



bool buffer2file(char *buffer, int len, char *pszFileName)
{
	extern char* g_dataPathDir;
    static char realName[1024] = {0};
    sprintf(realName, "%s/%s", g_dataPathDir,pszFileName);
	remove(realName);
    FILE *pOUT = fopen(realName, "ab+");
	if(NULL==pOUT){
		DBGPRINT(EERROR, ("[Trace@Utils] Info: OpenFailure (%s)\r\n",realName));
        return false;
    }
    fwrite(buffer,len,1,pOUT);
    fclose(pOUT);
	return true;
}

bool file2buffer(char *pszFileName, char **ppszBuff, int *piLen)
{
	extern char* g_dataPathDir;
	static char realName[1024] = {0};
	sprintf(realName, "%s/%s", g_dataPathDir,pszFileName);
	
    FILE *pFile = NULL;    
    char *pcFileBuff = NULL;
    int iInLen = 0;
    
    pFile = fopen(realName, "rb");
    if(0 == pFile){
        return false;
    }
    
    fseek(pFile, 0, SEEK_END);    
    iInLen = (int)ftell(pFile);
    pcFileBuff = (char*)malloc(iInLen+1);
    memset(pcFileBuff, 0x00, iInLen+1);
    fseek(pFile, 0, SEEK_SET);    
    fread(pcFileBuff,iInLen, 1, pFile);    
    fclose(pFile);
    
    *ppszBuff = pcFileBuff;
	if(piLen)*piLen = iInLen;
    
    return true;
}

#ifndef __ANDROID__
static bool _Asset_file2buffer(char *pszFileName, char **ppszBuff, int *piLen)
{
	extern char *g_resPathDir;
    static char realName[1024] = {0};    
    sprintf(realName, "%s/%s", g_resPathDir,pszFileName);
    
    FILE *pFile = NULL;
    char *pcFileBuff = NULL;
    int iInLen = 0;
    
    pFile = fopen(realName, "rb");
    if(0 == pFile){
        return false;
    }
    
    fseek(pFile, 0, SEEK_END);
    iInLen = ftell(pFile);
    pcFileBuff = (char*)malloc(iInLen+1);
    memset(pcFileBuff, 0x00, iInLen+1);
    fseek(pFile, 0, SEEK_SET);
    fread(pcFileBuff,iInLen, 1, pFile);
    fclose(pFile);
    
    *ppszBuff = pcFileBuff;
    *piLen = iInLen;
    
    return true;
}

#else

#include <android/asset_manager_jni.h>
#include <android/asset_manager.h>

static AAssetManager * gAssetMgr = NULL;
static bool _Asset_file2buffer(char *pszFileName, char **ppszBuff, int *piLen)
{
	bool bRet = true;
	DBGPRINT(EMSG,("[Trace@Utils] Asset_file2buffer gAssetMgr=0x%x\r\n", gAssetMgr));
	char *pcFileBuff = NULL;
	do{
		DBGPRINT(EMSG,("[Trace@Utils] pszFileName=%s\r\n", pszFileName));
		AAsset * pAsset = AAssetManager_open(gAssetMgr, pszFileName, AASSET_MODE_UNKNOWN);
		if( pAsset == NULL ) {
			DBGPRINT(EMSG,("[Trace@Utils] ERROR1=%s\r\n", pszFileName));
			bRet = false;
			break;
		}
		size_t size = AAsset_getLength(pAsset);
		if( size > 0 )
		{
			pcFileBuff = (char*)malloc(size+1);
			int iRet = AAsset_read( pAsset, pcFileBuff, size);
			if( iRet <= 0 )
			{
				DBGPRINT(EMSG,("[Trace@Utils] ERROR22222\r\n"));
				free(pcFileBuff);
				pcFileBuff = NULL;
			}
		}else{
			DBGPRINT(EMSG,("[Trace@Utils] ERROR3333\r\n"));
		}
		AAsset_close(pAsset);
		
		if(!pcFileBuff||0==size){
			DBGPRINT(EMSG,("[Trace@Utils] ERROR2\r\n"));
			bRet = false;
			break;
		}
		*ppszBuff = pcFileBuff;
		*piLen = size;
	}while(0);
	
	return bRet;
}

void nativeSetAssetManager(JNIEnv* env, jobject thiz, jobject assetManager)
{
	DBGPRINT(EMSG,("[Trace@Utils] nativeSetAssetManager\r\n"));
	gAssetMgr = AAssetManager_fromJava( env, assetManager );
}
#endif

bool Asset_file2buffer(char *pszFileName, char **ppszBuff, int *piLen)
{
	// ==========Asset search-path =================
	// 1. data dir First
	// 2. assert dir
	//==============================================
	if(file2buffer(pszFileName,ppszBuff,piLen)){
		return true;
	}
	return _Asset_file2buffer(pszFileName,ppszBuff,piLen);
}



#ifndef WIN32
static int is_dir(char * filename)
{  
	struct stat buf;  
	int ret = stat(filename,&buf);  
	if(0 == ret){  
		if(buf.st_mode&S_IFDIR){
			return 0;  
		}else {
			return 1;  
		}
	}
	return -1; 
} 

int cmmn_rmdir(char * dirname)  
{
	char chBuf[256] = { 0 };  
	DIR * dir = NULL;  
	struct dirent *ptr;
	int ret = 0;  
	dir = opendir(dirname);
	if(NULL == dir){
		return -1;  
	}
	while((ptr = readdir(dir))!=NULL){
		ret = strcmp(ptr->d_name, ".");  
		if(0 == ret){
			continue;
		}
		ret = strcmp(ptr->d_name, "..");  
        if(0 == ret){
            continue;  
        }  
		snprintf(chBuf,256,"%s/%s",dirname,ptr->d_name);
		ret = is_dir(chBuf);  
		if(0 == ret) { 
			ret = cmmn_rmdir(chBuf);  
			if(0 != ret){  
				return -1;  
			}  
		}else if(1 == ret){  
			ret = remove(chBuf);  
			if(0 != ret){  
				return -1;  
			}
		}
	}  
	closedir(dir);	
    ret = remove(dirname);  
    if(0 != ret){  
        return -1;  
    }  
	return 0;
}  

#else
#include "windows.h"
int cmmn_rmdir(char *cFilePath)
{
	WIN32_FIND_DATA data;
	HANDLE hFind;
    char cFullPath[256] = { 0 };
    char cNewPath[256] = { 0 };
	sprintf(cFullPath,"%s\\*.*",cFilePath);
	hFind=FindFirstFile(cFullPath,&data);
	if(INVALID_HANDLE_VALUE != hFind){
		do{
			if((!strcmp(".",data.cFileName)) || (!strcmp("..",data.cFileName))){
				continue;
			}
			if(data.dwFileAttributes==FILE_ATTRIBUTE_DIRECTORY){
				sprintf(cNewPath,"%s\\%s",cFilePath,data.cFileName);
				cmmn_rmdir(cNewPath);
			}
			sprintf(cFullPath,"%s\\%s",cFilePath,data.cFileName);
			DeleteFile(cFullPath);
		}while(FindNextFile(hFind,&data));
		FindClose(hFind);
	}
	RemoveDirectoryA(cFilePath);
	return 0;
}
#endif

#ifdef WIN32
#define ACCESS(fileName,accessMode) access(fileName,accessMode)
#define MKDIR(path) mkdir(path)
#define F_OK  (0)
#else
#define ACCESS(fileName,accessMode) access(fileName,accessMode)
#define MKDIR(path) mkdir(path,S_IRWXU | S_IRWXG | S_IROTH | S_IXOTH)
#endif
int cmmn_mkdir(char *directoryPath)
{
    unsigned int dirPathLen = (unsigned int)strlen(directoryPath);
    char tmpDirPath[256] = { 0 };
    for(unsigned int i = 0; i < dirPathLen; ++i){
        tmpDirPath[i] = directoryPath[i];
        if(tmpDirPath[i] == '\\' || tmpDirPath[i] == '/'){
            if (ACCESS(tmpDirPath,F_OK) != 0){
                int ret = MKDIR(tmpDirPath);
				if(ret != 0){
                    return ret;
                }
            }
        }
    }
    return 0;
}

char *serviceApi(char *serviceAddr,char *apiName)
{
	static char _URL[256] = {0};
	sprintf(_URL,"%s%s",serviceAddr,apiName);
	return _URL;
}