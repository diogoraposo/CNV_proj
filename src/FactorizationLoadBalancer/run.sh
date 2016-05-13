cp ../FactorizationDB/FactorizationDB_API.java .
cp ../FactorizationDB/FactorizationElement.java .
javac -cp "../lib/*" *.java -d bin/
java  -cp "../lib/*:bin/" FactorizationLoadBalancer.LoadBalancer
