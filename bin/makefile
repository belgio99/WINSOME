
JC = javac
JAVA_SRC_DIR = src
JAVA_CP_DIR = "../lib/gson-2.8.9.jar"
STORAGE_DIR = tmp

javac:
	$(JC) -cp $(JAVA_CP_DIR) *.java

cp_sampledata:
	cp sampledata/*.json $(STORAGE_DIR)

.PHONY = clean reset

clean:
	rm -rf *.class

reset:
	rm -rf $(STORAGE_DIR)/userdb.json $(STORAGE_DIR)/postdb.json $(STORAGE_DIR)/globaltagslist.json
