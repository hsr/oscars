#!/bin/bash
#
# Script to setup virtual machine for OSCARS IDC
#
# Xi Yang xyang@isi.edu November 15, 2011
# Andy Lake andy@es.net December 7, 2011
###################################################


SRCDIR=`dirname $0`
CONFDIR="$SRCDIR/../etc"

. ${CONFDIR}/ADVANCED_CONF > /dev/null
. ${CONFDIR}/OSCARS_VM_CONF > /dev/null


# prepare source VM image
VM_SRCDIR=`dirname $VM_SRC_IMG`

if [ ! -d $VM_SRCDIR ]; then
   mkdir -p $VM_SRCDIR > /dev/null
fi

if [ ! -f $VM_SRC_IMG ] && [ ! -f $VM_SRC_IMG.gz ]; then
    echo "### Source VM image $VM_SRC_IMG does not exist ###"
    echo ">>> downloading from $VM_IMG_URL ... "
    echo
    result=`wget -O $VM_SRC_IMG $VM_IMG_URL 2>&1`
    if [[ $result == *Not\ Found* ]]; then
        rm -f $VM_SRC_IMG > /dev/null
        echo "### Invalid source VM URL: $VM_IMG_URL ###"
        exit 1
    fi
    # handle compressed image file
    if [[ $VM_IMG_URL == *gz ]]; then
        mv $VM_SRC_IMG ${VM_SRC_IMG}.gz > /dev/null 2> /dev/null
    fi
fi

# clone VM into $VM_NAME 
if [ ! -f  $VM_SRCDIR/$VM_NAME.img ]; then
    if [ ! -f $VM_SRC_IMG ] && [[ $VM_IMG_URL == *gz ]]; then
        echo ">>> Copying ${VM_SRC_IMG}.gz to ${VM_NAME}.gz ... "
        cp -f ${VM_SRC_IMG}.gz $VM_SRCDIR/${VM_NAME}.img.gz > /dev/null 2> /dev/null
        echo ">>> Creating VM image by uncompressing ${VM_NAME}.gz. This will take a while ...... "
        gunzip $VM_SRCDIR/${VM_NAME}.img.gz > /dev/null 2> /dev/null
    else
        echo ">>> Creating VM image $VM_SRCDIR/$VM_NAME.img by cloning. This will take a while ...... "
        cp -f $VM_SRC_IMG $VM_SRCDIR/$VM_NAME.img > /dev/null
    fi
fi 

# check VM private key
if [ ! -f  $VM_SSH_KEY ]; then
    echo "### Cannot find VM private ssh key: $VM_SSH_KEY ###"
    echo ">>> Place the key to $VM_SSH_KEY and run script again"
    exit 1
else
    chmod 600 $VM_SSH_KEY
fi 

# create host-vm communication channel
/sbin/ifconfig $HOST_VM_COMM_IF 10.10.10.1 netmask 255.255.255.0

# configure /etc/libvirt/qemu/$VM_NAME.xml
cp -f ${CONFDIR}/kvm_sample.xml /etc/libvirt/qemu/$VM_NAME.xml
sed  -i "s/MY_VM_NAME/$VM_NAME/g" /etc/libvirt/qemu/$VM_NAME.xml
sed  -i "s/BRIDGE_INTERFACE/$BRIDGE_INTERFACE/g" /etc/libvirt/qemu/$VM_NAME.xml


# start OSCARS VM
vmlist=`virsh list`

if [[ $vmlist != *$VM_NAME* ]]; then
   virsh create /etc/libvirt/qemu/$VM_NAME.xml > /dev/null 2> /dev/null
   virsh start $VM_NAME > /dev/null 2> /dev/null
   echo "$VM_NAME is booting ... wait for 2 minutes ..." 
   sleep 120
fi

# probe whether the VM is alive
for ((i=1;i<=12;i+=1)); do
    ping=`ping -c1 10.10.10.2`
    if [[ $ping =~ "100% packet loss" ]]; then
        if [[ $i == 12 ]]; then
            echo "###Error: cannot communicate with OSCARS virtual machine ###"
            exit 1
        fi
    else
        break
    fi
    sleep 10
done

# reconfigure hostname, IP address, netmask and gateway
result=`$SSHCMD sudo sed -i \"s/HOSTNAME=.*/HOSTNAME=$IDC_HOST/g\" /etc/sysconfig/network`
result=`$SSHCMD sudo sed -i \"s/GATEWAY=.*/GATEWAY=$IDC_GATEWAY/g\" /etc/sysconfig/network`


result=`$SSHCMD sudo sed -i \"s/IPADDR=.*/IPADDR=$IDC_IP/g\" /etc/sysconfig/network-scripts/ifcfg-eth0`
result=`$SSHCMD sudo sed -i \"s/NETMASK=.*/NETMASK=$IDC_NETMASK/g\" /etc/sysconfig/network-scripts/ifcfg-eth0`
result=`$SSHCMD sudo sed -i \"s/GATEWAY=.*/GATEWAY=$IDC_GATEWAY/g\" /etc/sysconfig/network-scripts/ifcfg-eth0`

BROADCAST=`${SRCDIR}/get_netaddr.pl broadcast $IDC_IP $IDC_NETMASK`;
result=`$SSHCMD sudo sed -i \"s/BROADCAST=.*/BROADCAST=$BROADCAST/g\" /etc/sysconfig/network-scripts/ifcfg-eth0`
NETWORK=`${SRCDIR}/get_netaddr.pl network $IDC_IP $IDC_NETMASK`;
result=`$SSHCMD sudo sed -i \"s/NETWORK=.*/NETWORK=$NETWORK/g\" /etc/sysconfig/network-scripts/ifcfg-eth0`
HWADDR=`${SRCDIR}/get_netaddr.pl randmac`;
result=`$SSHCMD sudo sed -i \"s/HWADDR=.*/HWADDR=$HWADDR/g\" /etc/sysconfig/network-scripts/ifcfg-eth0`
sed  -i "s/mac address='[^']*/mac address='$HWADDR/g" /etc/libvirt/qemu/$VM_NAME.xml
