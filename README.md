Leshan
======

Leshan is a OMA Lightweight M2M server implementation.

What is OMA LWM2M: 
http://technical.openmobilealliance.org/Technical/release_program/lightweightM2M_v1_0.aspx

The specification: 
http://member.openmobilealliance.org/ftp/Public_documents/DM/LightweightM2M/

Introduction to LWM2M:
http://fr.slideshare.net/zdshelby/oma-lightweightm2-mtutorial

Contact
-------

Join the project mailling list : 

leshan-lwm2m@googlegroups.com

https://groups.google.com/d/forum/leshan-lwm2m

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
