package com.ibm.Git;

import java.io.File;
import java.util.List;

import com.ibm.Git.service.impl.GitServiceimpl;
import org.eclipse.jgit.api.Git;

public class App {
    public static void main(String[] args) {
        GitServiceimpl gitService = new GitServiceimpl();
        String gitUrl = "https://github.com/Sunitha-89/Test";
        String user = "Sunitha-89"; // Replace with your Git username
        String token = "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"; // Replace with your Git token
        String localDirPath = "C:\\Users\\SunithaGM\\Desktop\\New folder"; // Replace with the path to your local directory

        
        try {
            // Clone the repository
            List<String> projectName = gitService.validateAndcloneFromGit(gitUrl, user, token, localDirPath);
            System.out.println("Repository cloned successfully. Project Name: " + projectName);

            // Reopen the cloned repository
            File localDir = new File(localDirPath);
            try (Git git = Git.open(localDir)) {
                // Commit changes to the branch
                gitService.commitToBranch(gitUrl,
                		git, user, token);
                System.out.println("Changes committed successfully.");
            } catch (Exception e) {
                System.err.println("Error during commit operation: " + e.getMessage());
            }
        } catch (Exception e) {
            System.err.println("Error during Git operation: " + e.getMessage());
        }
        
    }
}
