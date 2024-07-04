package it.unipi.dii.aide.mircv.beans;

import java.io.IOException;
import java.io.Serial;
import java.io.Serializable;

public class DocumentIndexEntry implements Serializable{
    // PID of the document
    String pid;

    // DocID of the document
    int docid;

    // Length of the document
    int doclen;

    /**
     * Constructor of the DocumentIndexEntry
     * @param docid the document id
     * @param pid the document pid
     * @param doclen the document length
     */
    public DocumentIndexEntry(String pid, int docid, int doclen) {
        this.pid = pid;
        this.docid = docid;
        this.doclen = doclen;
    }

    public int getDocid() {
        return docid;
    }

    public String getPid() {
        return pid;
    }

    public int getDoclen() {
        return doclen;
    }

    public void setDocid(int docid) {
        this.docid = docid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public void setDoclen(int doclen) {
        this.doclen = doclen;
    }

    @Serial
    private void writeObject(java.io.ObjectOutputStream out) throws IOException {
        out.writeObject(pid);
        out.writeInt(docid);
        out.writeInt(doclen);
    }

    @Serial
    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        pid = in.readUTF();
        docid = in.readInt();
        doclen = in.readInt();
    }

    @Override
    public String toString() {
        return "DocumentIndexEntry{" +
                "pid='" + pid + '\'' +
                ", docid=" + docid +
                ", doclen=" + doclen +
                '}';
    }
}
