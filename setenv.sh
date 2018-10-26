inputfile=$1
echo "Setting properties from file $inputfile"
while read line; do echo $line; JAVA_OPTS="$JAVA_OPTS -D$line"; done < $inputfile
export JAVA_OPTS
export crest_dir=$PWD/crestdb-web/build/libs
