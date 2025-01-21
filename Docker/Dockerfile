FROM ubuntu:latest

RUN apt-get update && apt-get install -y curl unzip git zip xauth libxtst-dev libxrender1 libxt6 libxxf86vm-dev

RUN apt-get update && apt-get install -y \
    libgl1-mesa-dri \
    libglu1-mesa \
    libglfw3 \
    libglfw3-dev \
    mesa-utils # Replacement for libgl1-mesa-glx

RUN /bin/bash -c "\
  curl -s \"https://get.sdkman.io\" | bash && \
  source /root/.sdkman/bin/sdkman-init.sh && \
  sdk version && \
  sdk install java 23-open && \
  sdk install sbt && \
  sdk env | grep JAVA_HOME | cut -d '=' -f 2 > /tmp/java_home && \
  export JAVA_HOME=$(cat /tmp/java_home) && \
  echo \"JAVA_HOME=${JAVA_HOME}\"  >> /etc/profile \
  "

ENV JAVA_HOME=/root/.sdkman/candidates/java/23.0.1-open

RUN git clone https://github.com/philippeZim/JP_Morgan_Chess.git /app

WORKDIR /app

RUN /bin/bash -c "source /root/.sdkman/bin/sdkman-init.sh && sbt 'about'"

ENV _JAVA_OPTIONS=-Djava.awt.headless=false

RUN apt-get update && apt-get install -y x11-apps
ENV DISPLAY host.docker.internal:0
CMD ["/bin/bash", "-c", "source /root/.sdkman/bin/sdkman-init.sh && sbt run"]