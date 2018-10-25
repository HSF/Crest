####
#### On my mac: /Users/formica/MyApp/Library/swagger/swagger/swagger-codegen
SWAGGER_CODEGEN="/Users/formica/MyApp/Library/swagger/swagger-codegen"
SWAGGER_CODEGEN="/Users/formica/MyApp/Library/swagger/fork-swagger/swagger-codegen"
SWAGGER_CODEGEN="/Users/formica/MyApp/Library/swagger/github/swagger-codegen"
executable="$SWAGGER_CODEGEN/modules/swagger-codegen-cli/target/swagger-codegen-cli.jar"
export JAVA_OPTS="${JAVA_OPTS} -Xmx1024M -DloggerPath=conf/log4j.properties"
ags="$@ generate -t $SWAGGER_CODEGEN/modules/swagger-codegen/src/main/resources/scala-gatling -i ../swagger_schemas/swagger/json/crestdb-gatling.json -l scala-gatling -o ./scala-gatling"
echo "$ags"
java $JAVA_OPTS -jar $executable $ags
