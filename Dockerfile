# Note: We use cached images that are currently manually triggered to be built and redeployed to Artifactory.
# 		If you are making a change to this file, make sure to run
#		https://c05-acs.jenkins.autodesk.com/job/ACS/job/Custom-Deploy-Tasks/job/Mobile-Common/job/Mobile-Push-Docker/
#		job and then copy the generated docker image tag sha into
#       https://github.com/plangrid/build-mobile-groovy/blob/main/vars/dockerTag.groovy

FROM azul/zulu-openjdk:17.0.4.1-17.36.17

RUN apt-get update && apt-get install -y \
    curl \
    git \
    unzip \
    wget \
    build-essential

# Set desired Android Linux SDK version
ENV ANDROID_SDK_ZIP "commandlinetools-linux-6609375_latest.zip"
ENV ANDROID_SDK_ZIP_URL "https://dl.google.com/android/repository/${ANDROID_SDK_ZIP}"
ENV ANDROID_HOME /opt/android-sdk-linux

# Install Android SDK
RUN mkdir -p ${ANDROID_HOME}

RUN cd ${ANDROID_HOME} \
  && wget --quiet ${ANDROID_SDK_ZIP_URL} \
  && unzip -q ${ANDROID_HOME}/${ANDROID_SDK_ZIP} -d ${ANDROID_HOME}/cmdline-tools \
  && rm ${ANDROID_HOME}/${ANDROID_SDK_ZIP}

ENV PATH ${PATH}:${ANDROID_HOME}/cmdline-tools/tools/bin

RUN mkdir -p ${HOME}/.android
RUN touch ${HOME}/.android/repositories.cfg

ENV ANDROID_API_LEVEL "34"

RUN yes Y | sdkmanager --licenses
RUN yes Y | sdkmanager "build-tools;${ANDROID_API_LEVEL}.0.0"
RUN yes Y | sdkmanager "platform-tools"
RUN yes Y | sdkmanager "platforms;android-${ANDROID_API_LEVEL}"
RUN yes Y | sdkmanager "ndk;21.0.6113669"
RUN yes Y | sdkmanager "cmake;3.10.2.4988404"

ENV PATH ${PATH}:${ANDROID_HOME}/tools

# Make SDK dir writable
RUN chmod -R a+w ${ANDROID_HOME}


RUN mkdir sqldelight
COPY . ./sqldelight
