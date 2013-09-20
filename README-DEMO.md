# Multilayer demo VM guide

This document describes how to run the multilayer demo using the multilayerdemo VM. OSCARS installation is not covered here, and should be installed and configured as described at:

 - **OSCARS:** [http://github.com/hsr/oscars](http://github.com/hsr/oscars)
 - **OSCARS SDN PSS:** [http://github.com/hsr/oscars-sdnpss](http://github.com/hsr/oscars-sdnpss)

**NOTE:** Don't forget to change the OSCARS topology. You can use the python script `json2nmwg.py` under `oscars/tools/python/topology` to generate your NMWG topology file. However, this requires Floodlight to be running, and need to be postponed until later on.

After you have OSCARS installed, download and import the virtual machine to your server. Start the machine and make sure that the OSCARS server have connectivity to it. Both the username and password for the multilayerdemo VM is `mininet`.

The following sections describe how to:

 - **Start a mininet topology similar to the one used in the demo**
 - **Start Floodlight to control our network devices**
 - **Start OSCARS Listener to translate JSON requests to OSCARS API**
 - **Start OSCARS Trigger to monitor and offload traffic**

---

### Mininet topology

After you login, go to `~/esnet/oscars/tools/python/mininet` to start your mininet topology. There are a few mininet topologies defined in that directory, but the one we are interested in is `multilayerdemo.py`. Just start it passing your controller as a parameter:

    cd ~/esnet/oscars/tools/python/mininet
    sudo ./multilayerdemo.py 127.0.0.1
    
### Floodlight

Open a new console and start the version of Floodlight available on the VM:

    cd ~/esnet/floodlight
    java -jar target/floodlight.jar \
      -cf target/bin/floodlightdefault.properties

Now you should be able to access the Floodlight UI using your browser. The Floodlight UI URL is `http://<multilayerdemo vm ip>:8080/ui/index.html`

---

### OSCARS Listener

If you don't know what this is, read the description about the listener at: [http://github.com/hsr/oscars-listener](http://github.com/hsr/oscars-listener)

The version of the listener at the VM have an extra directory `~/esnet/oscars-listener/keys` that contains keys to access the OSCARS instalation at [https://infinerademo.es.net:8443/OSCARS](https://infinerademo.es.net:8443/OSCARS). If you are using another installation of OSCARS, make sure to set the keys as described in the [OSCARS Listener repository](http://github.com/hsr/oscars-listener](http://github.com/hsr/oscars-listener).

To start the OSCARS Listener, run these commands:

    cd ~/esnet/oscars-listener
    
    PORT="9911"; DOMAIN="testdomain-1"
    SERVER="infinerademo.es.net"; KEYS="./keys/"
    
    java -jar oscars-listener.jar \
       ${PORT} ${SERVER} ${DOMAIN} ${KEYS}
       
To test it, try to list some reservations (your can also use your browser instead of curl):

    curl http://<multilayerdemo vm ip>:9911/list/finished; echo;
    
    # the response should be something like:
    
    {'response': 'GRI: testdomain-1-49 Login: oscars Status: FINISHED Start Time: 1379538670 End Time: 1379542270 Bandwidth: 1000
    ... 
    GRI: testdomain-1-42 Login: oscars Status: FINISHED Start Time: 1379498550 End Time: 1379502150 Bandwidth: 1000'}

---

### OSCARS Trigger

The OSCARS Trigger is installed at `~/esnet/oscars-trigger/`. This version of the trigger is already configured with a topology file that matches the mininet topology you used before. This can be changed in the file `base.json` located at `oscarstrigger/static/data/topology/`. You can also modify the coordinates of the switches on the map in the file `base.csv` located at `oscarstrigger/static/data/coordinates/`.

To install and run the trigger web app, just run the following commands:

    cd ~/esnet/oscars-trigger
    
    FLOODLIGHT="127.0.0.1"
    OSCARS="<oscars server IP>"
    LISTENER="127.0.0.1"
    
    python ./run.py --controller=${FLOODLIGHT} \
                    --oscars=${OSCARS} \
                    --listener=${LISTENER}

Now you should be able to see it running at [http://\<multilayerdemo vm ip\>:5000](http://\<multilayerdemo vm ip\>:5000).

##### Trigger functionalities/configurations

###### Event listeners and events behaviour

Note that event listeners are added automatically by the trigger application when it have access to the OSCARS mysql database. The application will try to fetch circuits from the database with username and password equals to `reader`. You can change that in the file `config.py`. You need to restart after changing this value. The default threshold for an event listener created by the application is half of the reserved bandwidth. When this threshold is met, the applicaiton will generate an event based on the event listener and act on it automatically, requesting a new circuit to the OSCARS Listener with the same source and destination but 20x the specified threshold. These values (*.5 for threshold and 20x for new reservations*) can be also be changed in the file `config.py` and the changes will take place after you restart the application.

###### Authentication

Any user can authenticate using OpenID. You need a Google or Yahoo account to authenticate in the system. When you log in, a record with your OpenID information is created in the database, but you can't change anything in the system until your ROLE is set to ADMIN. After you have done that, you can manage (create and delete) event listeners. To make yourself an ADMIN, log in using your OpenID first, and then change the sqlite database using the following commands:

    cd ~/esnet/oscars-trigger
    sqlite3 app.db
    
On the sqlite console, list all the users and set the role of your user to `1`:
    
    select * from user;
    update user set role='1' where id='<your user id>'; 
    
Now if you refresh the events page you should see more options.


###### Big flow detection (threshold violation) vs Burstiness

The trigger web app does not generate events blindly whenever the threshold is exceeded in a single bandwidth measurement. This means that the trigger tolerates bursty traffic caused by sudden peaks in demand (such as short TCP transfers occurring at full speed). The bandwidth monitoring implemented in the trigger app is a moving average of the measured bandwidth (for each event listener). This means that there is a delay in detecting a threshold violation but also that short burts of traffic are allowed to procceed without problems.

This parameter can also be changed in the file `config.py`. You need to restart the application after changing this value.

---

#### Run the demo

The topology started on the first step has 8 hosts, 4 of them (h11, h12, h13, h14) connected to switch s1 and 4 (h51, h52, h53, h54) connected to switch s5. The host `hN` has ip address `10.0.0.N`. If you want to check which switch port is connected to the host, use the Floodlight UI. To access one of the hosts in mininet, go to the mininet console and enter:

    mininet> xterm <host>

This should open a console to host `<host>`. Open consoles to h11, h12 and h51. If you try to ping h51 (10.0.0.51) from h11, you should get *host not reachable* answer, since there is no connectivity between switches s1 and s5. However, if you ping h12 from h11, you should get a response. 

Use OSCARS to create a **100mbps** circuit between the port connecting h11 on s1 and the port connecting h51 on s5. When the reservation is `ACTIVE`, you should see the circuit in the web application graph. You can also check the flows created on each switch using the `OpenFlow Monitor` on the GUI. A event listener should also be created for that reservation, as explained before, and you should be able to see it under the `Trigger > Events` page of the web application. 

Now lets generate some traffic. Start an iperf **UDP** server on host h51 (we are using port 6000 here, but you can use any other port that is not in use):

    iperf -s -u -p 6000

Back on host h11, start a **UDP** flow of 20mbps to port 6000 of host h51 (the parameters will make the flow run for 10 seconds, printing average throughput every second):

    iperf -c 10.0.0.51 -p 6000 -t 10 -i 1 -u -b 20M
    
This flow should go unnoticed by the trigger, since its threshold is set to **50mbps**. Now, if you start a new flow of **200M**, the trigger should react to the increase in bandwidth demand automatically, requesting a new circuit reservation of capacity **1G**:

    iperf -c 10.0.0.51 -p 6000 -t 100 -i 1 -u -b 200M 

The topology should also be updated as the new circuit is created by OSCARS.

You might be asking why we are using UDP and not TCP. There are two reasons for that:

 1. First because we can not control the bandwidth of a TCP flow with **iperf**.
 2. Second because the new circuit request made by the trigger is for packets matching IP Proto = 17 (UDP). You can change that in the `FloodlightTrigger.actOnEvent` method. However, as we do not have flow visibility on the ports being monitored, it is not possible to determine what should be offloaded. Using a sampling feature like **sFlow** would make it possible, but that was out of the scope for this demo.

---

## Installing each component on a different server:

To install each of the components described above on a different server, follow the install instructions from each repository:

 - **OSCARS** : [http://github.com/hsr/oscars](http://github.com/hsr/oscars)
 - **OSCARS SDN PSS** : [http://github.com/hsr/oscars-sdnpss](http://github.com/hsr/oscars-sdnpss)
 - **OSCARS Listener** : [http://github.com/hsr/oscars-listener](http://github.com/hsr/oscars-listener)
 - **OSCARS Trigger** : [http://github.com/hsr/oscars-trigger](http://github.com/hsr/oscars-trigger)
 - **Floodlight** : [http://github.com/hsr/floodlight](http://github.com/hsr/floodlight)