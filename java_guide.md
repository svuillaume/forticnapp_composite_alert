
## 1️⃣ Install Java and Maven

Update packages and install OpenJDK 17 + Maven:

```bash
sudo apt update
sudo apt install openjdk-17-jdk maven -y

Verify the installation:

java -version
mvn -version

You should see Java 17 and Maven versions printed.

⸻

2️⃣ Download or Create the Lab Project

Ensure your project folder looks like this:

log4shell-lab/
 ├── pom.xml
 └── src/main/java/demo/
     ├── VulnerableServer.java
     └── CallbackMonitor.java

Important: The Java package in files is demo.

⸻

3️⃣ Build the Project

From inside the log4shell-lab folder, run:

mvn clean install

✅ This compiles the Java code and prepares it to run.

⸻

4️⃣ Run the Vulnerable Server

Start the vulnerable server with:

mvn exec:java -Dexec.mainClass="demo.VulnerableServer"

This launches the lab for testing.

⸻

5️⃣ Run the Callback Monitor (Optional)

To monitor for JNDI/LDAP callbacks:

mvn exec:java -Dexec.mainClass="demo.CallbackMonitor"

Use this if you want to observe the lab safely triggering outbound requests.

⸻

⚠️ Security Notes
	•	Isolation: Run the lab in a VM or isolated network.
	•	Vulnerable Version: Uses Log4j 2.14.1 (CVE-2021-44228).
	•	Do NOT expose to the Internet.
	•	Snapshots and revert points are recommended before testing.

⸻

✅ Summary
	•	Step 1: Install Java & Maven
	•	Step 2: Ensure project folder & package names match
	•	Step 3: Build project
	•	Step 4: Run VulnerableServer
	•	Step 5: Optional: Run CallbackMonitor
