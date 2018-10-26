#ifndef JS_COMMON_H
#define JS_COMMON_H

#ifdef __cplusplus
extern "C"
{
#endif


int mkdirp(char* _path, int mode);
char* basepath(char* _path, char delim );


#ifdef __cplusplus
}
#endif


#endif

