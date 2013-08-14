leshan
======

Leshan is a LWM2M Server implementation using Apache Mina.


Compile & Run
-------------

Screencast: http://ascii.io/a/4741

Compile with maven, generate a runnable jar:

> mvn assembly:assembly -DdescriptorId=jar-with-dependencies

Run:

> java -jar target/leshan-server-1.0-SNAPSHOT-jar-with-dependencies.jar

Now you can register your LWM2M client.

The list of the registered clients: http://localhost:8080/api/clients

Get the instace 0 of the object 3 of a registered client: http://localhost:8080/api/clients/{endpoint}/3/0
