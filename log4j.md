##Pre requisites

```
sudo apt update
sudo apt install openjdk-8-jdk

# Set Java 8 as default
sudo update-alternatives --config java
# Select the Java 8 option

# Verify
java -version
```

## Install dependancies 

```
mvn clean install 
```

## open 3 terminals 

###Terminal 1 

```
mvn exec:java -Dexec.mainClass=demo.VulnerableServer  
```
###Terminal 2

```
mvn exec:java -Dexec.mainClass=demo.CallbackMonitor
```

###Terminal 3



