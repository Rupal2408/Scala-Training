# Use a Scala and sbt based image
FROM hseeberger/scala-sbt:11.0.11_1.5.5_2.13.6

# Set the working directory in the Docker image
WORKDIR /app

# Copy the entire project into the Docker image
COPY . /app

ENV BROKER_HOST = 34.172.161.212
ENV SENDER_MAIL = "rupal.gupta240897@gmail.com"
ENV SENDER_MAIL_PASSWORD = "yjlx kmqz asah nzdp"
# Compile the Scala application
RUN sbt compile

# Command to run the application
CMD ["sbt", "run"]

# docker run -d -e BROKER_HOST=205.34.345.50 consumer-image