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

## 1. Start the vulnerable server:                                               

```
mvn exec:java -Dexec.mainClass=demo.VulnerableServer                          
```

## 2. In a separate terminal, start the callback monitor:                        
```
mvn exec:java -Dexec.mainClass=demo.CallbackMonitor
```
