while true; do 
cd ./bin
java tests.RandomNess $1 $2 > ../run.sh
cd ..
chmod 755 run.sh 
./run.sh
sleep 10
done
