
curl -X POST --form tag=SB_TAG-PYLD --form since=100 --form file=@/tmp/test.txt --form endtime=0 'http://localhost:8080/crestapi/payloads/store'

for a in {1..100}; do echo $a;
  echo "a blob $a" > /tmp/test.txt
  curl -X POST --form tag=AF_TEST_2019 --form since=$a --form file=@/tmp/test.txt --form endtime=0 'http://localhost:8080/crestapi/payloads/store'
  sleep 1
  ls /proc/$1/fd | wc -l
  sleep 3
done
