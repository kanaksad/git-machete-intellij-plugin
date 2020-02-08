package com.virtuslab.branchrelationfile.api;

import java.util.List;
import java.util.Optional;

public interface IBranchRelationFileEntry extends Cloneable {
  String getName();

  Optional<IBranchRelationFileEntry> getUpstream();

  void setUpstream(Optional<IBranchRelationFileEntry> upstream);

  List<IBranchRelationFileEntry> getSubbranches();

  Optional<String> getCustomAnnotation();

  void addSubbranch(IBranchRelationFileEntry subbranch);

  Object clone() throws CloneNotSupportedException;
}
