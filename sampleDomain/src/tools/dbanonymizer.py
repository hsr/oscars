import random, sys;


PATHELEM_TABLE_HEADER = "INSERT INTO `pathElems` VALUES ";
LAYER2DATA_TABLE_HEADER = "INSERT INTO `layer2data` VALUES ";
RESERVATION_TABLE_HEADER = "INSERT INTO `reservations` VALUES ";

original = []
patchedItems = {}
layer2data=[]
urns={}

def parseCSV (line, sep):
    parseFlag = True;
    res = [];
    currentIndex = 0;
    lastIndex = 0;

    for c in line:
        if c in ['"',"'"]:
            parseFlag = not parseFlag;
        if parseFlag:
            if c == sep:
                res += [line[lastIndex:currentIndex]]
                lastIndex = currentIndex + 1
        currentIndex = currentIndex + 1
    if currentIndex > lastIndex:
        res += [line[lastIndex:]]

    return res


def processReservation (line):
    global patchedItems

    userCounter=0;
    entries = []
    logins = {}
    # trim the begining of the line
    dbLine = line.replace (RESERVATION_TABLE_HEADER,"");
    newLine = RESERVATION_TABLE_HEADER;

    # trim the last semi-column
    dbLine = dbLine.rstrip (");\n");
    # itemize the line
    items = dbLine.split("),");

    for item in items:
        item = item.lstrip ("(");
        entries += [item];

    for entry in entries:
        tmp = parseCSV (entry,",")
        login = tmp[5];
        if len(tmp) != 12:
            print "Error at line " + entry
            print tmp
            print

        # alter node
        newLogin = ""
        if not login in logins:
            newLogin = "'user-" + str(userCounter) + "@localhost.localdomain'";
            logins[login]= newLogin;
            userCounter += 1;
        else:
            newLogin = logins[login];
        newLine += "("+tmp[0]+","+tmp[1]+","+tmp[2]+","+tmp[3]+","+tmp[4]+","+newLogin+","+tmp[6]+","+tmp[7]+","+tmp[8]+","+tmp[9]+","+tmp[10]+","+tmp[11]+"),"
    newLine = newLine.rstrip (",")
    newLine += ";\n"

    patchedItems [RESERVATION_TABLE_HEADER] = newLine


def stripUrn (rawUrn):
    urn = rawUrn.split(":");
    domain = urn[3].replace("domain=","").lower();
    node = urn[4].replace("node=","").lower();
    port = urn[5].replace("port=","");
    link = urn[6].replace("link=","");
    link = link.rstrip("'");

    newUrn = urn[0] + ":" + urn[1] + ":" + urn[2] + ":" + domain + ":" + node + ":" + port + ":" + link + "'"
    return newUrn
                   
def processLayer2Data():
    global layer2data, urns

    newLine = LAYER2DATA_TABLE_HEADER;

    for item in layer2data:
        urn1 = stripUrn(item[2])
        urn2 = stripUrn(item[3])
        if  (not urn1 in urns) or (not urn2 in urns):
            print "Cannot bind URN from Layer2Data table to PathElem table - entry is removed"
            print item
            print
            continue
        newUrn1 = urns[urn1]
        newUrn2 = urns[urn2]
        newLine += "(" + item[0] + "," + item[1] + "," + newUrn1 + "," + newUrn2 + "),"

    newLine = newLine.rstrip (",")
    newLine += ";\n"

    patchedItems [LAYER2DATA_TABLE_HEADER] = newLine

    

def loadLayer2Data (line):
    global layer2data
    entries=[]

    # trim the begining of the line
    dbLine = line.replace (LAYER2DATA_TABLE_HEADER,"");
    # trim the last semi-column
    dbLine = dbLine.rstrip (");\n");
    # itemize the line
    items = dbLine.split("),");

    for item in items:
        item = item.lstrip ("(");
        entries += [item];

    for entry in entries:
        tmp = parseCSV (entry,",");
        #print tmp
        layer2data += [tmp];



