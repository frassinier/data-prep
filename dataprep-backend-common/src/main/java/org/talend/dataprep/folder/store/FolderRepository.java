//  ============================================================================
//
//  Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
//  This source code is available under agreement available at
//  https://github.com/Talend/data-prep/blob/master/LICENSE
//
//  You should have received a copy of the agreement
//  along with this program; if not, write to Talend SA
//  9 rue Pages 92150 Suresnes, France
//
//  ============================================================================

package org.talend.dataprep.folder.store;

import java.util.ArrayList;
import java.util.List;

import org.talend.dataprep.api.folder.Folder;
import org.talend.dataprep.api.folder.FolderContentType;
import org.talend.dataprep.api.folder.FolderEntry;
import org.talend.dataprep.exception.TDPException;


/**
 * Folder repository that manage folders and folder entries.
 */
public interface FolderRepository {

    /**root
     * Return true if the given folder exists.
     * @param folderId the wanted folder folderId.
     * @return true if the given folder exists.
     */
    boolean exists(String folderId);

    /**
     * Get the current user home folder. If the folder does not yet exists, it's created.
     *
     * @return the Home {@link Folder}.
     */
    Folder getHome();


    /**
     * Add a folder.
     *
     * @param parentFolderId parent folder id.
     * @param path the path to create
     */
    Folder addFolder(String parentFolderId, String path);

    /**
     * @return A {@link java.lang.Iterable iterable} of children {@link Folder folder}. Returned folders are expected to be
     * visible by current user.
     * @param folderId the parent folder in the format /ffo/blab/mm or <code>null</code> for root folder
     */
    Iterable<Folder> children(String folderId);


    /**
     * Remove folder and content recursively only if no entry is found.
     * 
     * @param folderId the folderId to remove only the last part is removed.
     * @throws NotEmptyFolderException if folder recursively contains any entries (dataset etc...)
     */
    void removeFolder(String folderId) throws NotEmptyFolderException;

    /**
     * Rename a folder and its content recursively.
     *
     * @param folderId the folder id to rename
     * @param newName the new folder name.
     * @return the renamed folder.
     */
    Folder renameFolder(String folderId, String newName);

    /**
     * Add or replace (if already exists) the entry.
     *
     * @param folderEntry the {@link FolderEntry} to add.
     * @param folderId where to add this folder entry.
     */
    FolderEntry addFolderEntry(FolderEntry folderEntry, String folderId);

    /**
     * Remove a {@link FolderEntry}.
     *
     * @param folderId the folder path containing the entry
     * @param contentId the id
     * @param contentType  the type dataset, preparation
     */
    void removeFolderEntry(String folderId, String contentId, FolderContentType contentType);

    /**
     * List the {@link FolderEntry} of the wanted type within the given folderId.
     * @param folderId the parent folderId
     * @param contentType the contentClass to filter folder entries
     * @return A {@link java.lang.Iterable iterable} of {@link FolderEntry} content filtered for the given type
     */
    Iterable<FolderEntry> entries(String folderId, FolderContentType contentType);

    /**
     * Look for all the {@link FolderEntry} that points to an existing content.
     *
     * It's useful when you want to delete a dataset hence all its references.
     *
     * @param contentId the id
     * @param contentType  the type dataset, preparation
     * @return A {@link Iterable} of the {@link FolderEntry} containing the folderEntry as described by contentType and contentId.
     */
    Iterable<FolderEntry> findFolderEntries(String contentId, FolderContentType contentType);

    /**
     * <b>if the destination or entry doesn't exist a {@link IllegalArgumentException} will be thrown</b>
     *  @param folderEntry the {@link FolderEntry} to move.
     * @param fromId where to look for the folder entry.
     * @param toId the destination where to move the entry.
     */
    void moveFolderEntry(FolderEntry folderEntry, String fromId, String toId);

    /**
     * Copy the given folder entry to the target destination.
     * @param folderEntry the {@link FolderEntry} to move
     * @param toId the destination where to copy the entry
     */
    void copyFolderEntry(FolderEntry folderEntry, String toId);

    /**
     * Clear the whole content.
     */
    void clear();

    /**
     * used mainly for testing purpose
     * 
     * @return the number of created {@link Folder}
     */
    long size();

    /**
     *
     * @param queryString part of the name to search in folder (not case sensitive)
     * @param strict strict mode means the name is the full name
     * @return A {@link Iterable} of {@link Folder} with the query string in the name
     */
    Iterable<Folder> searchFolders(String queryString, boolean strict);

    /**
     * Return the folder that holds the given content id and content type.
     *
     * @param contentId the wanted content id.
     * @param type the content type.
     * @return the folder that holds the wanted entry or null if not found.
     */
    Folder locateEntry(String contentId, FolderContentType type);

    /**
     * Get the folder metadata
     *
     * @param folderId the folder id.
     * @return {@link Folder} the searched folder.
     */
    Folder getFolderById(String folderId);

    /**
     * Get the folder hierarchy (list of its parents)
     *
     * @param folder the folder.
     * @return A {@link Iterable} of {@link Folder} containing all its parents starting from home.
     */
    default List<Folder> getHierarchy(final Folder folder) {
        final List<Folder> hierarchy = new ArrayList<>();

        String nextParentId = folder.getParentId();
        while(nextParentId != null) {
            try {
                final Folder parent = this.getFolderById(nextParentId);
                hierarchy.add(0, parent);
                nextParentId = parent.getParentId();
            }
            catch(final TDPException e) {
                // in case of shared folder, we can have a 403 during hierarchy construction
                // ex: /folder/folderChild/folderGrandChild, with /folderChild shared but not /folder
                // we just add the current user home because every top level shared folder is considered
                // as the user's home child
                if(e.getCode().getHttpStatus() == 403) {
                    hierarchy.add(0, this.getHome());
                    break;
                }
                throw e;
            }
        }

        return hierarchy;
    }
}
