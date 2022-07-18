JFLAGS = -g
JC = javac


#
# Java
#

# Compile Java source files
javac:
	$(JC) $(JFLAGS) -d $(JAVA_OBJ_DIR) $(JAVA_SRC_DIR)/*.java

# Run Java program
java:
	$(JC) $(JFLAGS) -d $(JAVA_OBJ_DIR) $(JAVA_SRC_DIR)/*.java
	$(JAVA_OBJ_DIR)/$(JAVA_MAIN_CLASS).class
	java -cp $(JAVA_OBJ_DIR) $(JAVA_MAIN_CLASS)

