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



###Troubleshooting if no callback output 

```
# If it doesn't exist, create it now
mkdir -p src/main/resources

# Create the log4j2.xml file
cat > src/main/resources/log4j2.xml << 'EOF'
<?xml version="1.0" encoding="UTF-8"?>
<Configuration status="WARN">
    <Appenders>
        <Console name="Console" target="SYSTEM_OUT">
            <PatternLayout pattern="%d{HH:mm:ss.SSS} [%t] %-5level %logger{36} - %msg%n"/>
        </Console>
    </Appenders>
    <Loggers>
        <Root level="info">
            <AppenderRef ref="Console"/>
        </Root>
    </Loggers>
</Configuration>
EOF

# Verify it was created
cat src/main/resources/log4j2.xml

# Now rebuild
mvn clean compile

# Check if it's now in target/classes
ls -la target/classes/log4j2.xml
```



