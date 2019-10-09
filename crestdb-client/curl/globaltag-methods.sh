generate_post_data()
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
host=$1
tagdata="$(generate_post_data)"
apiname="api"

echo "Use data $tagdata"
curl -X POST -H "Accept: application/json" -H "Content-Type: application/json" "${host}/${apiname}/globaltags" --data "${tagdata}"
echo "Try to get back data from server"
curl -X GET "${host}/${apiname}/globaltags?by=name:GT"
