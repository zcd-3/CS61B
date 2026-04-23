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
     *         ├── heads
     *         │   ├── master
     *         │   ├── dev
     *         │   └── ...
     *         └── remotes
     *             ├── <remote-name>
     *             └── ...
     */

    /** The current working directory. */
    private static File CWD = new File(System.getProperty("user.dir"));
    /** The .gitlet directory. */
    private static File GITLET_DIR = join(CWD, ".gitlet");
    /** The rest directories. */
    private static File BLOB_DIR = join(GITLET_DIR, "blobs");
    private static File COMMIT_DIR = join(GITLET_DIR, "commits");
    private static File REFS_DIR = join(GITLET_DIR, "refs");
    private static File HEAD_DIR = join(REFS_DIR, "heads");
    private static File REMOTE_DIR = join(REFS_DIR, "remotes");

    /** Files */
    private static File HEAD = join(GITLET_DIR, "HEAD");
    private static File STAGE = join(GITLET_DIR, "stage");

    public static File getCWD() {
        return CWD;
    }
    public static File getGitletDir() {
        return GITLET_DIR;
    }
    public static File getBlobDir() {
        return BLOB_DIR;
    }
    public static File getCommitDir() {
        return COMMIT_DIR;
    }
    public static File getRefsDir() {
        return REFS_DIR;
    }
    public static File getHeadDir() {
        return HEAD_DIR;
    }
    public static File getRemoteDir() {
        return REMOTE_DIR;
    }
    public static File getHEAD() {
        return HEAD;
    }
    public static File getSTAGE() {
        return STAGE;
    }

    /** init method */
    public static void init() {
        String msg = "A Gitlet version-control system already exists in the current directory.";
        if (GITLET_DIR.exists()) {
            Utils.exitWithMessage(msg);
        }

        /// initiallize
        GITLET_DIR.mkdir();
        BLOB_DIR.mkdir();
        COMMIT_DIR.mkdir();
        REFS_DIR.mkdir();
        HEAD_DIR.mkdir();
        REMOTE_DIR.mkdir();
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
    private static void updateCurHead(String s) {
        writeContents(join(HEAD_DIR, getCurHead()), s);
    }

    /** Rewrite the current head. */
    private static void rewriteCurHead(String s) {
        writeContents(HEAD, s);
    }

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
        String id = c.getID();
        updateCurHead(id);
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
        System.out.println("=== Branches ===");
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
        for (String name : addNames) {
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
        List<String> cwdList = plainFilenamesIn(CWD); /// already sorted
        if (cwdList == null) { /// guard against NPE
            cwdList = Collections.emptyList();
        }
        Set<String> cwd = new HashSet<>(cwdList);

        System.out.println("=== Modifications Not Staged For Commit ===");
        List<String> modified = new ArrayList<>();
        for (String s : commits.keySet()) {
            if (addStage.containsKey(s)) {
                continue;
            }
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
        String msg = "There is an untracked file in the way;"
                + " delete it, or add and commit it first.";
        for (String s : cwdList) {
            boolean b1 = removeStage.contains(s);
            boolean b2 = addStage.containsKey(s);
            boolean b3 = commits.containsKey(s);
            boolean b4 = newCommits.containsKey(s);
            if ((b1 || (!b2 && !b3)) && b4) {
                exitWithMessage(msg);
            }
        }
        /// overwrite
        for (Map.Entry<String, String> e : newCommits.entrySet()) {
            String fileName = e.getKey();
            String blobID = e.getValue();
            byte[] content = readBlob(blobID);
            File f = join(CWD, fileName);
            writeContents(f, content);
        }
        /// delete
        for (String s : commits.keySet()) {
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
        List<String> cwdList = plainFilenamesIn(CWD); /// dealing untracked files
        if (cwdList == null) { /// guard against NPE
            cwdList = Collections.emptyList();
        }
        String m = "There is an untracked file in the way; delete it, or add and commit it first.";
        for (String s : cwdList) {
            if (!curCommits.containsKey(s)) {
                boolean b1 = splitCommits.containsKey(s);
                boolean b2 = newCommits.containsKey(s);
                if (!b1 && b2) {
                    exitWithMessage(m);
                }
                if (b1 && b2) {
                    if (!splitCommits.get(s).equals(newCommits.get(s))) {
                        exitWithMessage(m);
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
            if (cEn || sEn) {
                continue;
            }
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
                conflictFlag = true;
                File file = join(CWD, f);
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                out.writeBytes("<<<<<<< HEAD\n".getBytes(StandardCharsets.UTF_8));
                out.writeBytes(readBlob(c));
                out.writeBytes("=======\n".getBytes(StandardCharsets.UTF_8));
                out.writeBytes(readBlob(n));
                out.writeBytes(">>>>>>>\n".getBytes(StandardCharsets.UTF_8));
                writeContents(file, out.toByteArray());
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

        Map<String, Integer> disToA = disFrom(headA);
        Map<String, Integer> disToB = disFrom(headB);

        String best = null;
        int minSum = Integer.MAX_VALUE;
        for (String id : disToA.keySet()) {
            Integer db = disToB.get(id);
            if (db != null && db < minSum) {
                int sum = db + disToA.get(id);
                if (sum < minSum) {
                    minSum = sum;
                    best = id;
                }
            }
        }
        return best;
    }

    /** Returns shortest distance from head to each ancestor. */
    private static Map<String, Integer> disFrom(String head) {
        Map<String, Integer> dist = new HashMap<>();
        ArrayDeque<String> que = new ArrayDeque<>();
        dist.put(head, 0);
        que.addLast(head);
        while (!que.isEmpty()) {
            String id = que.removeFirst();
            int d = dist.get(id);
            Commit c = readCommit(id);
            String parent = c.getParentID();
            String secondParent = c.getSecondParentID();
            if (parent != null && !dist.containsKey(parent)) {
                dist.put(parent, d + 1);
                que.addLast(parent);
            }
            if (secondParent != null && !dist.containsKey(secondParent)) {
                dist.put(secondParent, d + 1);
                que.addLast(secondParent);
            }
        }
        return dist;
    }

    private static void changeCurGitletDir(String path) {
        GITLET_DIR = new File(path);
        CWD = GITLET_DIR.getParentFile();
        COMMIT_DIR = join(GITLET_DIR, "commits");
        BLOB_DIR = join(GITLET_DIR, "blobs");
        STAGE = join(GITLET_DIR, "stage");
        HEAD = join(GITLET_DIR, "HEAD");
        REFS_DIR = join(GITLET_DIR, "refs");
        REMOTE_DIR = join(GITLET_DIR, "remotes");
        HEAD_DIR = join(GITLET_DIR, "heads");
    }

    private static void changeBackGitletDir() {
        changeCurGitletDir(System.getProperty("user.dir") + File.separator + ".gitlet");
    }

    /** add-remote method */
    public static void addRemote(String name, String path) {
        File f = join(REMOTE_DIR, name);
        if (f.exists()) {
            exitWithMessage("A remote with that name already exists.");
        }
        String remotePath = path.replace("/", File.separator);
        writeContents(f, remotePath);
        File dir = join(HEAD_DIR, name);
        dir.mkdir();
    }

    /** rm-remote method */
    public static void removeRemote(String name) {
        File f = join(REMOTE_DIR, name);
        if (!f.exists()) {
            exitWithMessage("A remote with that name does not exist.");
        }
        f.delete();
        File dir = join(HEAD_DIR, name);
        if (dir.exists() && dir.isDirectory()) {
            for (File file : dir.listFiles()) {
                file.delete();
            }
            dir.delete();
        }
    }


    private static String checkRemote(String name) {
        File f = join(REMOTE_DIR, name);
        if (!f.exists()) {
            exitWithMessage("Remote directory not found.");
        }
        String remotePath = readContentsAsString(f);
        File g = new File(remotePath);
        if (!g.exists()) {
            exitWithMessage("Remote directory not found.");
        }
        return remotePath;
    }

    /** push method */
    public static void push(String name, String branch) {
        String remotePath = checkRemote(name);

        File remoteHeadFile = join(remotePath, "refs", "heads", branch);
        /// if branch not exist, create it
        if (!remoteHeadFile.exists()) {
            changeCurGitletDir(remotePath);
            createBranch(branch);
            changeBackGitletDir();
        }
        String remoteHead = readContentsAsString(remoteHeadFile);
        String curHeadId = getCurCommitID();
        Set<String> curReachable = allReachableCommits(curHeadId);
        if (!curReachable.contains(remoteHead)) {
            exitWithMessage("Please pull down remote changes before pushing.");
        }

        changeCurGitletDir(remotePath);
        Set<String> remoteReachable = allReachableCommits(remoteHead);
        changeBackGitletDir();
        curReachable.removeAll(remoteReachable); ///commits to copy

        copyCommitsTo(curReachable, remotePath);

        /// Update remote head
        writeContents(remoteHeadFile, curHeadId);
    }

    /** Return all commits reachable from the given commit. */
    private static Set<String> allReachableCommits(String start) {
        ArrayDeque<String> que = new ArrayDeque<>();
        Set<String> result = new HashSet<>();

        que.addLast(start);
        result.add(start);
        while (!que.isEmpty()) {
            String cur = que.removeFirst();
            Commit c = readCommit(cur);
            String p1 = c.getParentID();
            String p2 = c.getSecondParentID();
            if (p1 != null && result.add(p1)) {
                que.addLast(p1);
            }
            if (p2 != null && result.add(p2)) {
                que.addLast(p2);
            }
        }
        return result;
    }

    /** Copy commits to given remote path. */
    private static void copyCommitsTo(Set<String> commits, String remotePath) {
        File remoteCommitDir = join(remotePath, "commits");
        for (String c : commits) {
            File newCommit = join(remoteCommitDir, c);
            writeObject(newCommit, readCommit(c));
            copyBlobsTo(c, remotePath);
        }

    }

    /** Copy blobs of a commit to given remote path. */
    private static void copyBlobsTo(String commit, String remotePath) {
        File remoteBlobDir = join(remotePath, "blobs");
        Set<String> blobs = new HashSet<>(readCommit(commit).getPathToBlobs().values());
        for (String b : blobs) {
            File newBlob = join(remoteBlobDir, b);
            if (!newBlob.exists()) {
                writeContents(newBlob, readBlob(b));
            }
        }
    }

    /** fetch method */
    public static void fetch(String name, String branch) {
        String remotePath = checkRemote(name);
        File remoteHeadFile = join(remotePath, "refs", "heads", branch);
        if (!remoteHeadFile.exists()) {
            exitWithMessage("That remote does not have that branch.");
        }
        String remoteHead = readContentsAsString(remoteHeadFile);
        File localHead = join(HEAD_DIR, name + "/" + branch);
        writeContents(localHead, remoteHead);

        changeCurGitletDir(remotePath);
        Set<String> remoteReachable = allReachableCommits(remoteHead);
        changeBackGitletDir();
        copyCommitsFrom(remoteReachable, remotePath);
    }

    /** Copy commits from the given remote path. */
    private static void copyCommitsFrom(Set<String> commits, String remotePath) {
        File remoteCommitDir = join(remotePath, "commits");
        for (String c : commits) {
            File localCommit = join(COMMIT_DIR, c);
            if (localCommit.exists()) {
                continue;
            }
            File remoteCommit = join(remoteCommitDir, c);
            Commit r = readObject(remoteCommit, Commit.class);
            writeObject(localCommit, r);
            copyBlobsFrom(r, remotePath);
        }

    }

    /** Copy blobs of a commit from the given remote path. */
    private static void copyBlobsFrom(Commit c, String remotePath) {
        File remoteBlobDir = join(remotePath, "blobs");
        Set<String> blobs = new HashSet<>(c.getPathToBlobs().values());
        for (String b : blobs) {
            File localBlob = join(BLOB_DIR, b);
            if (!localBlob.exists()) {
                File remoteBlob = join(remoteBlobDir, b);
                writeContents(localBlob, readContents(remoteBlob));
            }
        }
    }

    /** pull method */
    public static void pull(String name, String branch) {
        fetch(name, branch);
        merge(name + "/" + branch);
    }
}
