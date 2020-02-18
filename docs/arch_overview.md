# Crunch Ski Architecture

#### Terminology
Stack:  A collection of resources managed by terraform or cloudformation

Workspace: Term to describe the entire application eco-system

##  5000Ft view

This project is a composition of serverless microservices that are hosted on AWS.
The microservices are comprised of java & scala lambda functions that are connected
to entirely serverless components for data storage, web hosting, proxies and apis.
Together, these components form the full backend for the crunch.ski web application.

The microservices are organized into independently deployable modules along functional
and lifecycle lines. The build is managed by gradle and collectively they comprise a
multi-module gradle project.  Some common libraries and base utility functions are 
deployed as lambda layers, allowing the microservices to share code without depending
on each other.

Infrastructure is partially managed by terraform and partially managed by serverless.
The infrastructure stack is complicated by the fact that some of the base components
(DNS, email, iam users) only need to be created once per workspace, whereas the other components such
as data stores and functions need to be duplicated between environments (e.g. dev, ci, prod).


The project's AWS region is configurable per environment but there are certain services that are 
only available in us-east-1, as such the shared stack can only be deployed in us-east-1.


## Design principles 
- ### Entirely Serverless
  Every piece of infrastructure that is provisioned must be billable by invocation count rather than by hour, 
  i.e. fixed costs are minimized and variable costs are maximized.  
  
  **Why is this important?**
  
  - Allows costs to scale as number of users grow.
  - Avoids need to manage servers.
  - Easier to automate as forces everything to be dynamically configured
       
- ###Fully Automated
  The project must be buildable in a single click.  This includes provisioning the workspace and environment(s).
  
  **Why is this important?**
  
  - Automation of the build allows developers to focus on writing code
  - Encourages habit of regular deployments
  - Ability to spin up / down consistent and repeatable environments
  
- ### Tested & Documented
  The project has comprehensive test coverage so that refactors can be carried out safely.  
  
- ### Modules are independently deployable
  This is a tricky one for microservices in a monorepo.  Needs a bit more focus.  Currently CI will
  build and deploy everything.  There are some options available for only deploying changed modules 
  but needs a lot more analysis.

## Infrastructure view



3. Code view