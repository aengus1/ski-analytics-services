### Email Sending & Receiving Setup

This is a totally manual setup right now. TODO -> automate this

1. In SES click create domain: crunch.ski
2. Click verify domain (use route53) and generate DKIM settings
-wait until domain is validated and dkim settings are validated

This domain is now set up for sending

For receiving:
- part of this was already setup so the steps may not be accurate

1. create a MX record on the crunch.ski domain:
crunch.ski 10 inbound-smtp.us-west-2.amazonaws.com

2. create a rule set in SES. choose forward to bucket -> currently this is going to email.ski-analytics.com
3. set up an SNS topic to notify a mailbox I monitor when the email is forwarded


// to enable sending from cognito
add an identity policy to the domain.
ses console -> click on crunch.ski and scroll down to identity policy
add custom policy - like:
`
{
    "Version": "2008-10-17",
    "Statement": [
        {
             "Sid": "stmnt1234567891234",
             "Effect": "Allow",
             "Principal": {
                "Service": "cognito-idp.amazonaws.com"
             },
             "Action": [
                 "ses:SendEmail",
                 "ses:SendRawEmail"
             ],
             "Resource": "arn:aws:ses:us-west-2:<MY-AWS-ACCOUNT-NUMBER>:identity/admin@example.com"
         }
     ]
 }`
`