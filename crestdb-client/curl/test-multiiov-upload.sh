
##curl -X POST --form tag=SB_TAG-PYLD --form since=100 --form file=@/tmp/test.txt --form endtime=0 'http://localhost:8080/crestapi/payloads/store'
host=$1
tagdest=$2

# generate_post_data()
# {
#   cat <<EOF
# {"size":4,"format":"HASH","filter": { "tagName" : "$tagdest" },"resources":[
# {"tagName":"$tagdest","since":1,"insertionTime":"2019-09-30T13:16:09.282+0000","payloadHash":"62ff533cdc8a65bbdc0216354798c12d429346c12afa40272722c47709ef4cce"},
# {"tagName":"$tagdest","since":2,"insertionTime":"2019-09-30T13:16:13.345+0000","payloadHash":"1213402cf2613692b541b6bf92985652c86e92b51b69b0cdad6ac6961269130f"},
# {"tagName":"$tagdest","since":3,"insertionTime":"2019-09-30T13:16:17.418+0000","payloadHash":"b7f9ed93ada5bf8d653756de9d97773ad5e07e4f5b1d45acd15ddb90f625fcdc"},
# {"tagName":"$tagdest","since":100,"insertionTime":"2019-09-30T13:16:09.233+0000","payloadHash":"1213402cf2613692b541b6bf92985652c86e92b51b69b0cdad6ac6961269130f"}]}
# EOF
# }
generate_post_data()
{
  cat <<EOF
{"size":400,"format":"HASH","filter": { "tagName" : "$tagdest" },"resources":[
{"since":401,"insertionTime":"2019-09-30T13:16:09.282+0000","payloadHash":"62ff533cdc8a65bbdc0216354798c12d429346c12afa40272722c47709ef4cce"},
{"since":402,"insertionTime":"2019-09-30T13:16:13.345+0000","payloadHash":"1213402cf2613692b541b6bf92985652c86e92b51b69b0cdad6ac6961269130f"},
{"since":403,"insertionTime":"2019-09-30T13:16:17.418+0000","payloadHash":"b7f9ed93ada5bf8d653756de9d97773ad5e07e4f5b1d45acd15ddb90f625fcdc"},
{"since":404,"insertionTime":"2019-09-30T13:16:09.233+0000","payloadHash":"1213402cf2613692b541b6bf92985652c86e92b51b69b0cdad6ac6961269130f"}]}
EOF
}

iovset=$(generate_post_data)
echo "Insert multi iov $iovset in tag $tagdest"
resp=`curl -X POST -H "Accept: application/json" -H "Content-Type: application/json" "${host}/crestapi/iovs/storebatch" --data "${iovset}"`

echo "Received response $resp"
