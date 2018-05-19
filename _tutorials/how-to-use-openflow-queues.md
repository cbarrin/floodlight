---
title: "How To Use OpenFlow Queues"
permalink: /tutorials/queues
excerpt: "Descriptions and samples of all layouts included with the theme and how to best use them."
last_modified_at: 2018-03-20T15:59:52-04:00
toc: true
toc_label: "On This Page"
toc_icon: "columns"
---

## What are queues?

Since the beginning of time...err OpenFlow 1.0, OpenFlow has supported queues for rate-limiting packets egress a switch port and for QoS implementation. Queues are designed to provide a guarantee on the rate of flow of packets placed in the queue. As such, different queues at
different rates can be used to prioritize "special" traffic over "ordinary" traffic (or limit "greedy" traffic as a punishment ). OpenFlow 1.0 and 1.1 support queues with guaranteed minimum rates, while OpenFlow 1.2+ support both minimum and maximum rates for a given queue.

OpenFlow 1.3 introduces meters to the OpenFlow protocol, which complement the queue framework already in place by allowing for the rate-monitoring of traffic prior to output. As such, note that meters and queues are complementary and are not different implementations of the same thing. 
A common misconception is that meters are a replacement for queues, which is not true. If you are interested in using meters with Floodlight, please check out this tutorial.

## What switches support queues?

OpenFlow queues have wide support across the board. They are supported on most software switch implementations (e.g. Open vSwitch (OVS), ofsoftswitch, etc), and many hardware switch vendors also support queues, with limitations of course – see your switch documentation for specifics.

Queues, although very useful, are defined outside the OpenFlow protocol. OpenFlow merely provides a wrapper around existing switch queuing mechanisms in order to inform the controller of said queues. Queues must be defined/instantiated by the switch out of band. This means that if you are using e.g. OVS, then you must set up the queues with OVS commands prior to attempting to use them with OpenFlow. This
is analogous to adding ports to an OpenFlow instance or virtual switch.

## Configuring queues on OVS


Since OVS is a very popular software switch, we will use it as our vehicle for switch-specific queue configuration. The following is an example of how to configure a queue on an OVS instance.

```
$ ovs-vsctl add-br s0
$ ovs-vsctl add-port s0 eth0
$ ovs-vsctl set port eth0 qos=@newqos -- --id=@newqos create qos \
  type=linux-htb other-config:max-rate=5000000 queues:0=@newqueue -- \
  --id=@newqueue create queue other-config:min-rate=3000000 \
  other-config:max-rate=3000000
```

After creating a simple switch, s0, we add a port, eth0, to the switch. Then, a lot of OVS magic happens in the third command. Let's inspect it in closer detail.

First, we specify the switch port on which we want to implement a QoS policy – this port is named eth0 and is the port we just added to our switch
 s0. Next, we create a new QoS instance of type linux-htb (common on most Linux distributions). We set the maximum rate of this QoS policy to 5Mbps. Then, we create the specific queues on this port. Queue 0 is our queue with a minimum rate of 3Mbps and a maximum rate of 3Mbps – in other words, it's a queue that will try to stay as close to 3Mbps as possible when it has packets to process. You can vary the max and min rates to your liking as long as your min rate does not exceed your max rate, of course. You must also stay within the rate of the overarching QoS policy (5Mbps in this example).
 
## Using queues in Floodlight

From a controller's perspective, there are few things that need to be done to queues. (All the hard work has already been done on the switch .) Specifically, we can query the switch for the queues it has:

```java
OFQueueGetConfigRequest cr =
    factory.buildQueueGetConfigRequest().setPort(OFPort.ANY).build();
/*
Request queues on any port (i.e. don't care) */
ListenableFuture < OFQueueGetConfigReply > future =
    switchService.getSwitch(DatpathId.of(1)).writeRequest(cr);
/* Send
request to switch 1 */
try {
    /* Wait up to 10s for a reply; return when received; else exception
thrown */
    OFQueueGetConfigReply reply = future.get(10, TimeUnit.SECONDS);
    /* Iterate over all queues */
    for (OFPacketQueue q: reply.getQueues()) {
        OFPort p = q.getPort(); /* The switch port the queue is on */
        long id = q.getQueueId(); /* The ID of the queue */
        /* Determine if the queue rates */
        for (OFQueueProp qp: q.getProperties()) {
            int rate;
            /* This is a bit clunky now -- need to improve API in Loxi */
            switch (qp.getType()) {
                case OFQueuePropertiesSerializerVer13.MIN_RATE_VAL:
                    /* min rate */
                    OFQueuePropMinRate min = (OFQueuePropMinRate) qp;
                    rate = min.getRate();
                    break;
                case OFQueuePropertiesSerializerVer13.MAX_RATE_VAL:
                    /* max rate */
                    OFQueuePropMaxRate max = (OFQueuePropMaxRate) qp;
                    rate = max.getRate();
                    break;
            }
        }
    }
} catch (InterruptedException | ExecutionException | TimeoutException e) { /* catch e.g. timeout */
    e.printStackTrace();
}
```

The above examples assumes the switch DPID is switch 00:00:00:00:00:00:00:01. It also assumes the IOFSwitchService is available in the switchService variable and that the variable factory is some OFFactory. If you need help with these details, refer to the Loxi tutorial and/or on how to use a Floodlight service (e.g. the IOFSwitchService) within your module using the links in the note at the top of this page.

Similar to the above example is sending and processing a queue statistics request/reply:

```java
OFQueueStatsRequest sr = factory.buildQueueStatsRequest().build();
/* Note use of writeStatsRequest (not writeRequest) */
ListenableFuture < List < OFQueueStatsReply >> future =
    switchService.getSwitch(DatpathId.of(1)).writeStatsRequest(sr);
try {
    List < OFQueueStatsReply > replies = future.get(10, TimeUnit.SECONDS);
    for (OFQueueStatsReply reply: replies) {
        for (OFQueueStatsEntry e: reply.getEntries()) {
            long id = e.getQueueId();
            U64 txb = e.getTxBytes();
            /* and so forth */
        }
    }
} catch (InterruptedException | ExecutionException | TimeoutException e) {
    e.printStackTrace();
}
```

We can also direct packets to a queue within a flow-mod. This is done through the flow's action – OpenFlow 1.0 uses the enqueue action, while OpenFlow 1.1+ uses the set_queue action. Note below that the set_queue action does not specify the switch port and only supplies the queue ID. The enqueue action specifies both the switch port and the queue ID. This implies that in OpenFlow 1.1 and up, queue IDs must be globally unique on a particular switch.

```java
ArrayList < OFAction > actions = new ArrayList < OFAction > ();
/* For OpenFlow 1.0 */
if (factory.getVersion().compareTo(OFVersion.OF_10) == 0) {
    OFActionEnqueue enqueue = factory.actions().buildEnqueue()
        .setPort(OFPort.of(2)) /* Must specify port number */
        .setQueueId(1)
        .build();
    actions(enqueue);
} else { /* For OpenFlow 1.1+ */
    OFActionSetQueue setQueue = factory.actions().buildSetQueue()
        .setQueueId(1)
        .build();
    actions(setQueue);
}
OFFlowAdd flowAdd = factory.buildFlowAdd()
    .setActions(actions)
    .build();
```