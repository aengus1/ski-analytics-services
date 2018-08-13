# Domain Setup

##  Goal
The goal of this work is to setup the DNS configuration for a statically hosted (S3 / CloudFront) site.  The configuration needs
to handle both http and https.  The site registrar is not on AWS.  The goal is to automate / script as much of this as possible with
 cloudformation so the same setup can be easily reproduced on another account or in another context.

### Overview
In my current setup I have a static site and an angular application that are both hosted on AWS from different codebases.
I have a single AWS account in which I want to deploy both a staging and a production environment.

My top-level domain is `crunch.ski`.  
Production Environment:
`crunch.ski` - main site
`app.crunch.ski` - application

Staging Environment:
`staging.crunch.ski` - main site
`staging-app.crunch.ski` - application

### Step 1 - Create the Domain Stack for the Hosted Zone
1. `cd domain`
2. `sls deploy -v` note you may need to login first `sls login`

This will create a hosted zone in Route 53 and output a list of nameservers


### Step 2 - Update Nameservers on site registrar
Go to your site registrar and find your registered domain name, in my case `crunch.ski`
Click Manage DNS, and then Update Nameservers.  Add each of the 4 nameservers output from the CF script in Step 1.
Note that it may take up to 48 hours for this change to propogate and the domain to actually reference your Route 53 hosted zone.

From GoDaddy -> manage domains -> my domain
Scrolldown and click change nameservers.  Will take a few minutes to register on godaddy portal, and a few hours to propogate
*Note that have to remove trailing dot and space from route 53 nameserver urls to be accepted by godaddy

### Step 3 - Certificate Creation
To serve https a certificate is required.  It is currently possible to create a certificate in cloudformation, but the validation 
can only be done via email, rather than by DNS which is currently available through the console.  As my site registrar is not delivering my email for some reason I have had to create the certificate manually.
1. Go to ACM in the console and change your region to us-east-1 (currently this is the only supported region for security certs in Route 53)
2. Click `Request a Certificate` and enter your domain name.  As I am planning to use sub-domains that I also want to share the same certificate I have
included `*.crunch.ski` as an alternate name.
3. Once created, you will have the option to export your DNS Validation CNAME records.  download the csv file
4. Before leaving ACM make a note of the certificate ARN in the details section on this page
5. Go to Route 53 and click on your hosted zone.  Add a CNAME record using the record name and record value given in the csv file


### Step 4 - Create the Frontend stack (staging)
The frontend stack contains all of the infrastructure required to host and deploy the site, including S3 buckets, CloudFront distributions,
Route 53 Recordsets, a deployment user and credential set.  It also uses the AWS System Parameter Store to store the deployment configuration
for this stack.  This saves having to update a bunch of settings in the application's continuous delivery scripts if any of the underlying resource references change,
we will simply look for parameters with these names.
1. Ensure that your certificate ARN is correctly set in the `custom` section of the front-end script `frontend/serverless.yml`
2. Deploy the staging version of the stack.  Because this stack creates a named IAM user we need to pass some flags to cloudformation to force it to run.
A convenience script `./deploy.sh` has been added to the frontend stack, simply run this.

### Step 5 - Create the Frontend stack (production)
Repeat step 4 but change the --stage=staging flag in ./deploy.sh to --stage=prod
