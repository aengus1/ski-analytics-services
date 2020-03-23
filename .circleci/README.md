# CircleCI Docker Image

The docker file in `.circleci/images` defines a docker image that 
installs the tools required to build and run the project.  This is designed for CI but
could also be used as a development environment - although some additional steps would be required
to configure the AWS CLI.

###Tools

- CircleCI required packages
- Node / NPM
- Serverless Framework
- OpenJDK
- Gradle
- AWS CLI

 

##Prerequisites
- Install docker locally: https://docs.docker.com/get-started/part2/
- Create an account on Dockerhub to host the image

## Building the image

```cd .circleci/images ```

```docker image build -t crunch-ski-ci:1.0.3 .```

## Deploying the image to Dockerhub

```docker login```

``` docker image tag crunch-ski-ci:1.0.3 aengus/crunch-ski:1.0.3```

```docker image push aengus/crunch-ski:1.0.3 ```

## Running the image in interactive mode
```docker run -it crunch-ski-ci:1.0.3 /bin/sh```    