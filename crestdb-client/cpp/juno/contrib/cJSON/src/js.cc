/*
   See cjs- for building 
  
  pfx=$(cjs-prefix) && clang js.cc jstest.cc -lstdc++ -I$pfx/include -L$pfx/lib -lcJSON -Wl,-rpath,$pfx/lib -o $LOCAL_BASE/env/bin/js && js out.js

*/

#include "cJSON/js.hh"

#include <cassert>
#include <cstdlib>
#include <stddef.h>
#include <stdio.h>
#include <sstream>
#include <vector>
#include <string>
#include <cstring>
#include <algorithm>

#include "cJSON/cJSON.h"
#include "cJSON/common.h"

// functions defined at tail
char* slurp( const char* filename );
void split( std::vector<std::string>& elem, const char* line, char delim );


// statics

const char* JS::INTEGER_TYPE = "integer" ;
const char* JS::FLOAT_TYPE = "real" ;
const char* JS::STRING_TYPE = "text" ;
const char* JS::BLOB_TYPE = "blob" ;
const char* JS::SENTINEL = "COLUMNS" ;

JS* JS::Load(const char* path)
{
    char* text = slurp(path);
    if(!text) return NULL ;
    JS* js = new JS(text);
    free(text);
    return js ; 
}

// lifecycle

JS::JS(const char* text) : m_root(NULL), m_verbosity(0)
{
    assert(text);
    m_root = cJSON_Parse(text);
    Analyse();
}

JS::~JS()
{
    cJSON_Delete(m_root);
}

//  primary operations

void JS::AddMap(const char* name, Map_t& map)
{
    cJSON* obj = cJSON_CreateObject();
    for(Map_t::iterator it=map.begin(); it != map.end() ; ++it ) AddKV(obj, it->first.c_str(),it->second.c_str()) ;
    cJSON_AddItemToObject(m_root,name,obj);
    Analyse();
}



Map_t JS::CreateSubMap(const char* wanted)
{
    return CreateMap('r', NULL, wanted);
}

Map_t JS::CreateRowMap(const char* columns)
{
    return CreateMap('r', columns, NULL);
}

Map_t JS::CreateTypeMap(const char* columns)
{
    return CreateMap('t', columns, NULL);
}

Map_t JS::CreateMap(char form, const char* columns, const char* wanted)
{
   std::vector<std::string> cols ;
   if(columns) split(cols, columns, ','); 

    Map_t xmap ; 
    for(Map_t::iterator it=m_map.begin() ; it != m_map.end() ; it++ )
    {
        const char* key = it->first.c_str();
        const char* val = it->second.c_str();
        char look = LookupType(key);
        const char* type = TypeName(look);
        const char* name = strrchr(key, '/') + 1;  // keys are full paths within the js tree, this plucks just the basename

        bool select = true ;
        if(!cols.empty())
        {
            select = std::find(cols.begin(), cols.end(), name) != cols.end() ;
        } 

        if(wanted)
        {  
            if(wanted[0] == '/') // absolute: match all "wanted" from start 
            {  
                select = strncmp( key, wanted, strlen(wanted)) == 0  ;
            }
            else   // relative: match "wanted" from end
            {
                select = strncmp( key + strlen(key) - strlen(wanted), wanted, strlen(wanted)) == 0  ;
            }
        }


        if(!select)
        {
           continue;
        }


        switch(form)
        {
           case 't':
                    xmap[std::string(name)] =  std::string(type);
                    break;
           case 'r':
                    xmap[std::string(name)] =  std::string(val);
                    break;
        }
    }   

    if(xmap.size() != m_map.size() && columns == NULL && wanted == NULL)
    {
        //printf("JS::CreateMap [%c] xmap %zu m_map %zu map size mismatch \n", form, xmap.size(),m_map.size() );
        DumpMap(xmap,  "xmap"); 
        DumpMap(m_map, "m_map"); 
    }
    return xmap ;
} 







// secondary operations

