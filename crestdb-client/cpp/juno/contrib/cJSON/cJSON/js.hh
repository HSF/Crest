#ifndef JS_H
#define JS_H

#include <string>
#include <map>
typedef std::map<std::string,std::string> Map_t ;
struct cJSON ;

class JS {

public:
   // statics
   static JS* Load(const char* path);

   static const char* INTEGER_TYPE ;
   static const char* FLOAT_TYPE ;
   static const char* STRING_TYPE ;
   static const char* BLOB_TYPE ;

   static const char* SENTINEL ;

public:
   // lifecycle
   JS(const char* text="{}");
   virtual ~JS();

public:
   // primary operations used by G4DAEMetadata


   void AddMap(const char* name, Map_t& map);  // causes a re-analysis
   Map_t CreateSubMap(const char* wanted);
   Map_t CreateRowMap(const char* columns=NULL);
   Map_t CreateTypeMap(const char* columns=NULL);
   Map_t CreateMap(char form, const char* columns=NULL, const char* wanted=NULL);
   std::string AsString(bool pretty=false);
   void SetKV(const char* name, const char* key, const char* val );
   //std::string Get(const char* name, const char* key);

   // for more flexible map handling 
   void Set(Map_t& map, const char* key, const char* val);

public:
   // secondary
   static void DumpMap(Map_t& map, const char* msg);
   void Print(const char* msg="JS::Print");
   void PrintToFile(const char* path);
   void Traverse(const char* wanted);
   void PrintMap(const char* msg="JS::PrintMap") ;
   void PrintMap(const char* msg, Map_t& map) ;
   Map_t GetRawMap(const char* wanted);

public:
   // tertiary 
   void Demo();
   void SetVerbosity(int verbosity);
   int GetVerbosity();
   Map_t GetMap(const char* wanted=NULL);

private:
   // high level internals :  convert from JSON tree into maps
   void Analyse(); 
   void ParseSentinels(Map_t& src, Map_t& dest);
   //void SetMode(int mode);
   //int GetMode();

private:
   // m_map manipulations
   void ClearMap(Map_t& map);
   void AddMapKV( const char* key, const char* val );

private:
   // navigating JS tree
   void Visit(cJSON *item, const char* prefix, const char* wanted, Map_t& map, int mode);
   void Recurse(cJSON* item, const char* prefix, const char* wanted, Map_t& map, int mode);

private:
   // adding to JS tree
   void AddKV(cJSON* obj, const char* key, const char* val );

private:
   // lookups
   char LookupType(const char* path) const;
   const char* TypeName( char type) const ;
   char Type( int type) const;

private:
   // debug internals
   void DumpItem( cJSON* item, const char* prefix  );

private: 
   // awkward to work with because too much state, and modes
   cJSON* m_root ; 
   Map_t m_map ;  
   Map_t m_type ;  
   int m_verbosity ;
   //int m_mode ;

};


#endif 

