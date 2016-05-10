javac -cp "../lib/*" FactorizationDB_API_sec.java FactorizationElement.java -d bin/
java -cp "../lib/*:bin/" FactorizationDB.FactorizationDB_API_sec > out
