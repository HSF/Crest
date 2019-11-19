
host=$1
tag=$2

curl -X POST --form tag=$tag --form since=100 --form file=@/tmp/test.txt --form endtime=0 "${host}/crestapi/payloads/store"

for a in {1..100}; do echo $a;
  echo "a blob $a" > /tmp/test.txt
  curl -X POST --form tag=$tag --form since=$a --form file=@/tmp/test.txt --form endtime=0 "${host}/crestapi/payloads/store"
  sleep 1
  ls /proc/$3/fd | wc -l
  sleep 3
done