void JS::DumpMap(Map_t& map, const char* msg)
{
    printf("JS::DumpMap %s \n", msg);
    for(Map_t::iterator it=map.begin() ; it != map.end() ; it++ )
    {
        const char* key = it->first.c_str();
        const char* val = it->second.c_str();
        printf(" %20s : %s \n", key, val );
    }
}

void JS::Print(const char* msg)
{
    printf("%s\n", msg);
    if(!m_root) return ;

    char *out = cJSON_Print(m_root);
    printf("%s\n",out);
    free(out);
}

std::string JS::AsString(bool pretty)
{
    char *out = pretty ? cJSON_Print(m_root) : cJSON_PrintUnformatted(m_root) ;
    std::string str(out);
    free(out);
    return str; 
}

void JS::PrintToFile(const char* _path)
{
    char* path = strdup(_path);
    char* base = basepath(path, '/');  // path upto last '/'
    int mode = 0777 ;
    mkdirp( base, mode ); 

    //if(rc){
    //    fprintf(stderr, "JS::PrintToFile mkdirp failed for base dir: [%s] exists already? \n", base);
    //} 

    char *out = cJSON_Print(m_root);
    FILE* fp=fopen(_path,"w");
    if(!fp){
        fprintf(stderr, "JS::PrintToFile failed to open for writing:  %s \n", path);
        return ;
    }
    //printf("JS::PrintToFile %s\n",path);
    fprintf(fp,"%s\n", out);
    fclose(fp);
    free(out);
    free(path); 
}

void JS::Traverse(const char* wanted)
{
   // used by interative JSON dumper : "which js"
    ClearMap(m_map);
    Recurse(m_root, "", wanted, m_map, 1 );
    PrintMap("JS::Traverse", m_map );
}

void JS::PrintMap(const char* msg) 
{
    // m_map only contains typed qtys, ie ones named in COLUMNS sentinel fields
    PrintMap(msg, m_map ); 
}

void JS::PrintMap(const char* msg, Map_t& map) 
{
    printf("%s\n", msg);
    for(Map_t::const_iterator it=map.begin() ; it != map.end() ; it++ )
    {
        const char* key = it->first.c_str();
        const char* val = it->second.c_str();
        char look = LookupType(key);
        const char* name = strrchr(key, '/') + 1;
        const char* type = TypeName(look);

        if(!name) name="" ;
        printf(" [%c]%10s %40s : %20s : %s \n", look,type, key, name, val );
    }
}


Map_t JS::GetRawMap(const char* wanted)
{
    Map_t raw ; 
    Recurse(m_root, "", wanted, raw, 2);
    return raw;
}



// tertiary operations


void JS::Demo()
{
    Map_t tmap = CreateTypeMap();
    Map_t rmap = CreateRowMap();

    DumpMap(tmap, "typemap");
    DumpMap(rmap, "rowmap");
}

void JS::SetVerbosity(int verbosity)
{
    m_verbosity = verbosity ;
}
int JS::GetVerbosity()
{
    return m_verbosity;
}



// high level internals :  convert from JSON tree into maps

void JS::Analyse()
{
   /*
      mode:0 
         initial traversal collecting all strings
         matching the sentinel into the selection map
   
      mode:1 
         subsequent traversal operates 
         based on the types found
   
   */

    // mode 0 : recurse to selects sentinel paths with type codes 
    ClearMap(m_map);
    Recurse(m_root, "", SENTINEL, m_map, 0);   
    ParseSentinels(m_map, m_type);   
    if(m_verbosity > 1) DumpMap(m_type, "m_type");

    // mode 1 : full tree recurse, collecting items with defined types into the map
    ClearMap(m_map);
    Recurse(m_root, "", "", m_map, 1 );  

}

