package com.ibm.Git.service;

import java.io.IOException;
import java.util.List;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;

public interface GitService {
	
	 public List<String>  validateAndcloneFromGit(String gitUrl,  String user, String token, String localDirPath) throws Exception ;


}
