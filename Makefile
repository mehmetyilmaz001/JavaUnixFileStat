#
# This Makefile produces the executables for the FileStat utility
# Requires make and gcc
# The JAVA_HOME environment variable must identify the JDK installation
# directory.
# After 'make all' you must install the resulting lib/libfilestat.so
# in a directory in java.library.path
# If in doubt, run the TestFileStat program. It will terminate with an
# exception where acceptable directories are listed.
#
JDKINCLUDES = -I${JAVA_HOME}/include -I${JAVA_HOME}/include/linux -I.
CLASSDIR = class
CP = -classpath ${CLASSDIR}
LIB = lib
JAR = ${LIB}/filestat.jar
MANIFEST = MANIFEST.MF
NATIVEINCL = se_soderstrom_linux_FileStat.h
NATIVELIB = ${LIB}/libfilestat.so
JAVADIR = se/soderstrom/linux
CDIR = native
CC = gcc

all : initdirs ${JAR} ${NATIVELIB}

initdirs :
	mkdir -p class lib

${JAR} : class/${JAVADIR}/*.class
	jar cmf ${MANIFEST} ${JAR} -C ${CLASSDIR} .

class/${JAVADIR}/*.class : ${JAVADIR}/*.java
	javac ${CP} -d ${CLASSDIR} ${JAVADIR}/*.java

${NATIVELIB} : ${NATIVEINCL} ${CDIR}/filestat.c
	${CC} ${JDKINCLUDES} -o ${NATIVELIB} -fPIC -shared ${CDIR}/filestat.c

${NATIVEINCL} : class/${JAVADIR}/FileStat.class
	rm -f ${NATIVEINCL}
	javah ${CP} se.soderstrom.linux.FileStat

clean :
	rm ${JAR} ${NATIVELIB} ${NATIVEINCL}
	rm -rf ${CLASSDIR}/se
