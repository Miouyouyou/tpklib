.PHONY: all
all:
	mvn package
	zip target/tpklib-1.0-SNAPSHOT.jar META-INF/MANIFEST.MF
	cp target/tpklib-1.0-SNAPSHOT.jar ${TIZEN_SDK_HOME}/ide/plugins/tpklib.jar
