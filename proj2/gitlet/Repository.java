package gitlet;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static gitlet.Utils.*;

/** Represents a gitlet repository.
 *  Most commands’ core logic will live here (or be dispatched from here to other classes).
 *  @author zcd3
 */
public class Repository {
    /**
     * Persistence
     * CWD
     * └── .gitlet
     *     ├── HEAD
     *     ├── stage
     *     ├── commits
     *     │   ├── <commitId1>
     *     │   ├── <commitId2>
     *     │   └── ...
     *     ├── blobs
     *     │   ├── <blobId1>
     *     │   ├── <blobId2>
     *     │   └── ...
     *     └── refs
     *         └── heads
     *             ├── master
     *             ├── dev
     *             └── ...
     */

    /** The current working directory. */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /** The .gitlet directory. */
    /** The rest directories. */
    public static final File GITLET_DIR = join(CWD, ".gitlet");
    public static final File BLOB_DIR = join(GITLET_DIR, "blobs");
    public static final File COMMIT_DIR = join(GITLET_DIR, "commits");
    public static final File REFS_DIR = join(GITLET_DIR, "refs");
    public static final File HEAD_DIR = join(REFS_DIR, "heads");

    /** Files */
    public static final File HEAD = join(GITLET_DIR, "HEAD");
    public static final File STAGE = join(GITLET_DIR, "stage");

    /** init method */
    public static void init() {
        if (GITLET_DIR.exists()) {
            Utils.exitWithMessage("A Gitlet version-control system already exists in the current directory.");
        }

        /// initiallize
        GITLET_DIR.mkdir();
        BLOB_DIR.mkdir();
        COMMIT_DIR.mkdir();
        REFS_DIR.mkdir();
        HEAD_DIR.mkdir();
        writeObject(STAGE, new Stage());
        Commit init = new Commit();
        File master = join(HEAD_DIR, "master");
        writeContents(master, init.getID());
        writeContents(HEAD, "master");
    }

    /** Get the Stage Object. */
    public static Stage getStage() {
        return readObject(STAGE, Stage.class);
    }

    /** add method */
    public static void addFile(String filename) {
        File f = join(CWD, filename);
        if (!f.exists() || !f.isFile()) {
            exitWithMessage("File does not exist.");
        }
        Stage stg = getStage();
        stg.addFile(filename);
    }

    /** rm method */
    public static void removeFile(String filename) {
        Stage stg = getStage();
        stg.removeFile(filename);
    }

    /** Return the current CommitID */
    public static String getCurCommitID() {
        File b = join(HEAD_DIR, getCurHead());
        return readContentsAsString(b);
    }

    /** Return the current Commit */
    public static Commit getCurCommit() {
        return readCommit(getCurCommitID());
    }

    /** Return the branch name of the current head */
    public static String getCurHead() {
        return readContentsAsString(HEAD);
    }

    /** Update the current head with a new Commit ID. */
    private static void updateCurHead(String s) { writeContents(join(HEAD_DIR, getCurHead()), s);}
    /** Rewrite the current head. */
    private static void rewriteCurHead(String s) { writeContents(HEAD, s); }

    /** Create a new branch. */
    public static void createBranch(String name) {
        File b = join(HEAD_DIR, name);
        if (b.exists()) {
            exitWithMessage("A branch with that name already exists.");
        }
        writeContents(b, getCurCommitID());
    }

    /** Remove a branch. */
    public static void removeBranch(String name) {
        if (name.equals(getCurHead())) {
            exitWithMessage("Cannot remove the current branch.");
        }
        File h = join(HEAD_DIR, name);
        if (!h.exists()) {
            exitWithMessage("A branch with that name does not exist.");
        }
        h.delete();
    }

    /** Return a Commit from a commit id. */
    public static Commit readCommit(String s) {
        if (s == null) {
            return null;
        }
        File c = join(COMMIT_DIR, s);
        if (!c.exists()) {
            exitWithMessage("No commit with that id exists.");
        }
        return readObject(c, Commit.class);
    }