void JS::ParseSentinels(Map_t& src, Map_t& dest)
{
    /*
        Sentinel m_map (key,val) entries like:: 

             "/parameters/COLUMNS" : "name:s,nphotons:i,nwork:i,nsmall:i,npass:i,nabort:i,nlaunch:i,tottime:f,maxtime:f,mintime:f"

        Are transformed into m_type entries

             "/parameters/name"      : "s"
             "/parameters/nphotons"  : "i"
             ...


    */
    for(Map_t::iterator it=src.begin() ; it != src.end() ; it++ )
    {
        const char* key = it->first.c_str();
        const char* val = it->second.c_str();

        size_t size = strlen(key) - strlen(SENTINEL) ; 
        std::string pfx(key, size );   

        std::vector<std::string> columns; 
        split(columns, val, ',' );     

        for(size_t c=0 ; c < columns.size() ; ++c)
        {
             std::vector<std::string> pair ; 
             split(pair, columns[c].c_str(), ':');
             assert(pair.size() == 2);

             std::string path(pfx);
             path += pair[0] ;
             dest[path] = pair[1] ;  // type code keyed by full path name 
        }          

        if(m_verbosity > 2) printf(" %40s : %20s : %s  \n", key, val, pfx.c_str() );
    }
}


// m_map manipulations

void JS::ClearMap(Map_t& map)
{
    map.clear();
}

void JS::AddMapKV( const char* key, const char* val )
{
    // replacing this with more flexible JS::Set
    std::string k(key);
    std::string v(val);
    m_map[k] = v ;
}

void JS::Set( Map_t& map, const char* key, const char* val )
{
    std::string k(key);
    std::string v(val);
    map[k] = v ;
}




Map_t JS::GetMap(const char* wanted)
{
    return wanted ? CreateSubMap(wanted) : m_map ;
}


// navigating JS tree


void JS::Visit(cJSON *item, const char* prefix, const char* wanted, Map_t& map, int mode)
{
    if(m_verbosity > 1) DumpItem(item, prefix);

    // just plucking sentinel strings ie COLUMNS with sqlite type info
    if(mode == 0 )  
    {
        if(item->type == cJSON_String ) Set(map, prefix, item->valuestring );
        return;
    }

    size_t size = 256 ;
    char* value = new char[size];

    if(mode == 1) // explicitly typed only 
    {

        char look = LookupType(prefix); // type char obtained from sentinel fields
        bool skip = false ; 

        switch(look)
        {
            case 'i':
                     snprintf(value, size,  "%d", item->valueint );
                     break;
            case 'f':
                     snprintf(value, size,  "%f", item->valuedouble );
                     break;
            case 's':
                     snprintf(value, size,  "%s", item->valuestring );
                     break;
            default:
                     skip = true ;
                     break;
                  
        }
        if(!skip) Set(map, prefix, value);
    }
    else if(mode == 2)
    {
        if(strlen(prefix) > strlen(wanted))  // skip the root of wanted object, just take content
        {
            const char* key = item->string ? item->string : "~" ;  // anonymous nodes like root have empty key  
            snprintf(value, size,  "%d", item->valueint );
            //printf("m2 prefix %s i %d f %f s %s \n", prefix, item->valueint, item->valuedouble, item->valuestring );  
            Set(map, key, value);
        }
    } 
    else
    {
        printf("mode ? %d \n", mode);
    }

    delete value ;
}


void JS::Recurse(cJSON *item, const char* prefix, const char* wanted, Map_t& map, int mode)
{
    while (item)
    {
        char* newprefix = NULL ; 
        const char* key = item->string ; 
        if(key)
        {
           newprefix = (char*)malloc(strlen(prefix)+strlen(key)+2);
           sprintf(newprefix,"%s/%s",prefix,key);
        }
        else
        {
           newprefix = (char*)malloc(strlen(prefix)+1);
           sprintf(newprefix,"%s",prefix);
        }

        bool match = false ;
        if(wanted[0] == '/') // absolute: match all "wanted" from start 
        {  
            match = strncmp( newprefix, wanted, strlen(wanted)) == 0  ;
        }
        else   // relative: match "wanted" from end
        {
            match = strncmp( newprefix + strlen(newprefix) - strlen(wanted), wanted, strlen(wanted)) == 0  ;
        }
        if(match) Visit(item, newprefix, wanted, map, mode);

        if(item->child) Recurse(item->child, newprefix, wanted, map, mode );
        item=item->next;
        free(newprefix);
    }
}


