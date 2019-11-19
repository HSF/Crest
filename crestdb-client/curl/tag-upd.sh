generate_post_data()
{
  cat <<EOF
{
  "description": "A new tag for testing",
  "endOfValidity": 1000,
  "lastValidatedTime": 1000
}
EOF
}
host=$1
tag=$2
tagdata="$(generate_post_data)"
echo "Use data $tagdata"
curl -X PUT -H "Accept: application/json" -H "Content-Type: application/json" "${host}/crestapi/tags/${tag}" --data "${tagdata}"
echo "Try to get back data from server"
curl -X GET "${host}/crestapi/tags?by=name:${tag}"