def processPathElem (line):

    global urns

    domains = {};
    entries = [];
    nodes = {};
    ports={};
    sourceItems = [];
    resultItems = [];

    nodeCounter=0;
    ipCounter=1;

    # trim the begining of the line
    dbLine = line.replace (PATHELEM_TABLE_HEADER,"");
    # trim the last semi-column
    dbLine = dbLine.rstrip (");\n");
    # itemize the line
    items = dbLine.split("),");

    for item in items:
        item = item.lstrip ("(");
        entries += [item];

    for entry in entries:
        tmp = parseCSV (entry,",");
        #print tmp
        sourceItems += [tmp];
        urn = tmp[3].split(":");
        domain = urn[3].replace("domain=","").lower();
        node = urn[4].replace("node=","").lower();
        port = urn[5].replace("port=","");
        link = urn[6].replace("link=","");
        link = link.rstrip("'");

        # alter node
        newNode = ""
        if not node in nodes:
            newNode = "node-" + str(nodeCounter);
            nodes[node]=newNode;
            nodeCounter += 1;
        else:
            newNode = nodes[node];
        #print node + " ---> " + newNode;

        newPort=""
        newLink=""

        if (port.find("xe-") == 0):
            p1 = random.randint(0,8)
            p2 = random.randint(0,8) 
            newPort = "xe-" + str(p1) + "/" + str(p2) + "/0"
            if link == "*":
                newLink=link
            else:
                newLink="xe-" + str(p1) + "/" + str(p2) + "/0.0"

        elif (port.find("ge-") == 0):
            p1 = random.randint(0,8)
            p2 = random.randint(0,8) 
            newPort = "ge-" + str(p1) + "/" + str(p2) + "/0"
            if link == "*":
                newLink=link
            else:
                newLink="ge-" + str(p1) + "/" + str(p2) + "/0.0"

        elif (port.find("TenGigabitEthernet") == 0):
            p1 = random.randint(0,8)
            p2 = random.randint(0,8) 
            newPort = "TenGigabitEthernet" + str(p1) + "/" + str(p2)
            if link == "*":
                newLink=link
            else:
                newLink="TenGigabitEthernet" + str(p1) + "/" + str(p2) + ".0"

        elif (port.find("GigabitEthernet") == 0):
            p1 = random.randint(0,8)
            p2 = random.randint(0,8) 
            newPort = "GigabitEthernet" + str(p1) + "/" + str(p2)
            if link == "*":
                newLink=link
            else:
                newLink="GigabitEthernet" + str(p1) + "/" + str(p2) + ".0"

        elif (port.find("S") == 0):
            if not port in ports:
                newPort = "S" +str(random.randint(0,9))+str(random.randint(0,9))+str(random.randint(0,9))
                newPort += str(random.randint(0,9))+str(random.randint(0,9))
                newLink = "192.168.1." + str(ipCounter);
                ports[port]= [newPort,newLink];
                ipCounter += 1;
            else:
                newPort = ports[port][0];
                newLink = ports[port][1];
        else:
            print "Cannot process path element (discard):"
            print tmp
            print
            continue

        newUrn = "'urn:ogf:network:" + domain + ":" + newNode + ":" + newPort + ":" + newLink + "'"
        resultItems +=  [newUrn]

    newLine = PATHELEM_TABLE_HEADER;
    for old,new in zip (sourceItems,resultItems):
        urns[stripUrn(old[3])] = new
        newLine += "(" + old[0] + "," + old[1] + "," + old[2] + "," + new + "),"
    newLine = newLine.rstrip (",")
    newLine += ";\n"

    patchedItems [PATHELEM_TABLE_HEADER] = newLine

#
#
#

if (len(sys.argv) != 3):
    print "Syntax Error: dbaninymizer <source_file> <destination_file>"
    exit(0)

srcFile = open (sys.argv[1],"r")
dstFile = open (sys.argv[2],"w")

pathElemEntry = -1;
layer2dataEntry = -1;
reservationEntry = -1;

for line in srcFile:
    original += [line]
    if (line.find (PATHELEM_TABLE_HEADER) >= 0):
        processPathElem (line);
    if (line.find (LAYER2DATA_TABLE_HEADER) >= 0):
        loadLayer2Data (line);
    if (line.find (RESERVATION_TABLE_HEADER) >= 0):
        processReservation (line);

srcFile.close()

processLayer2Data()

for line in original:
    if (line.find (PATHELEM_TABLE_HEADER) >= 0):
        dstFile.write (patchedItems[PATHELEM_TABLE_HEADER]);
    elif (line.find (LAYER2DATA_TABLE_HEADER) >= 0):
        dstFile.write (patchedItems[LAYER2DATA_TABLE_HEADER]);
    elif (line.find (RESERVATION_TABLE_HEADER) >= 0):
        dstFile.write (patchedItems[RESERVATION_TABLE_HEADER]);
    else:
        dstFile.write(line)


dstFile.close()

