[Build Status on drone.io](https://drone.io/github.com/jvermillard/leshan/latest)

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

Test Sandbox
------------

You can try it live on our demo instance: http://54.228.25.31/

Get and run last binary
-----------------------

```
wget https://drone.io/github.com/jvermillard/leshan/files/leshan.jar
java -jar ./leshan.jar
```

Compile & Run
-------------

```
mvn install
cd leshan-standalone
mvn assembly:assembly -DdescriptorId=jar-with-dependencies
```

Run:

```
java -jar target/leshan-standalone-*-SNAPSHOT-jar-with-dependencies.jar 
```


Connect on Leshan UI: http://localhost:8080

Leshan provides a very simple UI  to get the list of connected clients and interact with clients resources.

Now you can register your LWM2M client like [Eclipse Wakaama](http://eclipse.org/wakaama) or its lua binding [lualwm2m] (https://github.com/sbernard31/lualwm2m).

Alternatively, you can build a client using the leshan-client library or use the example LeshanDevice.  To build the LeshanDevice example, first compile the leshan-client:

```
mvn install
cd leshan-client
mvn assembly:assembly -DdescriptorId=jar-with-dependencies
```

Then run, setting first the hostname and port of the Leshan server followed by the port the client should use (i.e., something that isn't either 5683 or 5684 if both the server and client are running on the same machine):

```
java -jar target/leshan-client-*-SNAPSHOT-jar-with-dependencies.jar localhost 5683 9000 
```

The list of the registered clients: http://localhost:8080/api/clients

Get the instace 0 of the object 3 of a registered client: http://localhost:8080/api/clients/{endpoint}/3/0


![Leshan](https://raw.github.com/msangoi/leshan/master/lw-clients.png)

Code with eclipse
-----------------
You need to add the M2_REPO to your java classpath variables. To do that you can execute the following command:

```
mvn -Declipse.workspace=<path-to-eclipse-workspace> eclipse:add-maven-repo
```
An eclipse restart is needed (if you already have it open).

You can also do that inside eclipse: From the menu bar, select Window > Preferences. Select the *Java > Build Path > Classpath Variables* page.

Now, you need to eclipsify leshan java projects,so run:

```
mvn eclipse:eclipse
```
We advise you to activate *Format source code* and *Organized imports* on *Save Actions*.
