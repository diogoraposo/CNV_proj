cp ../FactorizationDB/FactorizationDB_API.java .
cp ../FactorizationDB/FactorizationElement.java .
javac -cp "../lib/*" AutoScaler.java FactorizationElement.java FactorizationDB_API.java -d bin/
java  -cp "../lib/*:bin/" FactorizationAutoScaler.AutoScaler
