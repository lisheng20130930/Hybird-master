#ifndef LOG_H
#define LOG_H



#if(defined(__IOS__)||defined(__ANDROID__))
#define LOGFILENAME log_filename("HJProject.log")
#else
#define LOGFILENAME "HJProject.log"
#endif


/* logger level */
enum{
    EDEBUG = 0x01,
	EMSG   = 0x02,
	EERROR = 0x04,
	ELEVEL = EERROR|EMSG
};

#define DBGPRINT(t,x)   {do{if(ELEVEL&(t)){_log x;}}while(0);}
#define DASSERT(x)  {do{if(!(x)){DBGPRINT(EERROR,("[Exception] Assert...\r\n"));}}while(0);}


void _log(char* pszFormat, ...);


#endif