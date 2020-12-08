UNAME_S := $(shell uname -s)
ifeq ($(UNAME_S), Darwin)
    $(info search spec file for MACOSX)
    SPECFILE        = $(shell find . -maxdepth 1 -type f -name *.spec)
else
    $(info search spec file for Linux)
    SPECFILE        = $(shell find . -maxdepth 1 -type f -name *.spec)
endif

SPECFILE_NAME       = $(shell awk '$$1 == "Name:"     { print $$2 }' $(SPECFILE) )
SPECFILE_VERSION    = $(shell awk '$$1 == "Version:"  { print $$2 }' $(SPECFILE) )
SPECFILE_RELEASE    = $(shell awk '$$1 == "Release:"  { print $$2 }' $(SPECFILE) )
TARFILE             = $(SPECFILE_NAME)-$(SPECFILE_VERSION).tgz
DIST                = $(shell rpm --eval %{dist})
CREST_VERSION       = 2.0
CREST_RELEASE       = $(shell sed -nr '/release=/ s/.*release=([^"]+).*/\1/p' $(PWD)/crestdb-web/src/main/resources/messages.properties)
TARGET_DIR          = "crest-dist"
CREST_TARFILE       = $(SPECFILE_NAME)-$(CREST_VERSION).tgz
CREST_WAR           = $(shell find ./crestdb-web/ -maxdepth 3 -type f -name "crest.war")
CREST_IMAGE         = "gitlab-registry.cern.ch/formica/crest:$(CREST_VERSION)"
##CREST_IMAGE         = "crest-test"
## Commands
MD = mkdir
CP = cp
DOCK = docker
GRADLE = ./gradlew
sources:
    ifeq ($(UNAME_S), Darwin)
        $(info create tar for MACOSX)
	    tar -zcvf --exclude='.git' --exclude='.gitignore' --transform 's,^,$(SPECFILE_NAME)-$(SPECFILE_VERSION)/,' $(TARFILE) src/*
    else
	    tar -zcvf $(TARFILE) --exclude-vcs --transform 's,^,$(SPECFILE_NAME)-$(SPECFILE_VERSION)/,' src/*
    endif

clean:
	rm -rf build/ $(TARFILE)
	$(GRADLE) clean
rpm: sources
	rpmbuild -bb --define 'dist $(DIST)' --define "_topdir $(PWD)/build" --define '_sourcedir $(PWD)' $(SPECFILE)
srpm: sources
	rpmbuild -bs --define 'dist $(DIST)' --define "_topdir $(PWD)/build" --define '_sourcedir $(PWD)' $(SPECFILE)
mrpm:
	rpmbuild -bb --define "_topdir $(PWD)/build" --define "_sourcedir $(PWD)" --define "version $(COOLR_VERSION)" --define "release $(COOLR_RELEASE)" $(SPECFILE)

dist:
	rm -rf $(TARGET_DIR)
	$(MD) -p $(TARGET_DIR)

build: clean
	$(GRADLE) clean build
package: dist
	$(CP) $(CREST_WAR) $(TARGET_DIR)/crest.war
	$(CP) ./logback.xml.crest $(TARGET_DIR)/logback.xml
	$(CP) ./javaopts.properties.rpm $(TARGET_DIR)/javaopts.properties
	$(CP) ./entrypoint.sh $(TARGET_DIR)/entrypoint.sh
	$(CP) ./config/application.properties $(TARGET_DIR)/
	tar -zcvf $(CREST_TARFILE) $(TARGET_DIR)/*

docker:
	$(info Prepare docker image)
	$(DOCK) build -t $(CREST_IMAGE) .
	$(DOCK) push $(CREST_IMAGE)
