package com.virtuslab.gitmachete.frontend.actions.traverse;

import static com.virtuslab.gitmachete.frontend.actions.traverse.TraverseSyncToRemote.syncBranchToRemote;
import static com.virtuslab.gitmachete.frontend.resourcebundles.GitMacheteBundle.getString;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageConstants;
import com.intellij.openapi.ui.MessageDialogBuilder;
import git4idea.repo.GitRepository;
import lombok.experimental.ExtensionMethod;
import lombok.val;
import org.checkerframework.checker.guieffect.qual.UI;
import org.checkerframework.checker.guieffect.qual.UIEffect;

import com.virtuslab.binding.RuntimeBinding;
import com.virtuslab.branchlayout.api.readwrite.IBranchLayoutWriter;
import com.virtuslab.gitmachete.backend.api.INonRootManagedBranchSnapshot;
import com.virtuslab.gitmachete.frontend.actions.backgroundables.OverrideForkPointBackgroundable;
import com.virtuslab.gitmachete.frontend.actions.backgroundables.RebaseOnParentBackgroundable;
import com.virtuslab.gitmachete.frontend.actions.backgroundables.SlideOutBackgroundable;
import com.virtuslab.gitmachete.frontend.actions.dialogs.OverrideForkPointDialog;
import com.virtuslab.gitmachete.frontend.resourcebundles.GitMacheteBundle;
import com.virtuslab.gitmachete.frontend.ui.api.table.BaseEnhancedGraphTable;
import com.virtuslab.gitmachete.frontend.vfsutils.GitVfsUtils;

@ExtensionMethod({GitMacheteBundle.class, GitVfsUtils.class})
public final class TraverseSyncToParent {

  private TraverseSyncToParent() {}

  @UIEffect
  static void syncBranchToParent(Project project, BaseEnhancedGraphTable graphTable,
      INonRootManagedBranchSnapshot gitMacheteBranch,
      GitRepository gitRepository, @UI Runnable traverseNextEntry) {
    val branchName = gitMacheteBranch.getName();
    val remoteTrackingBranch = gitMacheteBranch.getRemoteTrackingBranch();
    val gitMacheteNonRootBranch = gitMacheteBranch.asNonRoot();
    val syncToParentStatus = gitMacheteNonRootBranch.getSyncToParentStatus();
    val repositorySnapshot = graphTable.getGitMacheteRepositorySnapshot();

    switch (syncToParentStatus) {
      case MergedToParent :
        if (repositorySnapshot != null) {
          val branchLayout = repositorySnapshot.getBranchLayout();
          val currentBranchIfManaged = repositorySnapshot.getCurrentBranchIfManaged();
          new SlideOutBackgroundable(getString("action.GitMachete.BaseSlideOutAction.task.title"),
              gitMacheteBranch, gitRepository, currentBranchIfManaged, branchLayout, graphTable).queue();
          return;
        }
        break;

      case InSyncButForkPointOff :
        val selectedCommit = new OverrideForkPointDialog(project, gitMacheteNonRootBranch).showAndGetSelectedCommit();

        new OverrideForkPointBackgroundable(project, getString("action.GitMachete.BaseOverrideForkPointAction.task.title"),
            gitRepository, gitMacheteNonRootBranch, graphTable, selectedCommit).queue();
        return;

      case OutOfSync :
        val nonRootBranch = gitMacheteBranch.asNonRoot();
        val rebaseDialog = MessageDialogBuilder.yesNoCancel(
            getString("action.GitMachete.TraverseAction.dialog.diverged-from-parent.title"),
            getString(
                "action.GitMachete.TraverseAction.dialog.diverged-from-parent.text.HTML").fmt(
                    nonRootBranch.getName(),
                    nonRootBranch.getParent().getName()))
            .cancelText(getString("action.GitMachete.TraverseAction.dialog.cancel-traverse"));

        switch (rebaseDialog.show(project)) {
          case MessageConstants.YES :
            val branchToRebase = gitMacheteBranch.asNonRoot();
            if (repositorySnapshot != null && branchToRebase != null) {
              new RebaseOnParentBackgroundable(project,
                  getString("action.GitMachete.BaseSyncToParentByRebaseAction.task-title"),
                  gitRepository, repositorySnapshot, branchToRebase, /* shouldExplicitlyCheckout */ false).queue();
              return;
            }
            break;

          case MessageConstants.NO :
            break;

          default :
            return;
        }
        break;

      default :
        break;
    }

    graphTable.queueRepositoryUpdateAndModelRefresh(
        () -> syncBranchToRemote(project, graphTable, remoteTrackingBranch, branchName, gitRepository, traverseNextEntry));
  }

}
