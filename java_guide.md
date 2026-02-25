# Log4Shell Lab – Java + Maven Demo

This project demonstrates a simple Java environment with a vulnerable Log4j setup for lab/testing purposes.  
It includes two demo classes:

- `demo.VulnerableServer` – Simulates a vulnerable server
- `demo.CallbackMonitor` – Observes callbacks from the vulnerable server

> **Disclaimer:** This is for **educational purposes only**. Do not run on production or public-facing systems.

---

## Prerequisites

- **Java 17 (LTS)**
- **Maven**

### Install on Ubuntu/Debian
```bash
sudo apt update
sudo apt install openjdk-17-jdk maven -y

Install on RHEL/Fedora

sudo dnf install java-17-openjdk-devel maven -y

Verify

java -version
javac -version
mvn -version

Set JAVA_HOME (if needed)

echo 'export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64' >> ~/.bashrc
echo 'export PATH=$JAVA_HOME/bin:$PATH' >> ~/.bashrc
source ~/.bashrc


⸻

Project Setup

Create the project using Maven:

mkdir -p ~/projects && cd ~/projects

mvn archetype:generate \
  -DgroupId=demo \
  -DartifactId=log4shell-lab \
  -DarchetypeArtifactId=maven-archetype-quickstart \
  -DarchetypeVersion=1.4 \
  -DinteractiveMode=false

cd log4shell-lab

# Remove default App.java
rm -rf src/main/java/demo/App.java


⸻

Project Structure

log4shell-lab/
├── pom.xml
└── src/main/java/demo/
        VulnerableServer.java
        CallbackMonitor.java


⸻

pom.xml

<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
         http://maven.apache.org/xsd/maven-4.0.0.xsd">

    <modelVersion>4.0.0</modelVersion>

    <groupId>demo</groupId>
    <artifactId>log4shell-lab</artifactId>
    <version>1.0-SNAPSHOT</version>

    <properties>
        <maven.compiler.source>17</maven.compiler.source>
        <maven.compiler.target>17</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    </properties>

    <dependencies>
        <!-- Log4j vulnerable version for lab -->
        <dependency>
            <groupId>org.apache.logging.log4j</groupId>
            <artifactId>log4j-core</artifactId>
            <version>2.14.1</version>
        </dependency>
        <dependency>
            <groupId>org.apache.logging.log4j</groupId>
            <artifactId>log4j-api</artifactId>
            <version>2.14.1</version>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.codehaus.mojo</groupId>
                <artifactId>exec-maven-plugin</artifactId>
                <version>3.1.0</version>
            </plugin>
        </plugins>
    </build>
</project>


⸻

Demo Classes

VulnerableServer.java

package demo;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Scanner;

public class VulnerableServer {
    private static final Logger logger = LogManager.getLogger(VulnerableServer.class);

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("VulnerableServer started. Type log messages:");
        while (true) {
            String input = scanner.nextLine();
            logger.info(input); // This is intentionally vulnerable
        }
    }
}

CallbackMonitor.java

package demo;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CallbackMonitor {
    private static final Logger logger = LogManager.getLogger(CallbackMonitor.class);

    public static void main(String[] args) {
        System.out.println("CallbackMonitor started. Listening for callbacks...");
        // Example placeholder: just prints periodically
        while (true) {
            logger.info("Monitoring callbacks...");
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                break;
            }
        }
    }
}


⸻

Build & Run

Build

mvn clean package

Run Vulnerable Server

mvn exec:java -Dexec.mainClass="demo.VulnerableServer"

Run Callback Monitor

mvn exec:java -Dexec.mainClass="demo.CallbackMonitor"


⸻

Useful Maven Commands

mvn clean           # Remove target/
mvn compile         # Compile code
mvn package         # Build JAR
mvn exec:java       # Run main class
mvn dependency:tree # Show dependency tree


⸻

Quick Start Checklist
	•	Java 17 installed
	•	Maven installed
	•	Project generated via Maven archetype
	•	pom.xml updated
	•	Demo classes added
	•	mvn clean package successful
	•	Run classes with mvn exec:java

---

✅ This **single Markdown file** now contains **everything needed**: setup instructions, Maven project, pom.xml, demo classes, build & run commands, and troubleshooting checklist.

If you want, I can **also add instructions for switching Java versions and managing dependencies directly in this same file**, so it becomes a **complete one-stop reference**.  

Do you want me to do that?
