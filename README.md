This is the artifact for the paper "PRDTs: Composable Knowledge-Based Consensus Protocols with Replicated Data Types".

The official submission instructions can be found here: https://2025.splashcon.org/track/splash-2025-oopsla-artifacts#Call-for-Artifacts

 knows: Dots(Map())
  selfcontext: Dots(Map(🪪NODE1 -> [0:29]))}
        at replication.DeltaDissemination.handleMessage(DeltaDissemination.scala:178)
        at replication.DeltaDissemination.$anonfun$1$$anonfun$1(DeltaDissemination.scala:114)
        at channels.LatentConnection$$anon$3.$anonfun$1$$anonfun$1$$anonfun$1$$anonfun$1(Channels.scala:86)
        at channels.NioTCP.runSelection$$anonfun$1(NioTCP.scala:46)
        at java.base/java.lang.Iterable.forEach(Iterable.java:75)
        at channels.NioTCP.runSelection(NioTCP.scala:35)
        at channels.NioTCP.loopSelection(NioTCP.scala:30)
        at probench.cli$.$anonfun$6(cli.scala:233)
        at java.base/java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1144)
        at java.base/java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:642)
        at java.base/java.lang.Thread.run(Thread.java:1583)
Detected leader failure, triggering new election
Detected leader failure, triggering new election
Detected leader failure, triggering new election
Detected leader failure, triggering new election

Detected leader failure, triggering new election
Exception in thread "pool-1-thread-1" java.lang.IllegalStateException: could not answer request, missing deltas for: Dots(Map(🪪NODE1 -> [29]))
  relevant: List(Payload(Set(🪪NODE1),Dots(Map(🪪NODE1 -> [28])),Map(🪪NODE1 -> LastWriterWins(CausalTime(1749890579952,0,-712656600931955414),1749890579952))), Payload(Set(🪪NODE1),Dots(Map(🪪NODE1 -> [27])),Map(🪪NODE1 -> LastWriterWins(CausalTime(1749890579852,0,-712656600931955415),1749890579852))), Payload(Set(🪪NODE1),Dots(Map(🪪NODE1 -> [26])),Map(🪪NODE1 -> LastWriterWins(CausalTime(1749890579752,0,-712656600931955416),1749890579752))), Payload(Set(🪪NODE1),Dots(Map(🪪NODE1 -> [25])),Map(🪪NODE1 -> LastWriterWins(CausalTime(1749890579651,0,-712656600931955417),1749890579651))), Payload(Set(🪪NODE1),Dots(Map(🪪NODE1 -> [24])),Map(🪪NODE1 -> LastWriterWins(CausalTime(1749890579551,0,-712656600931955418),1749890579551))), Payload(Set(🪪NODE1),Dots(Map(🪪NODE1 -> [23])),Map(🪪NODE1 -> LastWriterWins(CausalTime(1749890579451,0,-712656600931955419),1749890579451))), Payload(Set(🪪NODE1),Dots(Map(🪪NODE1 -> [22])),Map(🪪NODE1 -> LastWriterWins(CausalTime(1749890579350,0,-712656600931955420),1749890579350))), Payload(Set(🪪NODE1),Dots(Map(🪪NODE1 -> [21])),Map(🪪NODE1 -> LastWriterWins(CausalTime(1749890579249,0,-712656600931955421),1749890579249))), Payload(Set(🪪NODE1),Dots(Map(🪪NODE1 -> [20])),Map(🪪NODE1 -> LastWriterWins(CausalTime(1749890579149,0,-712656600931955422),1749890579149))), Payload(Set(🪪NODE1),Dots(Map(🪪NODE1 -> [19])),Map(🪪NODE1 -> LastWriterWins(CausalTime(1749890579049,0,-712656600931955423),1749890579049))), Payload(Set(🪪NODE1),Dots(Map(🪪NODE1 -> [18])),Map(🪪NODE1 -> LastWriterWins(CausalTime(1749890578949,0,-712656600931955424),1749890578949))), Payload(Set(🪪NODE1),Dots(Map(🪪NODE1 -> [17])),Map(🪪NODE1 -> LastWriterWins(CausalTime(1749890578849,0,-712656600931955425),1749890578849))), Payload(Set(🪪NODE1),Dots(Map(🪪NODE1 -> [16])),Map(🪪NODE1 -> LastWriterWins(CausalTime(1749890578749,0,-712656600931955426),1749890578749))), Payload(Set(🪪NODE1),Dots(Map(🪪NODE1 -> [15])),Map(🪪NODE1 -> LastWriterWins(CausalTime(1749890578649,0,-712656600931955427),1749890578649))), Payload(Set(🪪NODE1),Dots(Map(🪪NODE1 -> [14])),Map(🪪NODE1 -> LastWriterWins(CausalTime(1749890578549,0,-712656600931955428),1749890578549))), Payload(Set(🪪NODE1),Dots(Map(🪪NODE1 -> [13])),Map(🪪NODE1 -> LastWriterWins(CausalTime(1749890578449,0,-712656600931955429),1749890578449))), Payload(Set(🪪NODE1),Dots(Map(🪪NODE1 -> [12])),Map(🪪NODE1 -> LastWriterWins(CausalTime(1749890578349,0,-712656600931955430),1749890578349))), Payload(Set(🪪NODE1),Dots(Map(🪪NODE1 -> [11])),Map(🪪NODE1 -> LastWriterWins(CausalTime(1749890578249,0,-712656600931955431),1749890578249))), Payload(Set(🪪NODE1),Dots(Map(🪪NODE1 -> [10])),Map(🪪NODE1 -> LastWriterWins(CausalTime(1749890578149,0,-712656600931955432),1749890578149))), Payload(Set(🪪NODE1),Dots(Map(🪪NODE1 -> [9])),Map(🪪NODE1 -> LastWriterWins(CausalTime(1749890578049,0,-712656600931955433),1749890578049))), Payload(Set(🪪NODE1),Dots(Map(🪪NODE1 -> [8])),Map(🪪NODE1 -> LastWriterWins(CausalTime(1749890577949,0,-712656600931955434),1749890577949))), Payload(Set(🪪NODE1),Dots(Map(🪪NODE1 -> [7])),Map(🪪NODE1 -> LastWriterWins(CausalTime(1749890577848,0,-712656600931955435),1749890577848))), Payload(Set(🪪NODE1),Dots(Map(🪪NODE1 -> [6])),Map(🪪NODE1 -> LastWriterWins(CausalTime(1749890577748,0,-712656600931955436),1749890577748))), Payload(Set(🪪NODE1),Dots(Map(🪪NODE1 -> [5])),Map(🪪NODE1 -> LastWriterWins(CausalTime(1749890577648,0,-712656600931955437),1749890577648))), Payload(Set(🪪NODE1),Dots(Map(🪪NODE1 -> [4])),Map(🪪NODE1 -> LastWriterWins(CausalTime(1749890577548,0,-712656600931955438),1749890577548))), Payload(Set(🪪NODE1),Dots(Map(🪪NODE1 -> [3])),Map(🪪NODE1 -> LastWriterWins(CausalTime(1749890577448,0,-712656600931955439),1749890577448))), Payload(Set(🪪NODE1),Dots(Map(🪪NODE1 -> [2])),Map(🪪NODE1 -> LastWriterWins(CausalTime(1749890577347,0,-712656600931955440),1749890577347))), Payload(Set(🪪NODE1),Dots(Map(🪪NODE1 -> [1])),Map(🪪NODE1 -> LastWriterWins(CausalTime(1749890577246,0,-712656600931955441),1749890577246))), Payload(Set(🪪NODE1),Dots(Map(🪪NODE1 -> [0])),Map(🪪NODE1 -> LastWriterWins(CausalTime(1749890577147,0,-712656600931955442),1749890577146))))
