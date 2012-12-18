%define install_base /opt/oscars
%define relnum 2 

Name:           oscars
Version:        0.6
Release:        %{relnum}
Summary:        OSCARS  
License:        distributable, see LICENSE
Group:          Development/Libraries
URL:            http://code.google.com/p/oscars-idc/
BuildRoot:      %{_tmppath}/%{name}-%{version}-%{release}-root-%(%{__id_u} -n)
BuildRequires:  java-1.6.0-openjdk
BuildRequires:  java-1.6.0-openjdk-devel
BuildRequires:  perl
BuildArch:      noarch
Requires:       oscars-env
Requires:       oscars-api
Requires:       oscars-authN
Requires:       oscars-authZ
Requires:       oscars-coordinator
Requires:       oscars-lookup
Requires:       oscars-notificationBridge
Requires:       oscars-pce-bandwidth
Requires:       oscars-pce-connectivity
Requires:       oscars-pce-dijkstra
Requires:       oscars-pce-l3mpls
Requires:       oscars-pce-nullagg
Requires:       oscars-pce-vlan
Requires:       oscars-resourceManager
Requires:       oscars-servlets
Requires:       oscars-tools
Requires:       oscars-topoBridge
Requires:       oscars-wbui
Requires:       oscars-wsnbroker
Requires:       java-1.6.0-openjdk

%description
OSCARS is a platform of services that allows for the reservation of circuits on a network. This package installs a default set of modules that can be used to perform basic OSCARS functions. 

%pre

%prep

%clean
rm -rf %{buildroot}

%build
mkdir -p %{buildroot}/%{install_base}/
echo "%{version}-%{release}" >  %{buildroot}/%{install_base}/VERSION

%install

%post

%files
%{install_base}/VERSION

