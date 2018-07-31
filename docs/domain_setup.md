# Domain Setup

##  Goal
The goal of this work is to setup the DNS configuration for a statically hosted (S3 / CloudFront) site.  The configuration needs to handle http, https and email.

### Overview
In my current setup I have a static site and an angular application that are both hosted on AWS from different codebases.  I have two different AWS accounts, one for dev/staging and another for production.  I have two domains, ski-analytics.com and ski-analytics.ca that point to the same site.

#### Dev / Staging Account
`staging.ski-analytics.com ` this is the staging version of the main site

`app.staging.ski-analytics.com` this is the staging version of the application

#### Production Account

`ski-analytics.com`  Production version of the main site
`ski-analytics.ca`  Alias for production version of main site

`app.ski-analytics.com`  Production version of the application

### Step 1 - Certificate Creation


### Step 2 - Cloud Formation script to create S3 bucket and cloudfront distribution

### Step 3 - Update Nameserver records on hosting provider

From GoDaddy -> manage domains -> my domain
Scrolldown and click change nameservers.  Will take a few minutes to register on godaddy portal, and a few hours to propogate
*Note that have to remove trailing dot and space from route 53 nameserver urls to be accepted by godaddy