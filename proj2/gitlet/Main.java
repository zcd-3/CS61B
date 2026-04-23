package gitlet;

import static gitlet.Utils.exitWithMessage;

/** Driver class for Gitlet, a subset of the Git version-control system.
 *  @author TODO
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND1> <OPERAND2> ... 
     */
    public static void main(String[] args) {
        if (args.length == 0) {
            Utils.exitWithMessage("Please enter a command.");
        }
        String firstArg = args[0];
        switch(firstArg) {
            case "init":
                validateNumArgs(args, 1);
                Repository.init();
                break;
            case "add":
                validateNumArgs(args, 2);
                checkInitialized();
                Repository.addFile(args[1]);
                break;
            case "commit":
                validateNumArgs(args, 2);
                checkInitialized();
                Repository.commit(args[1]);
                break;
            case "rm":
                validateNumArgs(args, 2);
                checkInitialized();
                Repository.removeFile(args[1]);
                break;
            case "log":
                validateNumArgs(args, 1);
                checkInitialized();
                Repository.log();
                break;
            case "global-log":
                validateNumArgs(args, 1);
                checkInitialized();
                Repository.globalLog();
                break;
            case "find":
                validateNumArgs(args, 2);
                checkInitialized();
                Repository.find(args[1]);
                break;
            case "status":
                validateNumArgs(args, 1);
                checkInitialized();
                Repository.status();
                break;
            case "checkout":
                checkInitialized();
                handleCheckout(args);
                break;
            case "branch":
                validateNumArgs(args, 2);
                checkInitialized();
                Repository.createBranch(args[1]);
                break;
            case "rm-branch":
                validateNumArgs(args, 2);
                checkInitialized();
                Repository.removeBranch(args[1]);
                break;
            case "reset":
                validateNumArgs(args, 2);
                checkInitialized();
                Repository.reset(args[1]);
                break;
            case "merge":
                validateNumArgs(args, 2);
                checkInitialized();
                Repository.merge(args[1]);
                break;
            case "add-remote":
                validateNumArgs(args, 3);
                checkInitialized();
                Repository.addRemote(args[1], args[2]);
                break;
            case "rm-remote":
                validateNumArgs(args, 2);
                checkInitialized();
                Repository.removeRemote(args[1]);
                break;
            case "push":
                validateNumArgs(args, 3);
                checkInitialized();
                Repository.push(args[1], args[2]);
                break;
            case "fetch":
                validateNumArgs(args, 3);
                checkInitialized();
                Repository.fetch(args[1], args[2]);
                break;
            case "pull":
                validateNumArgs(args, 3);
                checkInitialized();
                Repository.pull(args[1], args[2]);
                break;
            default:
                Utils.exitWithMessage("No command with that name exists.");
        }
    }

    private static void validateNumArgs(String[] args, int number) {
        if (args.length != number) {
            Utils.exitWithMessage("Incorrect operands.");
        }
    }

    /** helper method, ensures that the current working directory contains an initialized Gitlet repository. */
    private static void checkInitialized() {
        if (!Repository.getGitletDir().exists()) {
            exitWithMessage("Not in an initialized Gitlet directory.");
        }
    }

    private static void handleCheckout(String[] args) {
        if (args.length == 2) {
            // checkout [branch name]
            Repository.checkoutBranch(args[1]);
            return;
        }
        if (args.length == 3 && args[1].equals("--")) {
            // checkout -- [file name]
            Repository.checkoutFile(args[2]);
            return;
        }
        if (args.length == 4 && args[2].equals("--")) {
            // checkout [commit id] -- [file name]
            Repository.checkoutCommitFile(args[1], args[3]);
            return;
        }
        Utils.exitWithMessage("Incorrect operands.");
    }
}
