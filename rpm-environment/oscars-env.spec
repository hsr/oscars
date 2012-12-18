%define package_name env
%define oscars_src_dir rpm-environment
%define oscars_dist /opt/oscars
%define oscars_home /etc/oscars
%define relnum 1

Name:           oscars-%{package_name}
Version:        0.6
Release:        %{relnum}
Summary:        OSCARS Environment
License:        distributable, see LICENSE
Group:          Development/Libraries
URL:            http://code.google.com/p/oscars-idc/
Source0:        oscars-%{version}-%{relnum}.tar.gz
BuildRoot:      %{_tmppath}/%{name}-%{version}-%{release}-root-%(%{__id_u} -n)
BuildArch:      noarch
Requires:       java-1.6.0-openjdk

%description
Configures environment variables, certificates and scripts used globally by OSCARS modules

%pre
/usr/sbin/groupadd oscars 2> /dev/null || :
/usr/sbin/useradd -g oscars -r -s /sbin/nologin -c "OSCARS User" -d /tmp oscars 2> /dev/null || :

%prep
%setup -q -n oscars-%{version}-%{relnum}

%clean
rm -rf %{buildroot}

%build

%install
rm -rf %{buildroot}
mkdir -p %{buildroot}/etc/profile.d/
mkdir -p %{buildroot}/etc/init.d
echo "OSCARS_HOME=%{oscars_home}" > %{buildroot}/etc/profile.d/oscars.sh
echo "export OSCARS_HOME" >> %{buildroot}/etc/profile.d/oscars.sh
echo "OSCARS_DIST=%{oscars_dist}" >> %{buildroot}/etc/profile.d/oscars.sh
echo "export OSCARS_DIST" >> %{buildroot}/etc/profile.d/oscars.sh
mkdir -p %{buildroot}/%{oscars_dist}/bin
install -m 755 %{oscars_src_dir}/bin/gendefaultcerts %{buildroot}/%{oscars_dist}/bin/gendefaultcerts
install -m 755 %{oscars_src_dir}/scripts/oscars %{buildroot}/etc/init.d/oscars
utils/bin/exportconfig ./ %{oscars_home} %{buildroot}
bash common-soap/bin/exportconfig ./ %{oscars_home} %{buildroot}

%post
chmod 755 /etc/profile.d/oscars.sh
mkdir -p %{oscars_home}/modules
mkdir -p %{oscars_home}/keystores
%{oscars_dist}/bin/gendefaultcerts %{oscars_home}/keystores
chown oscars:oscars %{oscars_home}/keystores/*
chmod 600 %{oscars_home}/keystores/*

%files
%defattr(-,oscars,oscars,-)
%config %{oscars_home}/Utils/conf/*
/etc/profile.d/oscars.sh
%{oscars_dist}/bin/gendefaultcerts
/etc/init.d/oscars
%{oscars_home}/wsdl/*
