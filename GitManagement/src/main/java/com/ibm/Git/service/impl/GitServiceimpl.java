package com.ibm.Git.service.impl;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.StoredConfig;
import org.eclipse.jgit.transport.PushResult;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.RemoteRefUpdate;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.apache.commons.io.FileUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ibm.Git.service.GitService;

public class GitServiceimpl implements GitService {

    private static final Logger logger = LoggerFactory.getLogger(GitServiceimpl.class);
    private static final String HTTPS = "https";
    private static final String HTTP = "http";

    public List<String> validateAndcloneFromGit(String gitUrl, String user, String token, String localDirPath) throws Exception {
        UsernamePasswordCredentialsProvider upc = new UsernamePasswordCredentialsProvider(user, token);
        File localDir = new File(localDirPath);

        if (!localDir.exists()) {
            localDir.mkdirs();
        }

        // Check if user has access to the repository
        try {
            Git.lsRemoteRepository()
                    .setRemote(gitUrl)
                    .setCredentialsProvider(upc)
                    .call();
        } catch (GitAPIException e) {
            logger.error("User does not have read access to the repository: {}", e.getMessage());
            throw new Exception("User does not have read access to the repository.", e);
        }

        String branch = getDefaultBranch(gitUrl, upc);

        try (Git git = Git.cloneRepository()
                .setURI(gitUrl)
                .setDirectory(localDir)
                .setBranch(branch)
                .setCredentialsProvider(upc)
                .call()) {
            
            StoredConfig config = git.getRepository().getConfig();
            config.setBoolean(HTTP, null, "sslVerify", false);
            config.setBoolean(HTTPS, null, "sslVerify", false);
            config.save();

            Collection<Ref> refs = git.lsRemoteRepository()
                    .setHeads(true)
                    .setRemote(gitUrl)
                    .setCredentialsProvider(upc)
                    .call();

            Map<String, Ref> gitBranchMap = new HashMap<>();
            refs.forEach(ref -> {
                String branchName = ref.getName().substring(ref.getName().lastIndexOf("/") + 1);
                gitBranchMap.put(branchName, ref);
            });

            if (gitBranchMap.containsKey(branch)) {
                // Branch exists. Cloned to local repository successfully.
            } else {
                logger.error("Branch does not exist.");
                throw new Exception("Branch does not exist.");
            }
        } catch (GitAPIException | IOException e) {
            logger.error("Error during Git operations: {}", e.getMessage());
            throw new Exception("Git operation failed.", e);
        }

        // Get the project names from the subdirectories created after cloning
        List<String> projectNames = findProjectFolders(localDir.toPath());
        if (projectNames.isEmpty()) {
            throw new Exception("No valid Maven projects found in the repository.");
        }

        return projectNames;
    }
    
  
    public List<String> findProjectFolders(Path directoryPath) {
        try {
            if (!Files.isDirectory(directoryPath)) {
                logger.warn("Provided path is not a directory: {}", directoryPath);
                return Collections.emptyList();
            }

            try (Stream<Path> paths = Files.walk(directoryPath, 2)) {
                List<String> projectFolders = paths.filter(Files::isDirectory)
                        .filter(path -> !path.equals(directoryPath)) // Exclude the root directory itself
                        .filter(this::isMavenProject)
                        .map(path -> path.getFileName().toString())
                        .collect(Collectors.toList());

                if (!projectFolders.isEmpty()) {
                    logger.info("Maven project folders found: {}", projectFolders);
                    return projectFolders;
                } else {
                    logger.warn("No Maven project folders found in directory: {}", directoryPath);
                    return Collections.emptyList();
                }
            } catch (IOException e) {
                logger.error("Error while searching for project folders.", e);
                return Collections.emptyList();
            }
        } catch (InvalidPathException e) {
            logger.error("Invalid path provided: {}", e.getMessage());
            return Collections.emptyList();
        }
    }
    
