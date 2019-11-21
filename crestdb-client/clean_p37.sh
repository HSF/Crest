find "${OUTPUT_DIR}/" -type f -name \*.py -exec sed -i '' s/async=/async_req=/g {} +
find "${OUTPUT_DIR}/" -type f -name \*.py -exec sed -i '' s/async\ bool/async_req\ bool/g {} +
find "${OUTPUT_DIR}/" -type f -name \*.py -exec sed -i '' s/\'async\'/\'async_req\'/g {} +
sed -i '' s/if\ not\ async/if\ not\ async_req/g ${OUTPUT_DIR}/api_client.py
