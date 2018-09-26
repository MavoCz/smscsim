SMSC SMPP simulator
=======

Simple SMSC SMPP simulator written in Java, basic features:

* Listens on specified ports for SMPP messages and sends back OK responses.
* Supports listening on multiple ports.
* Thanks to Cloudhopper SMPP library detailed realtime statistics of SMPP traffic is available through JMX.  

**Delivery Receipt features**:

* For each submit (MT) request it receives, it sends a delivery receipt after fixed/random delay (configurable).
* Outgoing delivery receipt messages are sent to connected set of RX and TRX connections with the same application/system ID 
as had the submit message that triggered the delivery receipt. 
* RoundRobin is used to rotate between available matching connections.

**Deliver features, segmented messages**:

* Supports sending of deliver (MO) messages to connected clients.
* Deliver messages may be segmented SMPP messages, supports segmentation set via optional parameter or via UDH00 or UDH08.
* MO messages sent by simulator to connected clients are controlled with JMX commands - start, stop, send message stream and so on.

Simulator was used as a testing tools to test proper handling of delivery receipts and segmented deliver messages.  

**How to run in Intellij**

* Open the project in Intellij.
* Make sure the project is using JDK 7. This can be verified in the `Project Structure` window.
  * **Note**: Despite the note below saying at least JDK 7 is required, running the application with JDK 10 produced no delivery 
  receipts. The bad thing is running the application using JDK 10 throws no exceptions, but the thread handling delivery receipts 
  does not seem to be running at all. This may be because of the Java module system, but that's unconfirmed.
* Run the `ServerMain.java` at least once to make Intellij create a run configuration.
* After running `ServerMain.java`, go to Run -> Edit Configurations. 
* In the `Run/Debug Configurations`, you should see `ServerMain` under `Applications`
* Type `-ll INFO -p 34567 34568 34569` in the `Program Arguments` text box.
* Run or re-run `ServerMain.java`

**How to run**:

Following command starts SMPP servers on ports 34567, 34568 and 34569 and sets log level to INFO:
 
    java -Xms32m -Xmx1024m -jar smscsim.jar -ll INFO -p 34567 34568 34569

Only server ports and logging level can be defined when starting the server.
Other parameters can be changed by changing Spring xml context. See src\main\resources\context.xml. 
Project needs to be recompiled after such config change (room for improvement :-).

Obviously Java (at least 1.7) is needed to run the simulator and it needs to be in the path.

**How to use JMX features**:

Download monitoring tool [VisualVM](http://visualvm.java.net/download.html) 
and install additional [MBeans browser plugin](http://visualvm.java.net/plugins.html). 

**How to compile**:

Maven 3.x is needed to build the project and create distribution package.
 
    mvn clean install assembly:assembly

Unit test is provided to test basic delivery receipt functionality. See class

    net.voldrich.smscsim.ServerMainTest

Uses following fantastic libraries:

* [Cloudhopper SMPP](https://github.com/twitter/cloudhopper-smpp)
* [Spring](http://projects.spring.io/spring-framework/)
* [Google Guava](https://code.google.com/p/guava-libraries/)
* [JCommander](http://jcommander.org/)
 
License
-------

Copyright (C) 2014 Matous Voldrich.

This work is licensed under the Apache License, Version 2.0. See [LICENSE](License.txt) for details.