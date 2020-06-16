#################################################################################################################
##                  Dockerfile for CircleCI
##
##  This image contains the tools required to build and run the project.  It is used as a circleci
##  custom docker image for the build and deploy phases in the CI pipeline.
##
#################################################################################################################


FROM centos/nodejs-8-centos7

WORKDIR /usr/local/app
USER root

ENV TERRAFORM_VERSION="0.12.2"
ENV NODE_VERSION="13.x"
ENV JAVA_VERSION="11"
ENV GRADLE_VERSION="6.2.2"

# Update Yum repo
RUN yum -y update && yum clean all

## Install CircleCI required packages
RUN yum -y install git ssh tar gzip ca-certificates wget

## Install Node / NPM
RUN yum install -y gcc-c++ make && yum clean all
RUN curl -sL https://rpm.nodesource.com/setup_${NODE_VERSION} |  bash -
RUN yum -y install nodejs
CMD node -v

## Install Serverless Framework
RUN ["npm", "install", "--global", "--unsafe-perm", "serverless"]
RUN ["ln", "-s", "/opt/app-root/src/.npm-global/bin/serverless", "/usr/local/bin/serverless"]
CMD ["sls", "--version"]

## Install OpenJDK
RUN yum install -y java-${JAVA_VERSION}-openjdk-devel && yum clean all
CMD ["java", "--version"]
CMD ["javac", "--version"]

## Install Gradle
RUN wget https://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip -P /tmp
RUN unzip -d /opt/gradle /tmp/gradle-${GRADLE_VERSION}-bin.zip
ENV PATH=/opt/gradle/gradle-${GRADLE_VERSION}/bin:${PATH}
CMD ["gradle", "--version"]

## Install AWS CLI
RUN yum --enablerepo=extras install -y epel-release
RUN yum install -y python-pip && yum clean all
RUN pip install --upgrade pip
RUN pip install awscli
CMD ["aws", "--version"]

## Install Terraform
RUN cd /tmp && wget https://releases.hashicorp.com/terraform/${TERRAFORM_VERSION}/terraform_${TERRAFORM_VERSION}_linux_amd64.zip
RUN unzip ./terraform_${TERRAFORM_VERSION}_linux_amd64.zip -d /usr/local/bin
CMD ["terraform", "-v"]

## Set up ci user
RUN groupadd -r cigroup
RUN useradd --no-log-init -r -g cigroup circleci
RUN mkdir /home/circleci && mkdir /home/circleci/repo && mkdir /home/circleci/.ssh && cd /home/circleci
RUN chown -R circleci /home/circleci
RUN chgrp -R cigroup /home/circleci
RUN chmod -R 777 /home/circleci
ENV HOME="/home/circleci"

ENV SLS_DEBUG=*

USER circleci

## Install CircleCI CLI
RUN curl -fLSs https://raw.githubusercontent.com/CircleCI-Public/circleci-cli/master/install.sh | bash
