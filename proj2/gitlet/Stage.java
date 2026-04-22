package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Stage implements Serializable {
    /** Files staged for addition: name -> blobID */
    private final Map<String, String> addStage;
    /** Files staged for removal: name */
    private final Set<String> removeStage;

    public Stage() {
        addStage = new HashMap<>();
        removeStage = new HashSet<>();
    }

    public Map<String, String> getAddStage() { return addStage; }
    public Set<String> getRemoveStage() { return removeStage; }

    /** finish the work of add command. */
    public void addFile(String name) {
        Blob b = new Blob(Utils.join(Repository.CWD, name));
        String curID = b.getID();
        String blobInCommit = blobInCommit(name);
        if (blobInCommit != null && blobInCommit.equals(curID)) {
            addStage.remove(name);/// if the file in the cwd identify with the one in current commit
        } else {
            b.save();
            addStage.put(name, curID);
        }
        removeStage.remove(name);
        save();
    }

    /** add an existed file */
    public void addBlob(String name, String id) {
        addStage.put(name, id);
        save();
    }

    /** finish the work of rm command. */
    public void removeFile(String name) {
        boolean stagedForAdd = addStage.containsKey(name);
        boolean tracked = haveFile(name);
        if (!stagedForAdd && !tracked) {
            Utils.exitWithMessage("No reason to remove the file.");
        }
        if (stagedForAdd) {
            addStage.remove(name);
        }
        if (tracked) {
            removeStage.add(name);
            File f = Utils.join(Repository.CWD, name);
            Utils.restrictedDelete(f);
        }
        save();
    }

    /** Save the staging area. */
    private void save() {
        Utils.writeObject(Repository.STAGE, this);
    }

    /** Return the blob id of a file is in the current commit, return null if not exist. */
    private String blobInCommit(String name) {
        Commit c = Repository.getCurCommit();
        return c.getBlobID(name);
    }

    /** Return True if the current commit have file. */
    private boolean haveFile(String name) {
        Commit c = Repository.getCurCommit();
        return c.haveFile(name);
    }

    public void clear() {
        addStage.clear();
        removeStage.clear();
        save();
    }

    public boolean isEmpty() {
        return addStage.isEmpty() && removeStage.isEmpty();
    }

    public void unstage(String name) {
        addStage.remove(name);
        removeStage.remove(name);
        save();
    }
}
