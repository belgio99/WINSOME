JAVA_OUT_DIR = bin
JC = javac
JAVA_SRC_DIR = src
JAVA_CP_DIR = "lib/*:bin:bin/*"
JAVA_CP_WIN = "lib/*;bin;bin/*"
STORAGE_DIR = /tmp

javac:
	$(JC) -d $(JAVA_OUT_DIR) -cp $(JAVA_CP_DIR) $(JAVA_SRC_DIR)/*.java $(JAVA_SRC_DIR)/*/*.java
	cp $(JAVA_SRC_DIR)/*.txt $(JAVA_OUT_DIR)

javac_win:
	$(JC) -d $(JAVA_OUT_DIR) -cp $(JAVA_CP_WIN) $(JAVA_SRC_DIR)/*.java $(JAVA_SRC_DIR)/*/*.java
	copy $(JAVA_SRC_DIR)/*.txt $(JAVA_OUT_DIR) /Y

cp_sampledata:
	cp $(JAVA_SRC_DIR)/sampledata/*.json $(STORAGE_DIR)

.PHONY = clean reset

clean:
	rm -rf $(JAVA_OUT_DIR)

reset:
	rm -rf $(STORAGE_DIR)/userdb.json $(STORAGE_DIR)/postdb.json $(STORAGE_DIR)/globaltagslist.json
