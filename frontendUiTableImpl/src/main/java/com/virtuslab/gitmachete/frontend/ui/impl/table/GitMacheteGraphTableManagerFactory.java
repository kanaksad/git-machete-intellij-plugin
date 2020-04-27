package com.virtuslab.gitmachete.frontend.ui.impl.table;

import com.intellij.openapi.project.Project;

import com.virtuslab.gitmachete.frontend.ui.api.selection.IGitRepositorySelectionProvider;
import com.virtuslab.gitmachete.frontend.ui.api.table.IGraphTableManager;
import com.virtuslab.gitmachete.frontend.ui.api.table.IGraphTableManagerFactory;

public class GitMacheteGraphTableManagerFactory implements IGraphTableManagerFactory {
  @Override
  public IGraphTableManager create(Project project, IGitRepositorySelectionProvider gitRepositorySelectionProvider) {
    return new GitMacheteGraphTableManager(project, gitRepositorySelectionProvider);
  }
}
