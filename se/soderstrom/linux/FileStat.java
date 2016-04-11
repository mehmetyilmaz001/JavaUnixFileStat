package se.soderstrom.linux;

import java.io.File;
import java.sql.Timestamp;

/**
 * Class for collecting file information on a Linux/Unix system.
 * Depends on native code that has to be compiled on the system.
 * All methods throw SyscallException if the operating system does not
 * allow file information to be collected.
 * This is a RuntimeException that doesn't have to be declared.
 * A FileStat object is mutable, meaning that you may reuse it.
 *
 * @author Hakan Soderstrom, www.soderstrom.se
 */
public class FileStat {
    public static final String LIBRARY_ID = "filestat";
    public static final String LIBRARY_FILE_NAME = "lib" + LIBRARY_ID + ".so";

    public FileStat() {
	path = null;
    }

    public FileStat(String path, boolean followLinks) {
	stat(path, followLinks);
    }

    public FileStat(String path) {
	this(path, true);
    }

    public FileStat(File file, boolean followLinks) {
	this(file.getPath(), followLinks);
    }

    public FileStat(File file) {
	this(file, true);
    }

    /**
     * Collect information for a file.
     *
     * @param path must be the file path to examine.
     * @param followLinks true collects information following symbolic links,
     * false does not follow symbolic links.
     * The latter case is necessary for collecting information about
     * a symbolic link itself.
     * SIDE EFFECT: Sets the fields of this instance.
     */
    public void stat(String path, boolean followLinks) {
	this.path = path;
	int returnCode = doStat(path, this, followLinks? 0 : 1);

	// On error the return code is a negated errno
	if (returnCode > 0) throw new SyscallException("Could not stat " + path, returnCode);
	if (returnCode < 0) throw new RuntimeException("INTERNAL CONFLICT in native method");
    }

    public void stat(String path) {
	stat(path, true);
    }

    /**
     * Is a given file path a symbolic link?
     *
     * @param path must be the file path to examine.
     * @returns true if the file is a symbolic link, false otherwise.
     */
    public static boolean isSymLink(String path) {
	int returnCode = doSymLink(path);

	// Check if the return code signals failure.
	if (returnCode > 1000)
	    throw new SyscallException("Could not stat " + path, returnCode - 1000);
	if (returnCode < 0) throw new RuntimeException("INTERNAL CONFLICT in native method");
	return returnCode > 0;
    }

    // Path of file being queried
    private String path;
    String getPath() {return path;}

    // ID of device containing file
    private long device;
    public long getDevice() {return device;}

    // Inode number
    private long inode;
    public long getInode() {return inode;}

    // Protection (access control) bits the Unix way
    private int protection;
    public int getProtection() {return protection;}

    // Protection conventionally formatted
    public String formatProtection() {return String.format("%04o", protection);}

    // Number of hard link
    private long nlink;
    public long getNlink() {return nlink;}

    // User ID of owner
    private int uid;
    public int getUid() {return uid;}

    // Group ID of owner
    private int gid;
    public int getGid() {return gid;}

    // Device ID (if special file)
    private long rdev;
    public long getRdev() {return rdev;}

    // Device type conventionally formatted
    // Naively assuming 8 bit minor
    public String formatRdev() {
	String result;

	if (rdev > 0) {
	    int major = (int)(rdev >> 8);
	    int minor = (int)(rdev & 0xff);
	    result = major + "," + minor;
	} else {
	    result = "-,-";
	}

	return result;
    }

    // Total size, in bytes
    private long size;
    public long getSize() {return size;}

    // Format size with thousand separators according to
    // the default locale
    public String formatSize() {return String.format("%,d", size);}

    // Blocksize for filesystem I/O
    private long blksize;
    public long getBlksize() {return blksize;}

    // Number of blocks allocated
    private long blocks;
    public long getBlocks() {return blocks;}

    // Is this a regular file?
    private int isRegInt;
    public boolean isReg() {return isRegInt > 0;}

    // Is this a directory?
    private int isDirInt;
    public boolean isDir() {return isDirInt > 0;}

    // Is this a character device?
    private int isChrInt;
    public boolean isChr() {return isChrInt > 0;}

    // Is this a block device?
    private int isBlkInt;
    public boolean isBlk() {return isBlkInt > 0;}

    // Is this a FIFO (named pipe)?
    private int isFifoInt;
    public boolean isFifo() {return isFifoInt > 0;}

    // Is this a symbolic link?
    private int isLnkInt;
    public boolean isLnk() {return isLnkInt > 0;}

    // Is this a socket?
    private int isSockInt;
    public boolean isSock() {return isSockInt > 0;}

    // File timestamps are natively represented by two fields, seconds and nanos
    private long millis(long seconds, long nanos) {
	// Plain integer division truncates rather than round the nanos
	return seconds * 1000L + nanos/1000000L;
    }

