SMSC SMPP simulator
=======

Simple SMSC SMPP simulator which supports:

* Listens on specified ports for SMPP messages and sends back OK responses.
* For each submit request it receives it sends a delivery receipt with configurable delay.
* Supports listening on multiple ports.
* Outgoing delivery receipt messages are sent to connected set of RX and TRX connections with the same application/system ID 
as had the submit message that triggered the delivery receipt. RoundRobin is used to rotate between available connections.
* Supports sending of deliver messages to connected clients (ignores application ID of connected sessions).
* Deliver messages may be segmented SMPP messages, supports segmentation set via optional parameter or via UDH00 or UDH08.
* MO message sent by simulator is controlled with JMX commands - start, stop, send message stream and so on.

Was used as a testing tools to test proper handling of delivery receipts and segmented deliver messages. 

Maven 3.x is needed to build the project and create distribution package.
 
    mvn clean install assembly:assembly

Enables configuration via spring xml contexts. See src\main\resources\context.xml. Project needs to be recompiled after
config change (room for improvement :-).

Following command starts SMPP servers on ports 34567, 34568 and 34569 and sets log level to INFO:
 
    java -Xms32m -Xmx1024m -jar smscsim.jar -ll INFO -p 34567 34568 34569

Uses following libraries:

* [Cloudhopper SMPP](https://github.com/twitter/cloudhopper-smpp)
* [Spring](http://projects.spring.io/spring-framework/)
* [Google Guava](https://code.google.com/p/guava-libraries/)
* [JCommander](http://jcommander.org/)
 
License
-------

Copyright (C) 2014 Matous Voldrich.

This work is licensed under the Apache License, Version 2.0. See [LICENSE](License.txt) for details.