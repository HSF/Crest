generate_post_data()
{
  cat <<EOF
{
  "description": "A new tag for testing",
   "endOfValidity": -1.0,
   "lastValidatedTime": -1.0,
   "name": "A-TEST-TAG-03",
   "payloadSpec": "JSON",
   "synchronization": "any",
   "timeType": "time"
}
EOF
}
host=$1
apiname=$2
tagdata="$(generate_post_data)"
apiname="crestapi"
echo "Use data $tagdata"
resp=`curl -X POST -H "Accept: application/json" -H "Content-Type: application/json" "${host}/${apiname}/tags" --data "${tagdata}"`
echo "Received response $resp"
echo "Try to get back data from server"
curl -X GET "${host}/${apiname}/tags?by=name:TAG"
