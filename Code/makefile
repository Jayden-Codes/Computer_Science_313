JFLAGS = -g
JC = javac

.SUFFIXES: .java .class

.java.class:
	$(JC) $(JFLAGS) $*.java

CLASSES = \
		  Server.java \
		  Client.java \

compile: clean $(CLASSES:.java=.class) run

clean:
	del *.class &

runServer: 
	- start /b cmd /c java Server &
runClient1: 
	- start /b cmd /c java Client &
runClient2: 
	- start /b cmd /c java Client &

run: runServer runClient1 runClient2





