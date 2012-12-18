%define package_name servlets
%define service_name WBUIService
%define mvn_project_list common-libs,common-logging,common-soap,utils,authN,authZ,database,oscars-war
%define install_base /opt/oscars/%{package_name}
%define oscars_home /etc/oscars
%define log_dir /var/log/oscars
%define run_dir /var/run/oscars
%define relnum 2 

Name:           oscars-%{package_name}
Version:        0.6
Release:        %{relnum}
Summary:        OSCARS Servlet Interface 
License:        distributable, see LICENSE
Group:          Development/Libraries
URL:            http://code.google.com/p/oscars-idc/
Source0:        oscars-%{version}-%{relnum}.tar.gz
BuildRoot:      %{_tmppath}/%{name}-%{version}-%{release}-root-%(%{__id_u} -n)
BuildRequires:  java-1.6.0-openjdk
BuildRequires:  java-1.6.0-openjdk-devel
BuildRequires:  perl
BuildArch:      noarch
Requires:       oscars-env
Requires:       java-1.6.0-openjdk
Requires:       chkconfig

%description
Provides Java servlet interface for use by web interfaces

%pre
/usr/sbin/groupadd oscars 2> /dev/null || :
/usr/sbin/useradd -g oscars -r -s /sbin/nologin -c "OSCARS User" -d /tmp oscars 2> /dev/null || :

%prep
%setup -q -n oscars-%{version}-%{relnum}

%clean
rm -rf %{buildroot}

%build
mvn -DskipTests --projects %{mvn_project_list} clean package

%install
#Clean out previous build
rm -rf %{buildroot}

#Run install target
mvn -DskipTests --projects %{mvn_project_list} install 

#Create directory structure for build root
mkdir -p %{buildroot}/%{install_base}/target

#Copy jar files and scripts
cp oscars-war/target/*.war %{buildroot}/%{install_base}/target/

%post
if [ "$1" = "2" ]; then
  unlink %{install_base}/target/%{package_name}.war
fi

#Create symbolic links to latest version of jar files
ln -s %{install_base}/target/oscars-war-%{version}-%{relnum}.war %{install_base}/target/%{package_name}.war
chown oscars:oscars %{install_base}/target/%{package_name}.war

%files
%defattr(-,oscars,oscars,-)
%{install_base}/target/*

%preun
if [ $1 -eq 0 ]; then
    unlink %{install_base}/target/%{package_name}.war
fi
