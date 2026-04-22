package gitlet;

import java.io.File;
import java.io.Serializable;

/**
 * Represents a blob tracking each file in the repository.
 * @author zcd3
 */
public class Blob implements Serializable {
    /** The contents of the Blob. */
    private final byte[] contents;

    /** Constuctor */
    public Blob(File path) {
        contents = Utils.readContents(path);
    }

    /** Get the ID of the Blob. */
    public String getID() {
        return Utils.sha1(contents);
    }

    /** Persist this blob object under its ID. */
    public void save() {
        String Id = getID();
        File b = Utils.join(Repository.BLOB_DIR, Id);
        if (!b.exists()) {
            Utils.writeContents(b, contents);
        }
    }
}
