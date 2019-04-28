version = 0.2

target/life-fhir-gen-$(version)-standalone.jar:
	lein clean
	lein uberjar

target/win/life-fhir-gen-$(version)/life-fhir-gen.bat: script/life-fhir-gen.tpl.bat
	dirname $@ | xargs mkdir -p
	sed -e 's/<VERSION>/$(version)/' $< > $@

target/win/life-fhir-gen-$(version).zip: target/life-fhir-gen-$(version)-standalone.jar target/win/life-fhir-gen-$(version)/life-fhir-gen.bat
	cd target; cp life-fhir-gen-$(version)-standalone.jar win/life-fhir-gen-$(version)
	cd target/win; zip life-fhir-gen-$(version).zip life-fhir-gen-$(version)/*.jar
	cd target/win; zip --to-crlf life-fhir-gen-$(version).zip life-fhir-gen-$(version)/*.bat

target/linux/life-fhir-gen-$(version)/life-fhir-gen: script/life-fhir-gen.tpl.sh
	dirname $@ | xargs mkdir -p
	sed -e 's/<VERSION>/$(version)/' $< > $@
	chmod +x $@

target/linux/life-fhir-gen-$(version).tar.gz: target/life-fhir-gen-$(version)-standalone.jar target/linux/life-fhir-gen-$(version)/life-fhir-gen
	cd target; cp life-fhir-gen-$(version)-standalone.jar linux/life-fhir-gen-$(version)
	cd target/linux; tar czf life-fhir-gen-$(version).tar.gz life-fhir-gen-$(version)

all: target/win/life-fhir-gen-$(version).zip target/linux/life-fhir-gen-$(version).tar.gz

clean:
	lein clean

.PHONY: clean all
