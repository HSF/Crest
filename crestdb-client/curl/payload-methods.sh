generate_post_data()
{
  cat <<EOF
{
  "hash" : "somehash",
  "version" : "my-obj-v0",
  "objectType": "test-obj",
  "data" : "ZW5jb2RlZGI2NA==",
  "streamerInfo" : "ZW5jb2RlZHN0cmVhbWVy",
  "size" : 10
}
EOF
}
host=$1
pylddata="$(generate_post_data)"
apiname="api"
echo "Use data $pylddata"
curl -X POST -H "Accept: application/json" -H "Content-Type: application/json" "${host}/${apiname}/payloads" --data "${pylddata}"
echo "Try to get back data from server"
curl -H "X-Crest-PayloadFormat: DTO" -X GET "${host}/${apiname}/payloads/somehash"