    public boolean isMavenProject(Path directoryPath) {
        if (!Files.isDirectory(directoryPath)) {
            logger.warn("Provided path is not a directory: {}", directoryPath);
            return false;
        }

        Path pomFilePath = directoryPath.resolve("pom.xml");
        if (!Files.exists(pomFilePath)) {
           // logger.warn("pom.xml not found in directory: {}", directoryPath);
            return false;
        }

        Path srcMainJava = directoryPath.resolve("src").resolve("main").resolve("java");
        Path srcMainResources = directoryPath.resolve("src").resolve("main").resolve("resources");
        Path srcTestJava = directoryPath.resolve("src").resolve("test").resolve("java");

        boolean hasMavenStructure = Files.isDirectory(srcMainJava) && Files.isDirectory(srcMainResources)
                && Files.isDirectory(srcTestJava);

        if (hasMavenStructure) {
            logger.info("Valid Maven project found at: {}", directoryPath);
        } else {
            logger.warn("Directory does not follow Maven structure: {}", directoryPath);
        }

        return hasMavenStructure;
    }
    



    private String getDefaultBranch(String gitUrl, UsernamePasswordCredentialsProvider upc) throws GitAPIException, IOException {
        Collection<Ref> refs = Git.lsRemoteRepository()
                .setHeads(true)
                .setRemote(gitUrl)
                .setCredentialsProvider(upc)
                .call();

        for (Ref ref : refs) {
            String branchName = ref.getName().substring(ref.getName().lastIndexOf("/") + 1);
            if (branchName.equals("main") || branchName.equals("master")) {
                return branchName;
            }
        }

        // If no main or master branch is found, just return the first branch found
        if (!refs.isEmpty()) {
            Ref firstRef = refs.iterator().next();
            return firstRef.getName().substring(firstRef.getName().lastIndexOf("/") + 1);
        }

        throw new IOException("No branches found in the repository.");
    }

    private String findSubdirectoryName(File parentDir) {
        File[] files = parentDir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory() && !file.getName().equals(".git")) {
                    
                    return file.getName();
                }
            }
        }
        // Handle the case where no suitable subdirectory is found
        return parentDir.getName(); // Default to parent directory name
    }
    
    public void commitToBranch(String gitUrl, Git git, String user, String token) throws Exception {
    	
    	 UsernamePasswordCredentialsProvider upc = new UsernamePasswordCredentialsProvider(user, token);
    	 try {
             Git.lsRemoteRepository()
                     .setRemote(gitUrl)
                     .setCredentialsProvider(upc)
                     .call();
         } catch (GitAPIException e) {
             logger.error("User does not have write access to the repository: {}", e.getMessage());
             throw new Exception("User does not have write access to the repository.", e);
         }
        try {
        	
        	

            // Create a temporary branch
            String tempBranch = "temp_branch"; // Generate a unique branch name
            git.checkout()
               .setCreateBranch(true)
               .setName(tempBranch)
               .call();
            logger.info("Created temporary branch {}", tempBranch);

            // Stage all files
            git.add().addFilepattern(".").call();

            // Commit changes to the temporary branch
            git.commit().setMessage("test commit").call();
            logger.info("Committed changes to temporary branch {}", tempBranch);

            Iterable<PushResult> pushResults = git.push()
                    .setCredentialsProvider(upc)
                    .setRemote("origin")
                    .setRefSpecs(new RefSpec(tempBranch))
                    .call();

            for (PushResult pushResult : pushResults) {
                for (RemoteRefUpdate update : pushResult.getRemoteUpdates()) {
                    if (update.getStatus() == RemoteRefUpdate.Status.OK) {
                        logger.info("Pushed changes to temporary branch {}", tempBranch);
                    } else {
                        logger.error("Failed to push changes to temporary branch {}: {}", tempBranch, update.getStatus());
                        throw new Exception("Failed to push changes to temporary branch " + tempBranch + ": " + update.getStatus());
                    }
                }
            }

        } catch (GitAPIException | IOException e) {
            logger.error("Error committing changes: {}", e.getMessage());
            throw new Exception("Commit operation failed.", e);
        }
    }

    
    
   


}
