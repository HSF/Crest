
##curl -X POST --form tag=SB_TAG-PYLD --form since=100 --form file=@/tmp/test.txt --form endtime=0 'http://localhost:8080/crestapi/payloads/store'

generate_post_data()
{
  cat <<EOF
{
  "niovs": 2,
  "format": "PYL",
  "resources":[
  { "since" : $since1, "payloadHash": "file:///tmp/newfile-01.txt"},
  { "since" : $since2, "payloadHash": "file:///tmp/newfile-02.txt"}
  ]
}
EOF
}

function get_rndm_pyld() {
  dd if=/dev/urandom of=/tmp/newfile.stuff bs=1m count=10
}

host=$1
apiname=$2
tag=$3

get_rndm_pyld

for a in {1001..1010}; do echo $a;
  b=$((a + 1000))
  echo "another blob 1 is $a" > /tmp/newfile-01.txt
  echo "another blob 2 is $b" > /tmp/newfile-02.txt
  cat /tmp/newfile.stuff >> /tmp/newfile-01.txt
  cat /tmp/newfile.stuff >> /tmp/newfile-02.txt

  since1=$a
  since2=$b
  echo $(generate_post_data)
  resp=`curl --form tag=$tag --form endtime=0 --form iovsetupload="$(generate_post_data)"  --form "files=@/tmp/newfile-01.txt" --form "files=@/tmp/newfile-02.txt"  "${host}/${apiname}/payloads/uploadbatch"`
  echo "Received response $resp"
  sleep 1
done