// adding to JS tree
void JS::SetKV(const char* name, const char* key, const char* val )
{
     cJSON* obj = cJSON_GetObjectItem(m_root, name);
     if(!obj)
     {
         obj = cJSON_CreateObject();
         cJSON_AddItemToObject(m_root,name,obj);
         //printf("JS::SetKV create top level object named %s \n", name);
     }
     AddKV(obj, key, val);
     Analyse();
}


/*
std::string JS::Get(const char* name, const char* key)
{
     std::string ret ;
     cJSON* obj  = cJSON_GetObjectItem(m_root, name);
     if(!obj) return ret ;

     cJSON* item = cJSON_GetObjectItem(obj, name);
     if(!item) return ret ;

     ret.assign(item->valuestring);
     return ret ;
}
*/



void JS::AddKV(cJSON* obj, const char* key, const char* val )
{
   // endptr pointing to null terminator means converted while string 
   {
      char* endptr;
      long int lval = strtol(val, &endptr, 10); 
      if(!*endptr)  
      {
          cJSON_AddNumberToObject(obj, key, lval );
          return ; 
      }
   }
   {
      char* endptr;
      double dval = strtod(val, &endptr); 
      if(!*endptr)  
      {
          cJSON_AddNumberToObject(obj, key, dval );
          return ; 
      }
   }
   cJSON_AddStringToObject(obj, key, val );
}

// lookups

char JS::LookupType(const char* path) const
{
    char ret = ' ' ;

    std::string key(path);
    Map_t::const_iterator end = m_type.end();
    Map_t::const_iterator ptr = m_type.find(key);

    if(ptr != end) ret = *ptr->second.c_str();
    return ret ;    
}

const char* JS::TypeName( char type) const
{
    const char* r = NULL ;
    switch( type )
    {
        case 'i':r = INTEGER_TYPE ;break;
        case 'f':r = FLOAT_TYPE ;break;
        case 's':r = STRING_TYPE ;break;
         default:r = BLOB_TYPE ;break; 
    }
    return r ;    
}

char JS::Type( int type) const
{
    char rc = '~' ;
    switch( type ){
        case cJSON_False:
        case cJSON_True  : rc='B';break;
        case cJSON_NULL  : rc='N';break; 
        case cJSON_Object: rc='O';break;
        case cJSON_Array : rc='A';break;
        case cJSON_Number: rc='F';break;
        case cJSON_String: rc='T';break;
        default:rc='?';break;
    }
    return rc ; 
}


// debug internals

void JS::DumpItem( cJSON* item, const char* prefix  )
{
    const char* key = item->string ? item->string : "~" ;  // anonymous nodes like root have empty key 
    char lookup = LookupType(prefix);
    char type = Type(item->type);
    printf("%c [%c] %40s %20s  ",type, lookup, prefix, key );
    switch( type ){
        case 'O':break;
        case 'A':break;
        case 'F':printf(" %d %f ", item->valueint, item->valuedouble); break ;
        case 'T':printf(" %s ", item->valuestring); break ;
        case 'B':printf(" %d ", item->valueint );break;
        case 'N':printf(" nul ");break;
        default:printf("?????: item->type %d ", item->type );
    }
    printf("\n");
}





// tail functions

char* slurp( const char* filename )
{
    FILE *f=fopen(filename,"rb");
    if(!f)
    {
        fprintf(stderr, "slurp: failed to open %s \n", filename);
        return 0 ;
    }

    fseek(f,0,SEEK_END);
    long len=ftell(f);
    fseek(f,0,SEEK_SET);

    char *data=(char*)malloc(len+1);
    fread(data,1,len,f);
    fclose(f);
    data[len] = '\0' ;

    return data;
}


void split( std::vector<std::string>& elem, const char* line, char delim )
{
    if(line == NULL) return ;
    std::istringstream f(line);
    std::string s;
    while (getline(f, s, delim)) elem.push_back(s);
}


