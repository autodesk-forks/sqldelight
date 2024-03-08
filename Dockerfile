# Note: We use cached images that are currently manually triggered to be built and redeployed to Artifactory.
# 		If you are making a change to this file, make sure to run
#		https://c05-acs.jenkins.autodesk.com/job/ACS/job/Custom-Deploy-Tasks/job/Mobile-Common/job/Mobile-Push-Docker/
#		job and then copy the generated docker image tag sha into
#       https://github.com/plangrid/build-mobile-groovy/blob/main/vars/dockerTag.groovy

FROM azul/zulu-openjdk:17.0.4.1-17.36.17

RUN apt-get update && apt-get install -y \
    curl \
    git \
    build-essential

RUN mkdir sqldelight
COPY . ./sqldelight
