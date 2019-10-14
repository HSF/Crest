function json_data() {
  cat <<EOF
{ "" }
EOF
}

generate_globaltag_data()
{
  cat <<EOF
{
  "description": "A new global tag for testing",
  "validity": -1.0,
  "name": "GT-TAG-01",
  "release": "v1",
  "snapshotTime": 0,
  "scenario": "data challenge",
  "workflow": "some wf",
  "type": "test"
}
EOF
}

generate_tag_data()
{
  cat <<EOF
{
  "description": "A new tag for testing",
   "endOfValidity": -1.0,
   "lastValidatedTime": -1.0,
   "name": "$1",
   "payloadSpec": "JSON",
   "synchronization": "any",
   "timeType": "time"
}
EOF
}

function get_data() {
  echo "Execute $1 : get data of type $2 from server using search $3"

  resp=`curl -X GET -H "Accept: application/json" -H "Content-Type: application/json" "${host}/${apiname}/$2?by=$3"`
  echo "Received response "
  echo $resp | json_pp
}

function post_data() {
  pdata=$2
  resp=`curl -X POST -H "Accept: application/json" -H "Content-Type: application/json" "${host}/${apiname}/$1" --data "${pdata}"`
  echo "Received response "
  echo $resp | json_pp
}

function get_rndm_pyld() {
  rndfile=$1
  echo "generate 1MB of random payload data in file $rndfile"
  dd if=/dev/urandom of=$rndfile bs=1m count=10
}

generate_multi_upload_data()
{
  cat <<EOF
{
  "niovs": 2,
  "format": "PYL",
  "iovsList":[
  { "since" : $since1, "payload": "file:///tmp/test-01.txt"},
  { "since" : $since2, "payload": "file:///tmp/test-02.txt"}
  ]
}
EOF
}
function create_tag() {
  echo "Execute $1 : create tag $2 "
  tdata="$(generate_tag_data  $2)"
  echo "Upload ${tdata}"
  post_data "tags" "$tdata"
}

function multi_upload() {
  echo "Execute $1 : add data to tag $2 using an iov interval $3 to $4"
  tag=$2
  ST=$3
  END=$4
  get_rndm_pyld "/tmp/test-01.txt"
  get_rndm_pyld "/tmp/test-02.txt"
  for a in $(seq $ST 1 $END); do
    echo "generate insert for since $a";
    b=$((a + 1000))
    echo "another blob 1 is $a" >> /tmp/test-01.txt
    echo "another blob 2 is $b" >> /tmp/test-02.txt
    since1=$a
    since2=$b
    echo $(generate_multi_upload_data)
    resp=`curl --form tag="${tag}" --form endtime=0 --form iovsetupload="$(generate_multi_upload_data)"  --form "files=@/tmp/test-01.txt" --form "files=@/tmp/test-02.txt"  "${host}/${apiname}/payloads/uploadbatch"`
    echo "Post returned : $resp"
    #sleep 1
  done
}

###
# Main script
host=$1
apiname=$2
echo "Use host = $host apiname $apiname"
echo "Execute $3"
if [ "$host" == "help" ]; then
  echo "$0 <host> <apiname> <command>"
  echo "Use commands: get_data, create_tag, multi_upload.. "
  echo "get_data: <type> <search pattern>"
  echo "multi_upload: <tag> <iov-start> <iov-stop>"
  echo "create_tag: <tag>"
elif [[ "x$3" == "x" ]]; then
  echo "use arg help to get help."
else
  $3 "${@:3}"
fi
