%define __jar_repack 0
Name:           crest
Version:        %{version}
Release:        %{release}
Summary:        CREST REST application

Group:          Development/Tools
License:        ATLAS
BuildArch: noarch
BuildRoot: %(mktemp -ud %{_tmppath}/%{name}-%{version}-%{release}-XXXXXX)

%description
A REST service access to COOL DB

%pre
getent group crest >/dev/null || groupadd -r crest
getent passwd crest >/dev/null || \
    useradd -r -g crest -m -d /home/crest -s /sbin/nologin \
    -c "Service account to run CREST application" crest

%install
rm -rf $RPM_BUILD_ROOT
mkdir -p $RPM_BUILD_ROOT/usr/local/share/crest/config
cp -p %{_sourcedir}/build/libs/crest.jar $RPM_BUILD_ROOT/usr/local/share/crest
cp -p %{_sourcedir}/config/application.properties $RPM_BUILD_ROOT/usr/local/share/crest
cp -p %{_sourcedir}/crest.service $RPM_BUILD_ROOT/usr/local/share/crest
cp -p %{_sourcedir}/entrypoint.sh $RPM_BUILD_ROOT/usr/local/share/crest
cp -p %{_sourcedir}/logback.xml.crest $RPM_BUILD_ROOT/usr/local/share/crest/logback.xml
cp -p %{_sourcedir}/javaopts.properties.rpm $RPM_BUILD_ROOT/usr/local/share/crest/javaopts.properties
mkdir -p $RPM_BUILD_ROOT/usr/local/share/crest/data/dump
mkdir -p $RPM_BUILD_ROOT/usr/local/share/crest/data/web
%post
cp -p $RPM_BUILD_ROOT/usr/local/share/crest/crest.service /etc/systemd/system/.
##start crest service
systemctl restart crest


%files
%attr(755,crest,crest) /usr/local/share/crest
%attr(644,crest,crest) /usr/local/share/crest/application.properties

%changelog
* Sat Nov 7 2020 Andrea Formica  1.0.0
  - Initial rpm release
