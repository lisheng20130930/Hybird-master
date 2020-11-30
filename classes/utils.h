#ifndef UTILITY_H
#define UTILITY_H


#include "listlib.h"
#include "libos.h"


#ifndef __min
#define __min(a,b) (((a) < (b)) ? (a) : (b))
#endif

#ifndef __max
#define __max(a,b) (((a) > (b)) ? (a) : (b))
#endif


#define Cmmn_isSpace(a)   ((a)==0x20 || ((a)==0x09))
#define SkipSpace(a) while((Cmmn_isSpace(*a)||0x0a ==*a || 0x0d == *a) && *a ){a++;}


extern char* getMoblieParamter(char *key);
extern unsigned int getVersionCode();



bool Asset_file2buffer(char *pszFileName, char **ppszBuff, int *piLen);
bool buffer2file(char *buffer, int len, char *pszFileName);
bool file2buffer(char *pszFileName, char **ppszBuff, int *piLen);
int cmmn_mkdir(char *directoryPath);
int cmmn_rmdir(char * dirname);
char *serviceApi(char *serviceAddr,char *apiName);


#endif