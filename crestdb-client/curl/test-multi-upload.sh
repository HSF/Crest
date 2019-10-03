
##curl -X POST --form tag=SB_TAG-PYLD --form since=100 --form file=@/tmp/test.txt --form endtime=0 'http://localhost:8080/crestapi/payloads/store'

generate_post_data()
{
  cat <<EOF
{
  "niovs": 2,
  "format": "PYL",
  "resources":[
  { "since" : $since1, "payloadHash": "file:///tmp/test-01.txt"},
  { "since" : $since2, "payloadHash": "file:///tmp/test-02.txt"}
  ]
}
EOF
}
host=$1
tag=$2
for a in {1001..1010}; do echo $a;
  b=$((a + 1000))
  echo "another blob 1 is $a" > /tmp/test-01.txt
  echo "another blob 2 is $b" > /tmp/test-02.txt
  since1=$a
  since2=$b
  echo $(generate_post_data)
  resp=`curl --form tag=$tag --form endtime=0 --form iovsetupload="$(generate_post_data)"  --form "files=@/tmp/test-01.txt" --form "files=@/tmp/test-02.txt"  "${host}/crestapi/payloads/uploadbatch"`
  echo "Received response $resp"
  sleep 1
done
