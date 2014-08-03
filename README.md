SMSC SMPP simulator
=======

Simple SMSC SMPP simulator which supports:

* For each submit request it receives it sends a delivery receipt with configurable delay.
* Supports listening on multiple ports and multiple applications. 
* Sending of segmented SMPP messages, supports segmentation set via optional parameter or via UDH00 or UDH08.
* Outgoing (MO) messages are sent to connected set of RX and TRX connections with the same application/system ID.
 RoundRobin is used to rotate between them.
* Controlling MO messages sent by simulator with JMX commands - start, stop, send message stream and so on

Sample usage: java -Xms32m -Xmx1024m -jar smscsim.jar -ll INFO -p 34567 34568 34569

Implemented using following libraries:

* Cloudhopper SMPP
* Spring
* Guava
