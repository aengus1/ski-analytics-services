# Domain Names

## Front end
 | *Environment*       | *Domain*    | *Example* |
 | ------------- |:-------------:| :---:|
 | production      | var.domain_name | crunch.ski
 | other      |    {var.app_alias}.${var.domain_name}   |  dev.crunch.ski

## Authentication

| *Environment*       | *Domain*    | *Example* |
 | ------------- |:-------------:| :---:|
 | production      | {var.cognito_sub_domain}.${var.domain_name} | auth.crunch.ski
 | other      |    {var.cognito_sub_domain}.${stage}.${var.domain_name}   |  auth.dev.crunch.ski
