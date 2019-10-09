### tagmeta: tag_name | description | chansize | colsize | channelInfo | payloadInfo
generate_post_data()
{
  cat <<EOF
{
  "description": "A new tag meta for testing",
  "chansize": 2,
  "colsize": 1,
  "tagName": "A-TEST-TAG-02",
  "payloadInfo": "someinfo",
  "channelInfo": "[{\"100\":\"aaaa\"}]"
}
EOF
}
host=$1
tagname="A-TEST-TAG-02"
tagdata="$(generate_post_data)"
apiname="crestapi"
echo "Use data $tagdata"
curl -X POST -H "Accept: application/json" -H "Content-Type: application/json" "${host}/${apiname}/tags/${tagname}/meta" --data "${tagdata}"
echo "Try to get back data from server"
#curl -X GET "${host}/${apiname}/tags/${tagname}/meta"
