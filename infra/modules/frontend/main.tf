
variable "project_name" {
  type = string
  description = "name of project"
}

variable "domain_name" {
  type = string
  description = "domain name"
}


resource aws_s3_bucket "bucket-for-site" {
  
}