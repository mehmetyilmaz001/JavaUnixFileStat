/*
 * Native methods for collecting file information on a Linux/Unix system.
 * COMPILATION: Include the following command line options when compiling this code.
 * If JAVA_HOME is not defined, substitute the full path of the Java home directory.
 *
 * -I$JAVA_HOME/include -I$JAVA_HOME/include/linux
 *
 * If you are not running Linux the second directory may have a different name.
 *
 * Author: Hakan Soderstrom, www.soderstrom.se
 */
#include <stdio.h>
#include <errno.h>
#include <sys/stat.h>
#include <unistd.h>
#include <sys/types.h>
#include <jni.h>
#include "se_soderstrom_linux_FileStat.h"

#define FIELD_LONG "J"
#define FIELD_INT "I"

/*
 * Set an int instance field.
 * RETURN 0 on success, -1 on failure.
 */
int assignInt(JNIEnv *env, jclass cls, jobject obj, char *fieldName, int value) {
  jfieldID fid = (*env)->GetFieldID(env, cls, fieldName, FIELD_INT);
  if (fid == NULL) return -1;
  (*env)->SetIntField(env, obj, fid, value);
  return 0;
}

/*
 * Set a long int instance field.
 * RETURN 0 on success, -1 on failure.
 */
int assignLong(JNIEnv *env, jclass cls, jobject obj, char *fieldName, long int value) {
  jfieldID fid = (*env)->GetFieldID(env, cls, fieldName, FIELD_LONG);
  if (fid == NULL) return -1;
  (*env)->SetLongField(env, obj, fid, value);
  return 0;
}

/*
 * Collect file information
 *
 * @param env Pointer to the JVM environment.
 * @param jc Class to which the method belongs.
 * @param path Must be the path of the file to examine.
 * @param lstat determines if stat or lstat is invoked.
 * Zero means 'stat', a value > 0 means 'lstat'.
 * @return an integer error code.
 * The return code contains the errno > 0 if the system call fails.
 * The return code is -1 in case of Java out of memory, -2 on various
 * other errors probably indicating a bug in this code.
 */
JNIEXPORT jint JNICALL Java_se_soderstrom_linux_FileStat_doStat
(JNIEnv *env, jclass cls, jstring path, jobject obj, jint lstatFlag)
{
  struct stat buf;
  int flag;

  /* Run the system call given the path */
  const char *c_path = (*env)->GetStringUTFChars(env, path, NULL);
  if (c_path == NULL) return -1; /* OutOfMemoryError already thrown */
  int ret = lstatFlag? lstat(c_path, &buf) : stat(c_path, &buf);
  (*env)->ReleaseStringUTFChars(env, path, c_path);
  if (ret < 0) return errno;

  /* All file info is now in buf. Transfer to instance fields. */
  if (assignLong(env, cls, obj, "device", buf.st_dev) < 0) return -2;
  if (assignLong(env, cls, obj, "inode", buf.st_ino) < 0) return -2;
  if (assignInt(env, cls, obj, "protection", buf.st_mode & 07777) < 0) return -2;
  if (assignLong(env, cls, obj, "nlink", buf.st_nlink) < 0) return -2;
  if (assignInt(env, cls, obj, "uid", buf.st_uid) < 0) return -2;
  if (assignInt(env, cls, obj, "gid", buf.st_gid) < 0) return -2;
  if (assignLong(env, cls, obj, "rdev", buf.st_rdev) < 0) return -2;
  if (assignLong(env, cls, obj, "size", buf.st_size) < 0) return -2;
  if (assignLong(env, cls, obj, "blksize", buf.st_blksize) < 0) return -2;
  if (assignLong(env, cls, obj, "blocks", buf.st_blocks) < 0) return -2;
  flag = S_ISREG(buf.st_mode);
  if (assignInt(env, cls, obj, "isRegInt", flag) < 0) return -2;
  flag = S_ISDIR(buf.st_mode);
  if (assignInt(env, cls, obj, "isDirInt", flag) < 0) return -2;
  flag = S_ISCHR(buf.st_mode);
  if (assignInt(env, cls, obj, "isChrInt", flag) < 0) return -2;
  flag = S_ISBLK(buf.st_mode);
  if (assignInt(env, cls, obj, "isBlkInt", flag) < 0) return -2;
  flag = S_ISFIFO(buf.st_mode);
  if (assignInt(env, cls, obj, "isFifoInt", flag) < 0) return -2;
  flag = S_ISLNK(buf.st_mode);
  if (assignInt(env, cls, obj, "isLnkInt", flag) < 0) return -2;
  flag = S_ISSOCK(buf.st_mode);
  if (assignInt(env, cls, obj, "isSockInt", flag) < 0) return -2;
  /* Time stamps */
  if (assignLong(env, cls, obj, "atimeSecs", buf.st_atim.tv_sec) < 0) return -2;
  if (assignLong(env, cls, obj, "atimeNanos", buf.st_atim.tv_nsec) < 0) return -2;
  if (assignLong(env, cls, obj, "mtimeSecs", buf.st_mtim.tv_sec) < 0) return -2;
  if (assignLong(env, cls, obj, "mtimeNanos", buf.st_mtim.tv_nsec) < 0) return -2;
  if (assignLong(env, cls, obj, "ctimeSecs", buf.st_ctim.tv_sec) < 0) return -2;
  if (assignLong(env, cls, obj, "ctimeNanos", buf.st_ctim.tv_nsec) < 0) return -2;

  return 0;
}

/*
 * Check if a path is a symbolic link.
 */
JNIEXPORT jint JNICALL Java_se_soderstrom_linux_FileStat_doSymLink
(JNIEnv *env, jclass cls, jstring path)
{
  struct stat buf;

  /* Run the system call given the path */
  const char *c_path = (*env)->GetStringUTFChars(env, path, NULL);
  if (c_path == NULL) return -1; /* OutOfMemoryError already thrown */
  int ret = lstat(c_path, &buf);
  (*env)->ReleaseStringUTFChars(env, path, c_path);
  if (ret < 0) return errno + 1000;

  /* Maybe overly cautious but the macro definition is not very transparent */
  return (S_ISLNK(buf.st_mode))? 1 : 0;
}
