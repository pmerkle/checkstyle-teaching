package edu.kit.checkstyle.listeners;

import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import com.puppycrawl.tools.checkstyle.api.AuditListener;
import com.puppycrawl.tools.checkstyle.api.AutomaticBean;

import java.io.OutputStream;
import java.io.PrintWriter;

public class QualifiedListener extends AutomaticBean implements AuditListener
{
  public QualifiedListener()
  {}

  public void auditStarted(AuditEvent event)
  {
  }

  public void fileStarted(AuditEvent event)
  {
  }

  public void auditFinished(AuditEvent event)
  {
  }

  public void fileFinished(AuditEvent event)
  {
  }

  public void addError(AuditEvent event)
  {
      System.out.println(event.getSourceName() + ":" + event.getFileName() + ":" + event.getLine() + ":" + event.getColumn() + ":" + event.getMessage());
  }

  public void addException(AuditEvent event, Throwable e)
  {
  }
}
