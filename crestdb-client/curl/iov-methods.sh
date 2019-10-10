generate_post_data()
{
  cat <<EOF
{
  "tagName": "A-TEST-TAG-02",
  "since": 150,
  "payloadHash": "somehash"
}
EOF
}
host=$1
iovdata="$(generate_post_data)"
apiname="api"
echo "Use data $iovdata"
curl -X POST -H "Accept: application/json" -H "Content-Type: application/json" "${host}/${apiname}/iovs" --data "${iovdata}"
echo "Try to get back data from server"
curl -X GET "${host}/${apiname}/iovs?tagname=A-TEST-TAG-02"
