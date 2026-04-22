package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.*;

/** Represents a gitlet commit object.
 *  @author zcd3
 */
public class Commit implements Serializable {

    /** The message of this Commit. */
    private final String message;
    /** The date of this Commit. */
    private final Date date;
    /** The parent commit of this Commit. */
    private final String parent;
    private final String secondParent;
    private final Map<String, String> pathToBlobs;

    /** Default constructor */
    public Commit() {
        message = "initial commit";
        date = new Date(0);
        parent = null;
        secondParent = null;
        pathToBlobs = new HashMap<>();
    }

    /** Normal constructor */
    public Commit(String message, String secondParent) {
        this.message = message;
        date = new Date();
        parent = Repository.getCurCommitID();
        this.secondParent = secondParent;
        pathToBlobs = new HashMap<>();
    }

    /** Get the ID of this Commit and store it. */
    public String getID() {
        String id = Utils.sha1(Utils.serialize(this));
        File c = Utils.join(Repository.getCommitDir(), id);
        Utils.writeObject(c, this);
        return id;
    }

    /** GetFile method for checkout command. */
    public String getFile(String name) {
        if (haveFile(name)) {
            return pathToBlobs.get(name);
        }
        Utils.exitWithMessage("File does not exist in that commit.");
        return null;
    }

    public Map<String, String> getPathToBlobs() {
        return pathToBlobs;
    }

    /** GetFile method for add command. */
    public String getBlobID(String name) {
        if (haveFile(name)) {
            return pathToBlobs.get(name);
        } else {
            return null;
        }
    }

    public boolean haveFile(String name) {
        return pathToBlobs.containsKey(name);
    }

    /** Get the parent Commit. */
    public Commit getParent() {
        return Repository.readCommit(parent);
    }

    /** Get the parent CommitID. */
    public String getParentID() {
        return parent;
    }

    public String getSecondParentID() {
        return secondParent;
    }

    /** Get the message of the Commit. */
    public String getMessage() {
        return message;
    }

    /** Update the stage area to a new commit. */
    public void clearStage() {
        Stage stg = Repository.getStage();
        Map<String, String> addStage = stg.getAddStage();
        Set<String> removeStage = stg.getRemoveStage();
        pathToBlobs.putAll(addStage);
        Map<String, String> parentMap = getParent().getPathToBlobs();
        for (Map.Entry<String, String> e : parentMap.entrySet()) {
            if (!removeStage.contains(e.getKey()) && !pathToBlobs.containsKey(e.getKey())) {
                pathToBlobs.put(e.getKey(), e.getValue());
            }
        }
        stg.clear();
    }

    public static String formatDate(Date d) {
        Formatter f = new Formatter(Locale.US);
        f.format("%1$ta %1$tb %1$te %1$tT %1$tY %1$tz", d);
        String s = f.toString();
        f.close();
        return s;
    }

    public void print(String id) {
        System.out.println("===");
        System.out.println("commit " + id);
        if (secondParent != null) {
            System.out.println("Merge: " + abbrev(parent) + " " + abbrev(secondParent));
        }
        System.out.println("Date: " + formatDate(date));
        System.out.println(message);
        System.out.println();
    }

    /** Get the first seven digits of an id. */
    public static String abbrev(String fullId) {
        if (fullId == null) {
            return null;
        }
        int n = Math.min(7, fullId.length());
        return fullId.substring(0, n);
    }
}
