### Gotchas
Error removing a stack.  Delete the resource manually and then remove it from tf remote state like so:

`cd envs/data/api && terraform state rm 'module.api.aws_cognito_user_pool_domain.userpoolDomain'`
