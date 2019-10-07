generate_post_data()
{
  cat <<EOF
{
  "description": "A new tag for testing",
   "endOfValidity": -1.0,
   "lastValidatedTime": -1.0,
   "name": "A-TEST-TAG-02",
   "payloadSpec": "JSON",
   "synchronization": "any",
   "timeType": "time"
}
EOF
}
host=$1
tagdata="$(generate_post_data)"
apiname="api"

echo "Use data $tagdata"
curl -X POST -H "Accept: application/json" -H "Content-Type: application/json" "${host}/${apiname}/tags" --data "${tagdata}"
echo "Try to get back data from server"
curl -X GET "${host}/${apiname}/tags?by=name:MY"
