.SUFFIXES : .java .class

JC = javac

JFLAGS = -g \
	-Xdiags:verbose \


.java.class:
	$(JC) $(JFLAGS) $*.java

CLASSES = \
	MKTW_ADPT.java \


default: classes


classes: $(CLASSES:.java=.class)


clean:
	rm -f *.class

run:
	java MKTW_ADPT
