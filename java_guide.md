# Java Installation & Project Setup Guide for Linux

## Table of Contents
1. [Installing Java on Linux](#installing-java-on-linux)
2. [Verifying Java Installation](#verifying-java-installation)
3. [Installing Maven](#installing-maven)
4. [Creating a New Java Project](#creating-a-new-java-project)
5. [Project Structure Explained](#project-structure-explained)
6. [Running Your Project](#running-your-project)
7. [Common Commands Reference](#common-commands-reference)

---

## Installing Java on Linux

### Option 1: Install Java 17 (Recommended - Latest LTS)

#### For Ubuntu/Debian:

```bash
# Update package index
sudo apt update

# Install Java 17 (OpenJDK)
sudo apt install openjdk-17-jdk -y

# Verify installation
java -version
javac -version
```

#### For RHEL/CentOS/Fedora:

```bash
# Update package manager
sudo dnf update -y

# Install Java 17
sudo dnf install java-17-openjdk-devel -y

# Verify installation
java -version
javac -version
```

---

### Option 2: Install Java 11 (LTS)

#### For Ubuntu/Debian:

```bash
# Update package index
sudo apt update

# Install Java 11
sudo apt install openjdk-11-jdk -y
```

#### For RHEL/CentOS/Fedora:

```bash
sudo dnf install java-11-openjdk-devel -y
```

---

### Option 3: Install Java 8 (For Legacy Applications)

#### For Ubuntu/Debian:

```bash
# Update package index
sudo apt update

# Install Java 8
sudo apt install openjdk-8-jdk -y
```

#### For RHEL/CentOS/Fedora:

```bash
sudo dnf install java-1.8.0-openjdk-devel -y
```

---

### Managing Multiple Java Versions

If you have multiple Java versions installed, you can switch between them:

```bash
# List all installed Java versions
sudo update-alternatives --config java

# You'll see output like:
# Selection    Path                                         Priority   Status
# ------------------------------------------------------------
#   0            /usr/lib/jvm/java-17-openjdk-amd64/bin/java   1711      auto mode
#   1            /usr/lib/jvm/java-11-openjdk-amd64/bin/java   1111      manual mode
# * 2            /usr/lib/jvm/java-8-openjdk-amd64/bin/java    1081      manual mode

# Enter the number to select your preferred version
```

---

## Verifying Java Installation

```bash
# Check Java Runtime version
java -version

# Expected output:
# openjdk version "17.0.x" 2024-xx-xx
# OpenJDK Runtime Environment (build 17.0.x+x-Ubuntu)
# OpenJDK 64-Bit Server VM (build 17.0.x+x-Ubuntu, mixed mode, sharing)

# Check Java Compiler version
javac -version

# Expected output:
# javac 17.0.x
```

### Set JAVA_HOME Environment Variable

```bash
# Find Java installation path
sudo update-alternatives --config java
# Note the path, e.g., /usr/lib/jvm/java-17-openjdk-amd64

# Open your bash profile
nano ~/.bashrc

# Add these lines at the end:
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
export PATH=$JAVA_HOME/bin:$PATH

# Save and exit (Ctrl+X, then Y, then Enter)

# Reload the profile
source ~/.bashrc

# Verify JAVA_HOME
echo $JAVA_HOME
```

---

## Installing Maven

Maven is a build automation tool for Java projects.

### Ubuntu/Debian:

```bash
# Install Maven
sudo apt install maven -y

# Verify installation
mvn -version
```

### RHEL/CentOS/Fedora:

```bash
# Install Maven
sudo dnf install maven -y

# Verify installation
mvn -version
```

### Manual Installation (Latest Version):

```bash
# Download Maven
cd /tmp
wget https://dlcdn.apache.org/maven/maven-3/3.9.6/binaries/apache-maven-3.9.6-bin.tar.gz

# Extract
sudo tar -xvzf apache-maven-3.9.6-bin.tar.gz -C /opt

# Create symlink
sudo ln -s /opt/apache-maven-3.9.6 /opt/maven

# Configure environment variables
nano ~/.bashrc

# Add these lines:
export M2_HOME=/opt/maven
export PATH=$M2_HOME/bin:$PATH

# Reload
source ~/.bashrc

# Verify
mvn -version
```

---

## Creating a New Java Project

### Method 1: Maven Quickstart (Recommended)

This creates a standard Java project structure.

```bash
# Navigate to your projects directory
cd ~/projects
# or create one if it doesn't exist
mkdir -p ~/projects && cd ~/projects

# Generate a new Maven project
mvn archetype:generate \
  -DgroupId=com.mycompany.app \
  -DartifactId=my-app \
  -DarchetypeArtifactId=maven-archetype-quickstart \
  -DarchetypeVersion=1.4 \
  -DinteractiveMode=false

# Navigate into the project
cd my-app
```

**Parameters Explained:**
- `groupId`: Your organization/company identifier (e.g., `com.mycompany.app`)
- `artifactId`: Your project name (e.g., `my-app`)
- `archetypeArtifactId`: The template type (`maven-archetype-quickstart` for basic Java)
- `interactiveMode=false`: Skip prompts, use default values

---

### Method 2: Maven Web Application

For creating a web application:

```bash
mvn archetype:generate \
  -DgroupId=com.mycompany.webapp \
  -DartifactId=my-webapp \
  -DarchetypeArtifactId=maven-archetype-webapp \
  -DarchetypeVersion=1.4 \
  -DinteractiveMode=false

cd my-webapp
```

---

### Method 3: Spring Boot Application

For creating a Spring Boot project:

```bash
# Using Spring Initializr
curl https://start.spring.io/starter.tgz \
  -d dependencies=web \
  -d name=my-spring-app \
  -d packageName=com.mycompany.springapp \
  -d javaVersion=17 \
  | tar -xzvf -

cd my-spring-app
```

---

### Method 4: Manual Project Creation

Create a simple project from scratch:

```bash
# Create project directory
mkdir -p ~/projects/hello-world
cd ~/projects/hello-world

# Create directory structure
mkdir -p src/main/java/com/mycompany/app
mkdir -p src/test/java/com/mycompany/app

# Create Main class
cat > src/main/java/com/mycompany/app/App.java << 'EOF'
package com.mycompany.app;

public class App {
    public static void main(String[] args) {
        System.out.println("Hello World!");
    }
}
EOF

# Create pom.xml
cat > pom.xml << 'EOF'
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.mycompany.app</groupId>
    <artifactId>hello-world</artifactId>
    <version>1.0-SNAPSHOT</version>
    <packaging>jar</packaging>

    <name>Hello World</name>

    <properties>
        <maven.compiler.source>17</maven.compiler.source>
        <maven.compiler.target>17</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    </properties>

    <dependencies>
        <!-- JUnit for testing -->
        <dependency>
            <groupId>junit</groupId>
            <artifactId>junit</artifactId>
            <version>4.13.2</version>
            <scope>test</scope>
        </dependency>
    </dependencies>
</project>
EOF
```

---

## Project Structure Explained

After creating a Maven project, you'll see this structure:

```
my-app/
├── pom.xml                          # Project configuration
├── src/
│   ├── main/
│   │   ├── java/                    # Java source files
│   │   │   └── com/mycompany/app/
│   │   │       └── App.java
│   │   └── resources/               # Configuration files, properties
│   └── test/
│       ├── java/                    # Test source files
│       │   └── com/mycompany/app/
│       │       └── AppTest.java
│       └── resources/               # Test resources
└── target/                          # Compiled files (generated)
```

### Key Files:

**pom.xml** - Project Object Model
- Contains project metadata
- Manages dependencies
- Configures build process

**src/main/java** - Your application code

**src/test/java** - Your test code

**target/** - Compiled classes and JAR files (auto-generated)

---

## Running Your Project

### Compile the Project

```bash
# Compile source code
mvn compile

# Compiled classes will be in target/classes/
```

### Run the Application

#### Option 1: Using Maven exec plugin

```bash
# Add this to your pom.xml <build> section:
cat >> pom.xml << 'EOF'
    <build>
        <plugins>
            <plugin>
                <groupId>org.codehaus.mojo</groupId>
                <artifactId>exec-maven-plugin</artifactId>
                <version>3.1.0</version>
                <configuration>
                    <mainClass>com.mycompany.app.App</mainClass>
                </configuration>
            </plugin>
        </plugins>
    </build>
EOF

# Run the application
mvn exec:java
```

#### Option 2: Build and run JAR

```bash
# Package into JAR file
mvn package

# Run the JAR
java -jar target/my-app-1.0-SNAPSHOT.jar
```

#### Option 3: Using java command directly

```bash
# Compile
mvn compile

# Run using classpath
java -cp target/classes com.mycompany.app.App
```

### Run Tests

```bash
# Run all tests
mvn test

# Run specific test
mvn test -Dtest=AppTest

# Skip tests during build
mvn package -DskipTests
```

### Clean Build

```bash
# Remove target directory
mvn clean

# Clean and rebuild
mvn clean package
```

---

## Common Commands Reference

### Maven Commands

```bash
mvn clean              # Remove target directory
mvn compile            # Compile source code
mvn test              # Run tests
mvn package           # Create JAR/WAR file
mvn install           # Install to local repository
mvn clean install     # Clean, compile, test, and install
mvn dependency:tree   # Show dependency tree
mvn dependency:resolve # Download all dependencies
mvn exec:java         # Run application
```

### Project Management

```bash
# Create new project
mvn archetype:generate

# Update project dependencies
mvn clean install -U

# See effective POM (with all inherited properties)
mvn help:effective-pom

# Analyze dependencies
mvn dependency:analyze
```

### Java Commands

```bash
# Compile a single file
javac App.java

# Run a class
java App

# Create JAR manually
jar cvf myapp.jar -C target/classes .

# Run JAR
java -jar myapp.jar

# Run with classpath
java -cp target/classes:lib/* com.mycompany.app.App
```

---

## Adding Dependencies

To add a dependency (e.g., Apache Commons Lang), edit `pom.xml`:

```xml
<dependencies>
    <!-- Add this inside <dependencies> section -->
    <dependency>
        <groupId>org.apache.commons</groupId>
        <artifactId>commons-lang3</artifactId>
        <version>3.14.0</version>
    </dependency>
</dependencies>
```

Then run:

```bash
mvn clean install
```

Search for dependencies at: https://mvnrepository.com/

---

## Quick Start Checklist

- [ ] Install Java (JDK)
- [ ] Verify `java -version` and `javac -version`
- [ ] Set `JAVA_HOME` environment variable
- [ ] Install Maven
- [ ] Verify `mvn -version`
- [ ] Create project using `mvn archetype:generate`
- [ ] Navigate to project directory
- [ ] Run `mvn clean install`
- [ ] Run application with `mvn exec:java`

---

## Example: Complete Workflow
### Vulnerable Server
```bash
# 1. Install Java + Maven
sudo apt update
sudo apt install openjdk-17-jdk maven -y

# 2. Verify installation
java -version
mvn -version

# 3. Create project directory
mkdir -p ~/projects && cd ~/projects

# 4. Generate new Maven project (UPDATED groupId + artifactId)
mvn archetype:generate \
  -DgroupId=demo \
  -DartifactId=log4shell-lab \
  -DarchetypeArtifactId=maven-archetype-quickstart \
  -DarchetypeVersion=1.4 \
  -DinteractiveMode=false

# 5. Navigate to project
cd log4shell-lab

# 6. Build project
mvn clean install

# 7. Run vulnerable server (UPDATED main class)
mvn exec:java -Dexec.mainClass="demo.VulnerableServer"
```
###Callback server 

```
mvn exec:java -Dexec.mainClass="demo.CallbackMonitor"
```
---

## Troubleshooting

### Java not found
```bash
# Add to ~/.bashrc
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
export PATH=$JAVA_HOME/bin:$PATH
source ~/.bashrc
```

### Maven build fails
```bash
# Clear Maven cache
rm -rf ~/.m2/repository

# Rebuild
mvn clean install -U
```

### Permission denied
```bash
# Make sure you have write permissions
chmod -R 755 ~/projects
```

---

*Happy Coding! 🚀*
