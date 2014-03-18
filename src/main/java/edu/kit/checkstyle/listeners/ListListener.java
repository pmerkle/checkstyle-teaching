package edu.kit.checkstyle.listeners;

import java.io.PrintWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import com.puppycrawl.tools.checkstyle.api.AuditListener;
import com.puppycrawl.tools.checkstyle.api.AutomaticBean;


public class ListListener extends AutomaticBean implements AuditListener {

  private String packageName;
  private boolean closeOut = false;
  private PrintWriter writer = new PrintWriter(System.out);

  public void auditStarted(final AuditEvent e) {
  }

  public void fileStarted(final AuditEvent e) {
      packageName = new String();
  }

  public void fileFinished(final AuditEvent e) {
      packageName = null;
  }

  public void auditFinished(final AuditEvent e) {
    writer.flush();
    if (closeOut) {
      writer.close();
    }
  }

  public void addError(final AuditEvent e) {
    // this one is only for the *ListCheck classes
    String sourceName = e.getSourceName();

    if (sourceName == "edu.kit.checkstyle.checks.lists.PackageListCheck") {
        packageName = e.getMessage() + ".";
        writer.println("package: " + e.getMessage());
    } else if (sourceName =="edu.kit.checkstyle.checks.lists.ImportListCheck") {
        writer.println("import: " + e.getMessage());
    } else if (sourceName =="edu.kit.checkstyle.checks.lists.ClassListCheck") {
        writer.println("class: " + packageName + e.getMessage());
    } else if (sourceName == "edu.kit.checkstyle.checks.lists.EnumListCheck") {
        writer.println("enum: " + packageName + e.getMessage());
    } else if (sourceName == "edu.kit.checkstyle.checks.lists.MethodListCheck") {
        writer.println("method: " + packageName + e.getMessage());
    } else if (sourceName == "edu.kit.checkstyle.checks.lists.IdentifierListCheck") {
        writer.println("identifier: " + e.getMessage());
    } else {
        writer.println("unsupported: " + e.getMessage());
    }

  }

  public void addException(final AuditEvent e, final Throwable aThrowable) {
    aThrowable.printStackTrace(System.out);
  }

  public void setFile(final String fileName) throws FileNotFoundException {
    writer = new PrintWriter(new FileOutputStream(fileName));
    closeOut = true;
  }
}
