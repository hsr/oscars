%define package_name tools 
%define service_name Tools
%define mvn_project_list common-libs,common-logging,common-soap,utils,%{package_name}
%define install_base /opt/oscars/%{package_name}
%define oscars_home /etc/oscars
%define log_dir /var/log/oscars
%define run_dir /var/run/oscars
%define relnum 3 

Name:           oscars-%{package_name}
Version:        0.6
Release:        %{relnum}
Summary:        OSCARS Template Service 
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
Tools to manage the various OSCARS services.

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

#Copy jar files and scripts
cp %{package_name}/target/*.jar %{buildroot}/%{install_base}/target/
install -m 755 %{package_name}/bin/* %{buildroot}/%{install_base}/bin/
install -m 755 bin/parseManifest.sh %{buildroot}/%{install_base}/bin/

#Run exportconfig
mkdir -p %{buildroot}/%{oscars_home}/%{service_name}/conf
cp %{package_name}/config/manifest.yaml %{buildroot}/%{oscars_home}/%{service_name}/conf/manifest.yaml

#Update scripts in bin to point to default one-jar
perl -e 's/^vers=/#vers=/g' -pi $(find %{buildroot}/%{install_base}/bin -type f)
perl -e 's/%{package_name}-\$vers/%{package_name}/g' -pi $(find %{buildroot}/%{install_base}/bin -type f)
perl -e 's/oscarsidc\.jks/oscars.jks/g' -pi $(find %{buildroot}/%{install_base}/bin -type f)
perl -e 's/\$OSCARS_DIST\/bin\/parseManifest\.sh/\$OSCARS_DIST\/%{package_name}\/bin\/parseManifest.sh/g' -pi $(find %{buildroot}/%{install_base}/bin -type f)

%post

#clear out old symbolic links
if [ "$1" = "2" ]; then
  unlink %{install_base}/target/%{package_name}.one-jar.jar
  unlink %{install_base}/target/%{package_name}.jar
fi

#Create symbolic links to latest version of jar files
ln -s %{install_base}/target/%{package_name}-%{version}-%{relnum}.one-jar.jar %{install_base}/target/%{package_name}.one-jar.jar
chown oscars:oscars %{install_base}/target/%{package_name}.one-jar.jar
ln -s %{install_base}/target/%{package_name}-%{version}-%{relnum}.jar %{install_base}/target/%{package_name}.jar
chown oscars:oscars %{install_base}/target/%{package_name}.jar

%files
%defattr(-,oscars,oscars,-)
%config %{oscars_home}/%{service_name}/conf/*
%{install_base}/target/*
%{install_base}/bin/*

%preun
if [ $1 -eq 0 ]; then
    unlink %{install_base}/target/%{package_name}.one-jar.jar
    unlink %{install_base}/target/%{package_name}.jar
fi