# Hardware Dependencies

> In the Hardware Dependencies section, describe the hardware required to evaluate the artifact. If the artifact requires specific hardware (e.g., many cores, disk space, GPUs, specific processors), please provide instructions on how to gain access to the hardware. Keep in mind that reviewers must remain anonymous.

# Getting Started
 podman run -v ./benchmark-results:/app/benchmark-results --rm -it artifact-test etcd-benchmark-local 10 10 30 mixed
> In the Getting Started Guide, give instructions for setup and basic testing. List any software requirements and/or passwords needed to access the artifact. The instructions should take roughly 30 minutes to complete. Reviewers will follow the guide during an initial kick-the-tires phase and report issues as they arise.
>
> The Getting Started Guide should be as simple as possible, and yet it should stress the key elements of your artifact. Anyone who has followed the Getting Started Guide should have no technical difficulties with the rest of your artifact.

# Step by Step Instruction


> In the Step by Step Instructions, explain how to reproduce any experiments or other activities that support the conclusions in your paper. Write this for readers who have a deep interest in your work and are studying it to improve it or compare against it. If your artifact runs for more than a few minutes, point this out, note how long it is expected to run (roughly) and explain how to run it on smaller inputs. Reviewers may choose to run on smaller inputs or larger inputs depending on available resources.
>
> Be sure to explain the expected outputs produced by the Step by Step Instructions. State where to find the outputs and how to interpret them relative to the paper. If there are any expected warnings or error messages, explain those as well. Ideally, artifacts should include sample outputs and logs for comparison.

# Reusability Guide

> In the Reusability Guide, explain which parts of your artifact constitute the core pieces which should be evaluated for reusability. Explain how to adapt the artifact to new inputs or new use cases. Provide instructions for how to find/generate/read documentation about the core artifact. Articulate any limitations to the artifact’s reusability.
>
> If the Artifact Overview is written in a language like Markdown or TeX, then authors are encouraged to upload the rendered output to HotCRP and include the source files in the artifact.
