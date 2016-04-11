package se.soderstrom.linux;

import java.util.HashMap;

/**
 * Exception thrown when a Linux/Unix system call returns with error.
 * Translates the traditional errno to a readable message.
 * NOTE that the translation may be system dependent.
 *
 * @author Hakan Soderstrom, www.soderstrom.se
 */
public class SyscallException extends RuntimeException {
    // Linux/Unix errno
    public final int errno;

    // Linux/Unix error name
    public final String errname;

    // Linux/Unix error description
    public final String errdescr;

    public SyscallException(String message) {
	super(message);
	errno = 0;
	errname = "";
	errdescr = "";
    }

    public SyscallException(String message, int errno) {
	super(message);
	this.errno = errno;
	String[] errStrings = errMap.get(errno);

	if (errStrings != null) {
	    errname = errStrings[0];
	    errdescr = errStrings[1];
	} else {
	    errname = "*UNKNOWN*";
	    errdescr = "Unknown error number (" + errno + ")";
	}
    }

    public String toString() {
	return (errno != 0)?
	    (getMessage() + ": System call returned " + errname + " / " + errdescr) :
	    getMessage();
    }

    public static final int EPERM =		 1; /* Operation not permitted */
    public static final int ENOENT =		 2; /* No such file or directory */
    public static final int ESRCH =		 3; /* No such process */
    public static final int EINTR =		 4; /* Interrupted system call */
    public static final int EIO =		 5; /* I/O error */
    public static final int ENXIO =		 6; /* No such device or address */
    public static final int E2BIG =		 7; /* Argument list too long */
    public static final int ENOEXEC =		 8; /* Exec format error */
    public static final int EBADF =		 9; /* Bad file number */
    public static final int ECHILD =		10; /* No child processes */
    public static final int EAGAIN =		11; /* Try again */
    public static final int ENOMEM =		12; /* Out of memory */
    public static final int EACCES =		13; /* Permission denied */
    public static final int EFAULT =		14; /* Bad address */
    public static final int ENOTBLK =		15; /* Block device required */
    public static final int EBUSY =		16; /* Device or resource busy */
    public static final int EEXIST =		17; /* File exists */
    public static final int EXDEV =		18; /* Cross-device link */
    public static final int ENODEV =		19; /* No such device */
    public static final int ENOTDIR =		20; /* Not a directory */
    public static final int EISDIR =		21; /* Is a directory */
    public static final int EINVAL =		22; /* Invalid argument */
    public static final int ENFILE =		23; /* File table overflow */
    public static final int EMFILE =		24; /* Too many open files */
    public static final int ENOTTY =		25; /* Not a typewriter */
    public static final int ETXTBSY =		26; /* Text file busy */
    public static final int EFBIG =		27; /* File too large */
    public static final int ENOSPC =		28; /* No space left on device */
    public static final int ESPIPE =		29; /* Illegal seek */
    public static final int EROFS =		30; /* Read-only file system */
    public static final int EMLINK =		31; /* Too many links */
    public static final int EPIPE =		32; /* Broken pipe */
    public static final int EDOM =		33; /* Math argument out of domain of func */
    public static final int ERANGE =		34; /* Math result not representable */
    public static final int ENAMETOOLONG =	36; /* File name too long */
    public static final int ELOOP =		40; /* Too many symbolic links encountered */
 
    private static HashMap<Integer,String[]> errMap = new HashMap<Integer,String[]>();

    static {
	errMap.put(0, new String[] {"OK", "Operation successful"});
	errMap.put(EPERM, new String[] {"EPERM", "Operation not permitted"});
	errMap.put(ENOENT, new String[] {"ENOENT", "A component of the path path does not exist, or the path is an empty string"});
	errMap.put(ESRCH, new String[] {"ESRCH", "No such process"});
	errMap.put(EINTR, new String[] {"EINTR", "Interrupted system call"});
	errMap.put(EIO, new String[] {"EIO", "I/O error"});
	errMap.put(ENXIO, new String[] {"ENXIO", "No such device or address"});
	errMap.put(E2BIG, new String[] {"E2BIG", "Argument list too long"});
	errMap.put(ENOEXEC, new String[] {"ENOEXEC", "Exec format error"});
	errMap.put(EBADF, new String[] {"EBADF", "Bad file number"});
	errMap.put(ECHILD, new String[] {"ECHILD", "No child processes"});
	errMap.put(EAGAIN, new String[] {"EAGAIN", "Try again"});
	errMap.put(ENOMEM, new String[] {"ENOMEM", "Out of memory (i.e. kernel memory)"});
	errMap.put(EACCES, new String[] {"EACCES", "Search permission is denied for one of the directories in the path prefix of path"});
	errMap.put(EFAULT, new String[] {"EFAULT", "Bad address"});
	errMap.put(ENOTBLK, new String[] {"ENOTBLK", "Block device required"});
	errMap.put(EBUSY, new String[] {"EBUSY", "Device or resource busy"});
	errMap.put(EEXIST, new String[] {"EEXIST", "File exists"});
	errMap.put(EXDEV, new String[] {"EXDEV", "Cross-device link"});
	errMap.put(ENODEV, new String[] {"ENODEV", "No such device"});
	errMap.put(ENOTDIR, new String[] {"ENOTDIR", "A component of the path is not a directory"});
	errMap.put(EISDIR, new String[] {"EISDIR", "Is a directory"});
	errMap.put(EINVAL, new String[] {"EINVAL", "Invalid argument"});
	errMap.put(ENFILE, new String[] {"ENFILE", "File table overflow"});
	errMap.put(EMFILE, new String[] {"EMFILE", "Too many open files"});
	errMap.put(ENOTTY, new String[] {"ENOTTY", "Not a typewriter"});
	errMap.put(ETXTBSY, new String[] {"ETXTBSY", "Text file busy"});
	errMap.put(EFBIG, new String[] {"EFBIG", "File too large"});
	errMap.put(ENOSPC, new String[] {"ENOSPC", "No space left on device"});
	errMap.put(ESPIPE, new String[] {"ESPIPE", "Illegal seek"});
	errMap.put(EROFS, new String[] {"EROFS", "Read-only file system"});
	errMap.put(EMLINK, new String[] {"EMLINK", "Too many links"});
	errMap.put(EPIPE, new String[] {"EPIPE", "Broken pipe"});
	errMap.put(EDOM, new String[] {"EDOM", "Math argument out of domain of func"});
	errMap.put(ERANGE, new String[] {"ERANGE", "Math result not representable"});
	errMap.put(ENAMETOOLONG, new String[] {"ENAMETOOLONG", "File name too long"});
	errMap.put(ELOOP, new String[] {"ELOOP", "Too many symbolic links encountered"});
    };
}