    public static byte[] readBlob(String s) {
        if (s == null) {
            return new byte[0];
        }
        File b = join(BLOB_DIR, s);
        return readContents(b);
    }

    /** commmit method */
    public static void commit(String message) {
        commit(message, null);
    }
    private static void commit(String message, String secondParent) {
        if (message.equals("")) {
            exitWithMessage("Please enter a commit message.");
        }
        if (getStage().isEmpty()) {
            exitWithMessage("No changes added to the commit.");
        }
        Commit c = new Commit(message, secondParent);
        c.clearStage();
        String ID = c.getID();
        updateCurHead(ID);
    }

    /** log method */
    public static void log() {
        String s = getCurCommitID();
        while (s != null) {
            Commit c = readCommit(s);
            c.print(s);
            s = c.getParentID();
        }
    }

    /** global-log method */
    public static void globalLog() {
        List<String> commits = plainFilenamesIn(COMMIT_DIR);
        if (commits == null) {
            return;
        }
        for (String s : commits) {
            File f = join(COMMIT_DIR, s);
            Commit c = readObject(f, Commit.class);
            c.print(s);
        }
    }

    /** find method */
    public static void find(String message) {
        List<String> commits = plainFilenamesIn(COMMIT_DIR);
        if (commits == null) {
            exitWithMessage("Found no commit with that message.");
        }
        boolean found = false;
        for (String s : commits) {
            Commit c = readObject(join(COMMIT_DIR, s), Commit.class);
            if (message.equals(c.getMessage())) {
                System.out.println(s);
                found = true;
            }
        }
        if (!found) {
            exitWithMessage("Found no commit with that message.");
        }
    }

    /** status method */
    public static void status() {
        /// branches part
        System.out.println("===  Branches ===");
        String curHead = getCurHead();
        List<String> branches = plainFilenamesIn(HEAD_DIR);
        System.out.println("*" + curHead);
        for (String b : branches) {
            if (!b.equals(curHead)) {
                System.out.println(b);
            }
        }
        System.out.println();

        Stage stg = getStage();
        Map<String, String> addStage = stg.getAddStage();
        Set<String> removeStage = stg.getRemoveStage();

        /// Normal Stage part
        System.out.println("=== Staged Files ===");
        List<String> addNames = new ArrayList<>(addStage.keySet());
        Collections.sort(addNames);
        for(String name : addNames) {
            System.out.println(name);
        }
        System.out.println();
        System.out.println("=== Removed Files ===");
        List<String> removeName = new ArrayList<>(removeStage);
        Collections.sort(removeName);
        for (String name : removeName) {
            System.out.println(name);
        }
        System.out.println();

        /// EC part
        Map<String, String> commits = getCurCommit().getPathToBlobs();
        List<String> cwdList = plainFilenamesIn(CWD);/// already sorted
        if (cwdList == null) { /// guard against NPE
            cwdList = Collections.emptyList();
        }
        Set<String> cwd = new HashSet<>(cwdList);

        System.out.println("=== Modifications Not Staged For Commit ===");
        List<String> modified = new ArrayList<>();
        for (String s : commits.keySet()) {
            if (addStage.containsKey(s)) { continue; }
            if (cwd.contains(s)) {
                if (!Arrays.equals(readBlob(commits.get(s)), readContents(join(CWD, s)))) {
                    modified.add(s + " (modified)");
                }
            } else {
                if (!removeStage.contains(s)) {
                    modified.add(s + " (deleted)");
                }
            }
        }
        for (String s : addNames) {
            if (cwd.contains(s)) {
                if (!Arrays.equals(readBlob(addStage.get(s)), readContents(join(CWD, s)))) {
                    modified.add(s + " (modified)");
                }
            } else {
                modified.add(s + " (deleted)");
            }
        }
        Collections.sort(modified);
        for (String s : modified) {
            System.out.println(s);
        }
        System.out.println();

        System.out.println("=== Untracked Files ===");
        for (String s : cwdList) {
            if (removeStage.contains(s) || (!addStage.containsKey(s) && !commits.containsKey(s))) {
                System.out.println(s);
            }
        }
        System.out.println();
    }

