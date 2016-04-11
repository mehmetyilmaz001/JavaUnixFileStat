package se.soderstrom.linux;

import java.io.File;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import se.soderstrom.linux.FileStat;

/**
 * Class for manually testing the FileStat class.
 * Invokes FileStat methods, prints the result, then invokes
 * the stat(1) command and prints its output.
 * You must verify the outcome by comparing the printouts.
 */
public class TestFileStat {
    public static final String FMT = "yyyy-MM-dd HH:mm:ss.SSS";
    public static final String STAT = "stat";

    /**
     * Collect file information for any number of files.
     *
     * @param args must be a list of paths to examine.
     * SIDE EFFECT: Prints the result on standard out.
     */
    public static void main(String[] args) {
	if (args.length == 0) System.out.println("NOTE: The program needs one or more file paths to examine");

	for (String path : args) {
	    boolean isSymLink = FileStat.isSymLink(path);
	    System.out.println("Is symlink: " + isSymLink);
	    FileStat fs = new FileStat(path);
	    System.out.println(fs);
	    printMillis("  atime millis: ", fs.getAtimeMillis());
	    printMillis("  mtime millis: ", fs.getMtimeMillis());
	    printMillis("  ctime millis: ", fs.getCtimeMillis());
	    System.out.println("---");
	    statCommand(path);
	    System.out.println("=================");
	}
    }

    private static void printMillis(String message, long millis) {
	SimpleDateFormat fmt = new SimpleDateFormat(FMT);
	System.out.println(message + fmt.format(new Date(millis)));
    }

    // Run the OS 'stat' command on a path to verify our own findings
    private static void statCommand(String path) {
	ProcessBuilder pb = new ProcessBuilder(STAT, path);

	try {
	    Process pr = pb.start();
	    BufferedReader br = new BufferedReader(new InputStreamReader(pr.getInputStream()));
	    String line;

	    while ((line = br.readLine()) != null) {
		System.out.println(line);
	    }
	} catch (IOException exc) {
	    System.err.println("OOPS! 'stat path' caused this: " + exc);
	}
    }
}
