#!/bin/bash
#
# Script to setup OSCARS IDC
#
# Xi Yang xyang@isi.edu November 20, 2011
# Andy Lake andy@es.net December 7, 2011
###################################################

SRCDIR=`dirname $0`
CONFDIR="$SRCDIR/etc"
SCRIPTDIR="$SRCDIR/scripts"

echo ""
echo " Before running this you MUST make sure the 'OSCARS_VM_CONF' is"
echo " configured with the proper site specific values"
if [ ! -e "$CONFDIR/OSCARS_VM_CONF" ]; then
    echo " >>> OSCARS_VM_CONF is missing. Please add to $CONFDIR."
    echo
    exit 1
fi
echo " Please check the 'OSCARS_VM_CONF' to verify it has proper information."
read -p "Have you verified 'OSCARS_VM_CONF' (y/n) ? " YESNO
if [ $YESNO == 'y' -o $YESNO == 'Y' ]; then
    echo ""
    cp ${CONFDIR}/OSCARS_VM_CONF ${CONFDIR}/OSCARS_VM_CONF.old > /dev/null
    echo " Copied OSCARS_VM_CONF into OSCARS_VM_CONF.old"
    echo " continuing..."
else
    echo " Please edit 'OSCARS_VM_CONF' before running this script"
    exit 1
fi

CHECKPOINT=0
if [ -e .checkpoint ]; then
    echo ""
    echo "---- The script has been previously executed."
    read -p " Do you want to resume from where you have stopped or failed (y/n) ?" YESNO
    if [ $YESNO == 'y' -o $YESNO == 'Y' ]; then
        . ${SRCDIR}/.checkpoint > /dev/null
        rm .checkpoint  > /dev/null
    else
        rm .checkpoint  > /dev/null
    fi    
    echo
fi

chmod 755 ${CONFDIR}/*_CONF
. ${CONFDIR}/ADVANCED_CONF > /dev/null
. ${CONFDIR}/OSCARS_VM_CONF > /dev/null


if [[ $CHECKPOINT < 1 ]]; then
    echo
    echo "Configuring KVM virtual machine: $VM_NAME"
    ${SCRIPTDIR}/setup_kvm.sh
    if [ ! $? -eq 0 ] ; then
        echo "setup_kvm.sh failed"
        exit 1
    fi
else
    echo
    echo "Skip configuring KVM virtual machine: $VM_NAME"
fi

echo "export CHECKPOINT=1" > .checkpoint

if [[ $CHECKPOINT < 2 ]]; then
    echo
    echo "Configuring OSCARS"
    ${SCRIPTDIR}/setup_oscars.sh
    retcode=$?
    if [ ! $retcode -eq 0 ] ; then
        echo "setup_oscars.sh failed"
        exit 1
    fi
else
    echo
    echo "Skip configuring OSCARS"
fi

echo "export CHECKPOINT=2" > .checkpoint

echo "Finalizing: preparing VM for reboot."
# Make sure OSCARS boots on start-up
$SSHCMD sudo sed -i "s/###//g" /etc/rc.local
$SSHCMD sudo sed -i "s/###//g" /etc/rc.d/rc.local
#setup DNS
$SSHCMD sudo sed -i "s/###/nameserver\ $IDC_DNS/" /etc/resolv.conf
# Remove public key used for config
$SSHCMD rm /home/oscars/.ssh/authorized_keys

echo "Shutdown OSCARS VM host and Rebooting ... waiting for 40 sec for shutdown to complete..."
`virsh shutdown $VM_NAME >/dev/null 2>/dev/null`
sleep 40
echo "Rebooting OSCARS VM host"
`virsh create /etc/libvirt/qemu/$VM_NAME.xml >/dev/null 2>/dev/null`
`virsh start $VM_NAME >/dev/null 2>/dev/null`
sleep 80
rm -f .checkpoint > /dev/null
echo ""
echo "***IMPORTANT: REMEMBER TO MANUALLY CHANGE THE DEFAULT OSCARS PASSWORDS***"
echo "1. Login to host as user oscars and change your password with the 'passwd' command"
echo "2. Login to host as user oscars and change the root password with the 'sudo passwd root' command"
echo "3. Login to the WBUI at https://${IDC_HOST}:8443/OSCARS and change the 'oscars-admin' password"
echo ""
echo "Done... Enjoy!"
