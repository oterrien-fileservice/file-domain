package com.ote.file.api;

import com.ote.file.api.exception.*;
import com.ote.file.api.model.File;
import com.ote.file.api.model.Folder;

import java.util.Set;

public interface IFileService {

    Set<Folder> getFolders(String user, String application, String perimeter)
            throws UserNotFoundException, ApplicationNotFoundException, PerimeterNotFoundException, UnauthorizedException;

    Set<File> getFiles(String user, String application, String perimeter, Folder folder)
            throws UserNotFoundException, ApplicationNotFoundException, PerimeterNotFoundException, FolderNotFoundException, UnauthorizedException;

    Set<File> getFiles(String user, String application, String perimeter)
            throws UserNotFoundException, ApplicationNotFoundException, PerimeterNotFoundException, UnauthorizedException;

    byte[] read(String user, String application, String perimeter, File file)
            throws UserNotFoundException, ApplicationNotFoundException, PerimeterNotFoundException, FolderNotFoundException, FileNotFoundException, UnauthorizedException, LockException;

    void save(String user, String application, String perimeter, File file, byte[] content, boolean replaceIfFound)
            throws UserNotFoundException, ApplicationNotFoundException, PerimeterNotFoundException, FolderNotFoundException, FileFoundException, UnauthorizedException, LockException;

    void append(String user, String application, String perimeter, File file, byte[] content, boolean createIfNotFound)
            throws UserNotFoundException, ApplicationNotFoundException, PerimeterNotFoundException, FolderNotFoundException, FileNotFoundException, UnauthorizedException, LockException;
}