    /** reset method */
    public static void reset(String commitID) {
        String realID = resolveCommitId(commitID);
        checkoutToCommit(realID);
        updateCurHead(realID);
    }

    /** return the Commit ID of a given head. */
    private static String readHead(String head) {
        File b = join(HEAD_DIR, head);
        if (!b.exists()) {
            exitWithMessage("No such branch exists.");
        }
        if (getCurHead().equals(head)) {
            exitWithMessage("No need to checkout the current branch.");
        }
        return readContentsAsString(b);
    }

    /** checkout method */
    public static void checkoutBranch(String head) {
        checkoutToCommit(readHead(head));
        rewriteCurHead(head);
    }

    private static void checkoutToCommit(String id) {
        Stage stg = getStage();
        Map<String, String> newCommits = readCommit(id).getPathToBlobs();
        Map<String, String> commits = getCurCommit().getPathToBlobs();
        Map<String, String> addStage = stg.getAddStage();
        Set<String> removeStage = stg.getRemoveStage();
        List<String> cwdList = plainFilenamesIn(CWD);
        if (cwdList == null) { /// guard against NPE
            cwdList = Collections.emptyList();
        }
        for (String s : cwdList) {
            if ((removeStage.contains(s) || (!addStage.containsKey(s) && !commits.containsKey(s))) && newCommits.containsKey(s)) {
                exitWithMessage("There is an untracked file in the way; delete it, or add and commit it first.");
            }
        }

        for (Map.Entry<String, String> e : newCommits.entrySet()) {/// overwrite
            String fileName = e.getKey();
            String blobID = e.getValue();
            byte[] content = readBlob(blobID);
            File f = join(CWD, fileName);
            writeContents(f, content);
        }
        for (String s : commits.keySet()) {/// delete
            if (!newCommits.containsKey(s)) {
                if (removeStage.contains(s) && cwdList.contains(s)) {
                    continue;
                }
                restrictedDelete(join(CWD, s));
            }
        }
        stg.clear();
    }

    public static void checkoutFile(String name) {
        checkoutCommitFile(getCurCommitID(), name);
    }

    public static void checkoutCommitFile(String commitID, String name) {
        Commit c = readCommit(resolveCommitId(commitID));

        if (!c.haveFile(name)) {
            exitWithMessage("File does not exist in that commit.");
        }
        Map<String, String> commitMap = c.getPathToBlobs();
        File f = join(CWD, name);
        writeContents(f, readBlob(commitMap.get(name)));
        Stage stg = getStage();
        stg.unstage(name);
    }