    // The java.sql.Timestamp class includes nanos in a kludgy way
    private Timestamp toTimestamp(long seconds, long nanos) {
	Timestamp tstamp = new Timestamp(seconds * 1000L);
	tstamp.setNanos((int)nanos);
	return tstamp;
    }

    // Time of last access
    private long atimeSecs;
    private long atimeNanos;
    public long getAtimeSecs() {return atimeSecs;}
    public long getAtimeNanos() {return atimeNanos;}
    // Get atime millis, nanos are truncated not rounded
    public long getAtimeMillis() {return millis(atimeSecs, atimeNanos);}
    public Timestamp getAtime() {return toTimestamp(atimeSecs, atimeNanos);}

    // Time of last modification
    private long mtimeSecs;
    private long mtimeNanos;
    public long getMtimeSecs() {return mtimeSecs;}
    public long getMtimeNanos() {return mtimeNanos;}
    // Get mtime millis, nanos are truncated not rounded
    public long getMtimeMillis() {return millis(mtimeSecs, mtimeNanos);}
    public Timestamp getMtime() {return toTimestamp(mtimeSecs, mtimeNanos);}

    // Time of last status change
    private long ctimeSecs;
    private long ctimeNanos;
    public long getCtimeSecs() {return ctimeSecs;}
    public long getCtimeNanos() {return ctimeNanos;}
    // Get ctime millis, nanos are truncated not rounded
    public long getCtimeMillis() {return millis(ctimeSecs, ctimeNanos);}
    public Timestamp getCtime() {return toTimestamp(ctimeSecs, ctimeNanos);}

    public String toString() {
	final String NL = System.getProperty("line.separator");
	final String INDENT = "  ";
	StringBuilder sb = new StringBuilder();
	sb.append("Stat ").append(path).append(" (").append(NL);
	sb.append(INDENT).append("device        : ").append(device).append(NL);
	sb.append(INDENT).append("inode         : ").append(inode).append(NL);
	sb.append(INDENT).append("protection    : ").append(formatProtection()).append(NL);
	sb.append(INDENT).append("hard links    : ").append(nlink).append(NL);
	sb.append(INDENT).append("uid           : ").append(uid).append(NL);
	sb.append(INDENT).append("gid           : ").append(gid).append(NL);
	sb.append(INDENT).append("device type   : ").append(formatRdev()).append(NL);
	sb.append(INDENT).append("size          : ").append(formatSize()).append(NL);
	sb.append(INDENT).append("block size    : ").append(blksize).append(NL);
	sb.append(INDENT).append("blocks        : ").append(blocks).append(NL);
	sb.append(INDENT).append("regular file? : ").append(isReg()).append(NL);
	sb.append(INDENT).append("directory?    : ").append(isDir()).append(NL);
	sb.append(INDENT).append("char device?  : ").append(isChr()).append(NL);
	sb.append(INDENT).append("block device? : ").append(isBlk()).append(NL);
	sb.append(INDENT).append("fifo?         : ").append(isFifo()).append(NL);
	sb.append(INDENT).append("symlink?      : ").append(isLnk()).append(NL);
	sb.append(INDENT).append("socket?       : ").append(isSock()).append(NL);
	sb.append(INDENT).append("accessed      : ").append(getAtime()).append(NL);
	sb.append(INDENT).append("modified      : ").append(getMtime()).append(NL);
	sb.append(INDENT).append("status change : ").append(getCtime()).append(NL);
	sb.append(")");
	return sb.toString();
    }

    /**
     * Natively collect file information resolving any symbolic links.
     * @param path must be the file path to examine.
     * @param fileStat must be a FileStat object to receive the file information.
     * @param lstat must be 0 to invoke 'stat', 1 (or anything greater than 0)
     * to invoke 'lstat'.
     * @returns 0 on success, errno on failure, negative indicating internal conflict.
     * SIDE EFFECT: Sets many fields of the FileStat object.
     */
    private native static int doStat(String path, FileStat fileStat, int lstat);

    /**
     * Natively check a file path for being a symbolic link.
     * @param path must be the file path to examine.
     * @returns 1 if the path is a symbolic link, 0 if it is not.
     * Returns errno + 1000 on failure, or a negative number on internal conflict.
     */
    private native static int doSymLink(String path);

    static {
	try {
	    // The file name of the native library must be "libfilestat.so"
	    // It must be located in one of the directories mentioned in
	    // java.library.path or /etc/ld.so.conf
	    System.loadLibrary(LIBRARY_ID);
	    // Alternatively, use System.load("<real file path>")
	    // where the file name must pinpoint the library exactly.
	} catch (UnsatisfiedLinkError err) {
	    System.err.println(err);
	    System.err.println("TIP: Try naming the dynamic library " + LIBRARY_FILE_NAME +
			       " and put it in one of the following directories: " +
			       System.getProperty("java.library.path"));
	    throw err;
	}
    }
}
