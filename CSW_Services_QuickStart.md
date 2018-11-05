# TMT CSW Services Quickstart

This is a quickstart guide to help developers writing software for TMT to start the CSW services and debug those services (V0.5.0) to start the CSW services and debug those services (V0.5)

## Common Software (CSW) projects

TMT Common Software (CSW) consists of a set of services (e.g., for sending commands, telemetry), and a framework for software applications to integrate with those services. See the following links for documentation, code, and background information:

  - [CSW Getting Started](https://tmtsoftware.github.io/csw/commons/getting-started.html): basic guide walks you through setup, dependencies, and writing basic applications.
  - [GitHub page for main CSW repo](https://github.com/tmtsoftware/csw): you will need to clone this repo in order to get the CSW framework code.
  - [CSW Releases](https://github.com/tmtsoftware/csw/releases): download from here `csw-apps-x.x.x.zip` (latest version) to obtain CSW service executables. While it should also be possible to build the services from the main CSW repo above, TMT recommends that you download and run these released executables.

## Starting CSW Services
Startup script for CSW:, is found in $CSW_ROOT/csw/target/universal/stage/bin.  But first you should set your environment variables 
>	 In Bash: export clusterSeed=YourIpAddressOfYourMachine:port# 
>		eg:      export clusterSeed=172.14.155.245:5552
>		the port# (or seed)  is the number used used when you start the location service
>		
>	 In Bash: export interface=X  , where X is  set to your network interface, which you can get from "ifconfig"
>		eg:       export interface=eno1 

To start CSW you type:
>        $CSW_ROOT/csw/target/universal/stage/bin/csw_services.sh start -i eno1

(If you set the interfaceName env variable to "eno1" (which is dependent on your machine - type: ifconfig and use the one that says BROADCAST, use the label before the colon) , then you can just use "start", otherwise: )


It logs to /tmp/csw/logs, and when it tries to stop it get the *.pid files and uses that to determine the pid of the process.
If those files don't exist then it assumes the processes are stopped.

It stops all the CSW services by calling
  ``` redisSentinel=redis-sentinel
   redisClient=`echo ${redisSentinel} | sed -e 's/-sentinel/-cli/'`
   ${redisClient} -p ${port} shutdown
```


## HOW TO START without the startup file

Order of starting services:
         location, configuration, event, alarm, then it starts redis sentinel

Note, that any of the lines below can have more DEBUGGING information if you add:
 * -v or -verbose
 * -d or -debug
 * -jvm-debug

For the following, ports (or seed_port) for the different services are:

    - seed_port=5552
    - config_port=5000
    - sentinel_port=26379
    - event_master_port=6379
    - alarm_master_port=7379

And clusterSeeds = YourIpAddressOfYourMachine:port# (In the examples below I use 172.17.15.245 for the IP address


### LOCATION SERVICE, (also called the cluster seed), seed_port=5552, and seeds=172.17.15.245:5552, locationLogFile=/tmp/csw/logs/location.log

With a log file and persistant:
>        nohup ./csw-location-server --clusterPort ${seed_port} -DclusterSeeds=${seeds} &> ${locationLogFile} &
OR, on the command line, no log file:
>       ./csw-location-server --clusterPort 5552 -DclusterSeeds=172.17.15.245:5552

### CONFIGURATION SERVICE, config_port=5000, initSvnRepo(see warning) seeds:172.17.15.245:5552
>       nohup ./csw-config-server  --port ${config_port} ${initSvnRepo} -DclusterSeeds=${seeds} &> ${configLogFile} &
OR, on the command line, no log file:
>        ./csw-config-server --port 5000 "" -DclusterSeeds=172.17.15.245:5552
WARNING: The first time you start the configuration service you need to add an option --initRepo 

### EVENT SERVICE:
>        nohup redis-server ../conf/event_service/master.conf  > /tmp/csw/logs/event_master.log 2>&1 &
OR, on the command line, no log file:
>        ./redis-server ../conf/event_service/master.conf  

### ALARM SERVICE
>        nohup redis-server ../conf/alarm_service/master.conf  > /tmp/csw/logs/alarm_master.log 2>&1 &
OR, on the command line, no log file:
>        ./redis-server ../conf/alarm_service/master.conf 
 
### REDIS SENTINEL, sentinel_port=26379, and seeds=172.17.15.245:5552
This is required
>        nohup ./csw-location-agent -DclusterSeeds=${seeds} --name "EventServer,AlarmServer" --command "$redisSentinel ${sentinelConf} --port ${sentinel_port}" --port "${sentinel_port}"> ${sentinelLogFile} 2>&1 &
OR, on the command line, no log file:
>        ./csw-location-agent -DclusterSeeds=172.17.15.245:5552 --name "EventServer,AlarmServer" --command "redis-sentinel ../conf/redis_sentinel/sentinel.conf --port "26379" --port "26379"

(no idea why it has port twice - likely don't need to do that)

## Modifications to csw-services.sh
I made 2 modifications to the file:
 * In the start_services() function it has a sleep of 2 after calling start_seed (which is the location services).  I increased it to 7 seconds and everything starts fine.

I also changed it to have a status command, and also made the stop command also search for the pid's and kill them that way.  The other way it just looks for a file in /tmp/logs/*.pid and kills those pid's.

## Environment Variables to set
Need to set clusterSeeds and interfaceName.  "clusterSeeds" is set to your ip address with a colon and then the port# for the location service, eg: 172.17.15.245:5552.  The "interfaceName" is set to eno1 (depends on what ifconfig shows you).

### Error when starting
Warning, if you happen to download the zip file and try to start up CSW and then you decide to move that directory to a new location - then you will have to likely make some modifications to the configuration files.  It appears to update the configuration file in csw/target/universal/stage/conf/redis_sentinel/sentinel.conf


## Testing

The easiest way to create tests in the IDE is so hover the mouse over the name of the class in its declaration and press `Ctrl-shift-t`. This will start a dialogue to make some new tests (e.g., using JUnit).

The above works well for unit tests (where you just want to test individual class methods), but it is harder to do tests that involve CSW services. As of the time of writing (CSW 0.5), you need to got through some extra work to set up CSW service clients if you want to send and observe responses to commands, subscribe to events etc. For an example of how to do this see the ComponentTest that is part of the **assemblylongcommands** project in the [proto2018 repo](http://172.17.15.243:9081/prototyping/proto2018 ).

Also, at the time of writing, if you want to run the tests from the command line using the `sbt test` task, you will need to add something like the following line to `build.sbt` (the line with `crossPaths`):
```
lazy val `assemblylongcommands-assembly` = project
   .settings(
    crossPaths := false,
    libraryDependencies ++= Dependencies.AssemblylongcommandsAssembly
   )
```

## Use of CommandResponseManager

The *CommandResponseManager* is used to update command completion responses and return them to the sender of a command. In an email discussion with AB on 10/26/2018 he pointed out the following (relevant to CSW 0.5):
```
One thing to watch out for with the CommandResponseManager is that it works like a stack. You can add subcommands and update the responses for them, but if the stack becomes empty, the parent command will be completed right away.

So for example: 

        commandResponseManager.addSubCommand(parentCmd.runId, setAbsTarget.runId)
        commandResponseManager.addSubCommand(parentCmd.runId, beginMotion.runId)
        commandResponseManager.updateSubCommand(setAbsTarget.runId, resp1)
        commandResponseManager.updateSubCommand(beginMotion.runId, resp.asInstanceOf[SubmitResponse])
will work fine, but:

        commandResponseManager.addSubCommand(parentCmd.runId, setAbsTarget.runId)
        commandResponseManager.updateSubCommand(setAbsTarget.runId, resp1)
        commandResponseManager.addSubCommand(parentCmd.runId, beginMotion.runId)
        commandResponseManager.updateSubCommand(beginMotion.runId, resp2)

will complete the parent command after the second line, before the second command is added.

```

                                                          
