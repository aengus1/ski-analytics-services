### User Table appsync policy document //TODO -> move this to the appsync stack
data "aws_iam_policy_document" "usertable_appsync_policy_doc" {
  statement {
    sid = "1"
    effect = "Allow"
    actions = [
      "dynamodb:GetItem",
      "dynamodb:PutItem",
      "dynamodb:DeleteItem",
      "dynamodb:UpdateItem",
      "dynamodb:Query",
      "dynamodb:Scan",
      "dynamodb:BatchGetItem",
      "dynamodb:BatchWriteItem"
    ]
    resources = [
      aws_dynamodb_table.user_table.arn
    ]
  }
}
### User Table appsync policy  //TODO -> move this to the appsync stack
resource aws_iam_policy "user_table_appsync_access" {
  name = "user_table_appsync_policy"
  description = "managed policy to allow AWS AppSync to access the tables created by this template"
  path = "/appsync/"
  policy = data.aws_iam_policy_document.usertable_appsync_policy_doc.json
}

## TODO -> websocket access //in ws stack