    /** Dealing the case that the input id's length less than 40. */
    private static String resolveCommitId(String prefix) {
        if (prefix.length() == 40) {
            return prefix;
        }
        List<String> all = plainFilenamesIn(COMMIT_DIR);
        if (all == null) {
            exitWithMessage("No commit with that id exists.");
        }

        String match = null;
        for (String id : all) {
            if (id.startsWith(prefix)) {
                if (match != null) {
                    exitWithMessage("No commit with that id exists.");
                }
                match = id;
            }
        }
        if (match == null) {
            exitWithMessage("No commit with that id exists.");
        }
        return match;
    }
    /** merge method */
    public static void merge(String head) {
        mergeCheck(head);
        String newHeadID = readHead(head);
        String curHeadID = getCurCommitID();
        String splitPoint = splitPoint(head);
        if (splitPoint.equals(newHeadID)) {
            System.out.println("Given branch is an ancestor of the current branch.");
            return;
        }
        if (splitPoint.equals(curHeadID)) {
            checkoutBranch(head);
            System.out.println("Current branch fast-forwarded.");
            return;
        }

        Map<String, String> splitCommits = readCommit(splitPoint).getPathToBlobs();
        Map<String, String> curCommits = readCommit(getCurCommitID()).getPathToBlobs();
        Map<String, String> newCommits = readCommit(newHeadID).getPathToBlobs();
        Stage stg = getStage();

        /// dealing untracked files
        List<String> cwdList = plainFilenamesIn(CWD);
        if (cwdList == null) { /// guard against NPE
            cwdList = Collections.emptyList();
        }
        for (String s : cwdList) {
            if (!curCommits.containsKey(s)) {
                if (!splitCommits.containsKey(s) && newCommits.containsKey(s)) {
                    exitWithMessage("There is an untracked file in the way; delete it, or add and commit it first.");
                }
                if (splitCommits.containsKey(s) && newCommits.containsKey(s)) {
                    if (!splitCommits.get(s).equals(newCommits.get(s))) {
                        exitWithMessage("There is an untracked file in the way; delete it, or add and commit it first.");
                    }
                }
            }
        }

        Set<String> allFiles = new HashSet<>();
        allFiles.addAll(splitCommits.keySet());
        allFiles.addAll(curCommits.keySet());
        allFiles.addAll(newCommits.keySet());
        boolean conflictFlag = false;
        for (String f : allFiles) {
            String s = splitCommits.get(f);
            String c = curCommits.get(f);
            String n = newCommits.get(f);
            boolean cEn = Objects.equals(c, n);
            boolean sEc = Objects.equals(s, c);
            boolean sEn = Objects.equals(s, n);
            if (cEn || sEn) { continue; }
            if (sEc) {
                if (n == null) {
                    removeFile(f);
                } else {
                    File file = join(CWD, f);
                    String blob = newCommits.get(f);
                    writeContents(file, readBlob(blob));
                    stg.addBlob(f, blob);
                }
            } else {
                /// conflict
                conflictFlag = true;
                File file = join(CWD, f);
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                out.writeBytes("<<<<<<< HEAD\n".getBytes(StandardCharsets.UTF_8));
                out.writeBytes(readBlob(c));
                out.writeBytes("=======\n".getBytes(StandardCharsets.UTF_8));
                out.writeBytes(readBlob(n));
                out.writeBytes(">>>>>>>\n".getBytes(StandardCharsets.UTF_8));
                byte[] merge = out.toByteArray();
                writeContents(file, merge);
                stg.addFile(f);
            }
        }
        commit("Merged " + head + " into " + getCurHead() + ".", newHeadID);
        if (conflictFlag) {
            System.out.println("Encountered a merge conflict.");
        }
    }

    /** Check the basic failure cases of merge. */
    private static void mergeCheck(String head) {
        if (head.equals(getCurHead())) {
            exitWithMessage("Cannot merge a branch with itself.");
        }
        File b = join(HEAD_DIR, head);
        if (!b.exists()) {
            exitWithMessage("A branch with that name does not exist.");
        }
        Stage stg = getStage();
        if (!stg.isEmpty()) {
            exitWithMessage("You have uncommitted changes.");
        }
    }

    /** return the split point */
    private static String splitPoint(String head) {
        String headA = getCurCommitID();
        String headB = readHead(head);
        while (!headA.equals(headB)) {
            headA = readParentID(headA);
            headB = readParentID(headB);
            if (headA == null) {
                headA = readHead(head);
            }
            if (headB == null) {
                headB = getCurCommitID();
            }
        }
        return headA;
    }

    /** return the parent id with the given id */
    private static String readParentID(String ID) {
        return readCommit(ID).getParentID();
    }

    /** helper function for debug. */
    public static void helper() {
        Stage stg = getStage();
        System.out.println("Stage: " + stg.getAddStage() + " " + stg.getRemoveStage());
        System.out.println("Commit " + getCurCommitID() + ": " + getCurCommit().getPathToBlobs());
    }
}
