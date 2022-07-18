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

# Run Java program with arguments
java_args:
	$(JC) $(JFLAGS) -d $(JAVA_OBJ_DIR) $(JAVA_SRC_DIR)/*.java
	$(JAVA_OBJ_DIR)/$(JAVA_MAIN_CLASS).class
	java -cp $(JAVA_OBJ_DIR) $(JAVA_MAIN_CLASS) $(JAVA_ARGS)

# Run Java program with arguments and profiling
java_args_profile:
	$(JC) $(JFLAGS) -d $(JAVA_OBJ_DIR) $(JAVA_SRC_DIR)/*.java
	$(JAVA_OBJ_DIR)/$(JAVA_MAIN_CLASS).class
	java -cp $(JAVA_OBJ_DIR) $(JAVA_MAIN_CLASS) $(JAVA_ARGS)
	java -cp $(JAVA_OBJ_DIR) $(JAVA_MAIN_CLASS) $(JAVA_ARGS) -XX:+UnlockDiagnosticVMOptions -XX:+PrintAssembly -XX:+PrintInlining -XX:+PrintOptimizationDetails -XX:+PrintMethodTimes -XX:+PrintNMethods -XX:+PrintAssembly -XX:+PrintCompilation -XX:+PrintInlining -XX:+PrintOptimizationDetails -XX:+PrintMethodTimes -XX:+PrintNMethods -XX:+Print