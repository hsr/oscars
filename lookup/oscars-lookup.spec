%define package_name lookup
%define service_name LookupService
%define mvn_project_list common-libs,common-logging,common-soap,utils,%{package_name}
%define install_base /opt/oscars/%{package_name}
%define oscars_home /etc/oscars
%define log_dir /var/log/oscars
%define run_dir /var/run/oscars
%define relnum 4 

Name:           oscars-%{package_name}
Version:        0.6
Release:        %{relnum}
Summary:        OSCARS Lookup Bridge
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
The OSCARS Lookup Bridge is a service that discovers the location of other services in OSCARS. Specifically its used to find other Interdomain Controllers when building a circuit across multiple administrative domains.

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
rm -rf %{buildroot}
mvn -DskipTests --projects %{mvn_project_list} install 
mkdir -p %{buildroot}/%{install_base}/target
mkdir -p %{buildroot}/%{install_base}/bin
mkdir -p %{buildroot}/etc/init.d
cp %{package_name}/target/*.jar %{buildroot}/%{install_base}/target/
install -m 755 %{package_name}/bin/* %{buildroot}/%{install_base}/bin/
install -m 755 %{package_name}/scripts/oscars-%{package_name} %{buildroot}/etc/init.d/oscars-%{package_name}
%{package_name}/bin/exportconfig ./ %{oscars_home} %{log_dir} %{buildroot}
%{package_name}/scripts/configure_keystores %{buildroot}/%{oscars_home}/LookupService/conf %{oscars_home}/keystores/oscars.jks %{oscars_home}/keystores/localhost.jks  %{oscars_home}/keystores/oscars.jks
perl -e 's/^vers=/#vers=/g' -pi $(find %{buildroot}/%{install_base}/bin -type f)
perl -e 's/%{package_name}-\$vers/%{package_name}/g' -pi $(find %{buildroot}/%{install_base}/bin -type f)

%post
mkdir -p %{run_dir}
chown oscars:oscars %{run_dir}
mkdir -p %{log_dir}
chown oscars:oscars %{log_dir}

#clear out old symbolic links
if [ "$1" = "2" ]; then
  unlink %{install_base}/target/%{package_name}.one-jar.jar
  unlink %{install_base}/target/%{package_name}.jar
  unlink %{oscars_home}/modules/oscars-%{package_name}.enabled
fi

ln -s %{install_base}/target/%{package_name}-%{version}-%{relnum}.one-jar.jar %{install_base}/target/%{package_name}.one-jar.jar
chown oscars:oscars %{install_base}/target/%{package_name}.one-jar.jar
ln -s %{install_base}/target/%{package_name}-%{version}-%{relnum}.jar %{install_base}/target/%{package_name}.jar
chown oscars:oscars %{install_base}/target/%{package_name}.jar
mkdir -p %{oscars_home}/modules
chown oscars:oscars %{oscars_home}/modules
ln -s /etc/init.d/oscars-%{package_name} %{oscars_home}/modules/oscars-lookup.enabled
chown oscars:oscars %{oscars_home}/modules/oscars-lookup.enabled
/sbin/chkconfig --add oscars-%{package_name}

%files
%defattr(-,oscars,oscars,-)
%config %{oscars_home}/%{service_name}/conf/*
%{oscars_home}/%{service_name}/data/*
%{install_base}/target/*
%{install_base}/bin/*
/etc/init.d/oscars-%{package_name}

%preun
if [ $1 -eq 0 ]; then
    /sbin/chkconfig --del oscars-%{package_name}
    /sbin/service oscars-%{package_name} stop
    unlink %{install_base}/target/%{package_name}.one-jar.jar
    unlink %{install_base}/target/%{package_name}.jar
    unlink %{oscars_home}/modules/oscars-lookup.enabled
fi
