#ifndef ASSETMGR_H
#define ASSETMGR_H



enum{
	GetServerVersion = 0,
	InGetServerVersion,
	CheckServerVersion,
	LoadServerIni,
	CheckServerIni,
	LoadLocalIni,
	CheckUpdate,
	StartLoading,
	InLoading,
	EndLoading,
	EError,
};


void AssetMgr_setPerference(char *channelId, unsigned int versionCode);
unsigned int AssetMgr_combinedVersionCode();
void AssetMgr_setConfig(char *pszURL);
int  AssetMgr_totalCount();
int  AssetMgr_curCount();
int  AssetMgr_getStatus();
void AssetMgr_update();
void AssetMgr_clear();
void AssetMgr_removeALL();


#endif