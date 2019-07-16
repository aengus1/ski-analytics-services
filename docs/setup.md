**Initial Setup Notes**
 
 1. update node, npm, install serverless
 2. install gradle `brew install gradle`
 3. serverless login
 4. create an IAM user called `sls-admin`
    - attach direct policy `AdministratorAccess`
    - enable programmatic access
 5.  setup credentials as a new profile called sls-dev `serverless config credentials -p aws -k <key> -s <secret> -n sls-dev`
 6.  setup continuous integration [CI setup](ci_setup.md)
 
