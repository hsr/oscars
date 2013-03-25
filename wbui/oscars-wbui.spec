%define package_name wbui 
%define service_name WBUIService
%define mvn_project_list common-libs,common-logging,common-soap,utils,%{package_name}
%define install_base /opt/oscars/%{package_name}
%define oscars_home /etc/oscars
%define log_dir /var/log/oscars
%define run_dir /var/run/oscars
%define relnum 3 

Name:           oscars-%{package_name}
Version:        0.6
Release:        %{relnum}
Summary:        OSCARS Web User Interface 
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
Requires:       oscars-servlets
Requires:       java-1.6.0-openjdk
Requires:       chkconfig

%description
Provides a web interface to OSCARS for creating circuits and managing users.

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
mkdir -p %{buildroot}/%{install_base}/bin
mkdir -p %{buildroot}/etc/init.d
mkdir -p %{buildroot}/var/lib/oscars/wbui/war-tmp

#Copy jar files and scripts
cp %{package_name}/target/*.jar %{buildroot}/%{install_base}/target/
install -m 755 %{package_name}/bin/* %{buildroot}/%{install_base}/bin/
install -m 755 %{package_name}/scripts/oscars-%{package_name} %{buildroot}/etc/init.d/oscars-%{package_name}

#Run exportconfig
%{package_name}/bin/exportconfig ./ %{oscars_home} %{log_dir} %{buildroot}

#Update CXF config files with paths to keystores
%{package_name}/scripts/configure_keystores %{buildroot}/%{oscars_home}/%{service_name}/conf %{oscars_home}/keystores/oscars.jks %{oscars_home}/keystores/oscars.jks  %{oscars_home}/keystores/oscars.jks

#Update path to war file
%{package_name}/scripts/configure_war %{buildroot}/%{oscars_home}/%{service_name}/conf

#Allow to run on non-local host interfaces
perl -e 's/<Set name="host">(.+?)<\/Set>/<!-- <Set name="host">$1<\/Set> -->/g' -pi $(find %{buildroot}/%{oscars_home}/%{service_name}/conf/jetty.*.xml -type f)

#Update scripts in bin to point to default one-jar
perl -e 's/^vers=/#vers=/g' -pi $(find %{buildroot}/%{install_base}/bin -type f)
perl -e 's/%{package_name}-\$vers/%{package_name}/g' -pi $(find %{buildroot}/%{install_base}/bin -type f)

%post
#Create directory for PID files
mkdir -p %{run_dir}
chown oscars:oscars %{run_dir}

#Create directory for logs
mkdir -p %{log_dir}
chown oscars:oscars %{log_dir}

#clear out old symbolic links
if [ "$1" = "2" ]; then
  unlink %{install_base}/target/%{package_name}.one-jar.jar
  unlink %{install_base}/target/%{package_name}.jar
  unlink %{oscars_home}/modules/oscars-%{package_name}.enabled
fi

#Create symbolic links to latest version of jar files
ln -s %{install_base}/target/%{package_name}-%{version}-%{relnum}.one-jar.jar %{install_base}/target/%{package_name}.one-jar.jar
chown oscars:oscars %{install_base}/target/%{package_name}.one-jar.jar
ln -s %{install_base}/target/%{package_name}-%{version}-%{relnum}.jar %{install_base}/target/%{package_name}.jar
chown oscars:oscars %{install_base}/target/%{package_name}.jar

#Add symbolic link to modules directory so can be started by oscars init.d script
mkdir -p %{oscars_home}/modules
chown oscars:oscars %{oscars_home}/modules
ln -s /etc/init.d/oscars-%{package_name} %{oscars_home}/modules/oscars-%{package_name}.enabled
chown oscars:oscars %{oscars_home}/modules/oscars-%{package_name}.enabled

#Configure service to start when machine boots
/sbin/chkconfig --add oscars-%{package_name}

%files
%defattr(-,oscars,oscars,-)
%config %{oscars_home}/%{service_name}/conf/*
%{install_base}/target/*
%{install_base}/bin/*
/etc/init.d/oscars-%{package_name}
/var/lib/oscars/wbui/*

%preun
if [ $1 -eq 0 ]; then
    /sbin/chkconfig --del oscars-%{package_name}
    /sbin/service oscars-%{package_name} stop
    unlink %{install_base}/target/%{package_name}.one-jar.jar
    unlink %{install_base}/target/%{package_name}.jar
    unlink %{oscars_home}/modules/oscars-%{package_name}.enabled
fi
