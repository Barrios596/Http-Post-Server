# Http-Post-Server

Tomcat server for receiving and storing binary files sent through HTTP.

To run, install maven and add it to PATH, then run in terminal:

`mvn clean tomcat7:run`

The server will be listening on port 5050, and it will receive .png and .mp4 files on address `/servlet`.
