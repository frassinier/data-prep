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

package org.talend.dataprep.api.service;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.TEXT_PLAIN_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.*;
import static org.talend.daikon.exception.ExceptionContext.withBuilder;
import static org.talend.dataprep.exception.error.PreparationErrorCodes.UNABLE_TO_READ_PREPARATION;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;

import org.apache.commons.lang.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import org.talend.dataprep.api.preparation.Action;
import org.talend.dataprep.api.preparation.AppendStep;
import org.talend.dataprep.api.preparation.Preparation;
import org.talend.dataprep.api.service.api.PreviewAddParameters;
import org.talend.dataprep.api.service.api.PreviewDiffParameters;
import org.talend.dataprep.api.service.api.PreviewUpdateParameters;
import org.talend.dataprep.api.service.command.dataset.CompatibleDataSetList;
import org.talend.dataprep.api.service.command.preparation.*;
import org.talend.dataprep.command.CommandHelper;
import org.talend.dataprep.command.GenericCommand;
import org.talend.dataprep.command.preparation.PreparationDetailsGet;
import org.talend.dataprep.command.preparation.PreparationGetActions;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.error.APIErrorCodes;
import org.talend.dataprep.metrics.Timed;

import com.fasterxml.jackson.core.type.TypeReference;
import com.netflix.hystrix.HystrixCommand;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@RestController
public class PreparationAPI extends APIService {

    @RequestMapping(value = "/api/preparations", method = RequestMethod.GET, produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get all preparations.", notes = "Returns the list of preparations the current user is allowed to see.")
    @Timed
    public StreamingResponseBody listPreparations(
            @RequestParam(value = "format", defaultValue = "long") @ApiParam(name = "format", value = "Format of the returned document (can be 'long' or 'short'). Defaults to 'long'.") String format) {

        if (LOG.isDebugEnabled()) {
            LOG.debug("Listing preparations (pool: {} )...", getConnectionStats());
        }

        PreparationList.Format listFormat = PreparationList.Format.valueOf(format.toUpperCase());
        HystrixCommand<InputStream> command;
        try {
            command = getCommand(PreparationList.class, listFormat);
            return CommandHelper.toStreaming(command);
        } finally {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Listed preparations (pool: {} )...", getConnectionStats());
            }
        }
    }


    /**
     * Returns a list containing all data sets metadata that are compatible with a preparation identified by
     * <tt>preparationId</tt>: its id. If no compatible data set is found an empty list is returned. The base data set
     * of the preparation with id <tt>preparationId</tt> is never returned in the list.
     *
     * @param preparationId the specified preparation id
     * @param sort the sort criterion: either name or date.
     * @param order the sorting order: either asc or desc
     */
    @RequestMapping(value = "/api/preparations/{id}/basedatasets", method = RequestMethod.GET, produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get all data sets that are compatible with a preparation.", notes = "Returns the list of data sets the current user is allowed to see and that are compatible with the preparation.")
    @Timed
    public StreamingResponseBody listCompatibleDatasets(
            @PathVariable(value = "id") @ApiParam(name = "id", value = "Preparation id.") String preparationId,
            @ApiParam(value = "Sort key (by name or date), defaults to 'date'.") @RequestParam(defaultValue = "DATE", required = false) String sort,
            @ApiParam(value = "Order for sort key (desc or asc), defaults to 'desc'.") @RequestParam(defaultValue = "DESC", required = false) String order) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Listing compatible datasets (pool: {} )...", getConnectionStats());
        }

        try {
            // get the preparation
            final Preparation preparation = internalGetPreparation(preparationId);

            // to list compatible datasets
            String dataSetId = preparation.getDataSetId();
            HystrixCommand<InputStream> listCommand = getCommand(CompatibleDataSetList.class, dataSetId, sort, order);
            return CommandHelper.toStreaming(listCommand);
        } finally {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Listing compatible datasets (pool: {}) done.", getConnectionStats());
            }
        }
    }

    //@formatter:off
    @RequestMapping(value = "/api/preparations", method = POST, consumes = APPLICATION_JSON_VALUE, produces = TEXT_PLAIN_VALUE)
    @ApiOperation(value = "Create a new preparation for preparation content in body.", notes = "Returns the created preparation id.")
    @Timed
    public String createPreparation(
            @ApiParam(name = "folder", value = "Where to store the preparation.") @RequestParam(value = "folder") String folder,
            @ApiParam(name = "body", value = "The original preparation. You may set all values, service will override values you can't write to.") @RequestBody Preparation preparation) {
    //@formatter:on

        if (LOG.isDebugEnabled()) {
            LOG.debug("Creating a preparation in {} (pool: {} )...", folder, getConnectionStats());
        }

        PreparationCreate preparationCreate = getCommand(PreparationCreate.class, preparation, folder);
        final String preparationId = preparationCreate.execute();

        LOG.info("new preparation {} created in {}", preparationId, folder);

        return preparationId;
    }

    @RequestMapping(value = "/api/preparations/{id}", method = PUT, consumes = APPLICATION_JSON_VALUE, produces = TEXT_PLAIN_VALUE)
    @ApiOperation(value = "Update a preparation with content in body.", notes = "Returns the updated preparation id.")
    @Timed
    public String updatePreparation(
            @ApiParam(name = "id", value = "The id of the preparation to update.") @PathVariable("id") String id,
            @ApiParam(name = "body", value = "The updated preparation. Null values are ignored during update. You may set all values, service will override values you can't write to.") @RequestBody Preparation preparation) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Updating preparation (pool: {} )...", getConnectionStats());
        }
        PreparationUpdate preparationUpdate = getCommand(PreparationUpdate.class, id, preparation);
        final String preparationId = preparationUpdate.execute();
        if (LOG.isDebugEnabled()) {
            LOG.debug("Updated preparation (pool: {} )...", getConnectionStats());
        }
        return preparationId;
    }

    @RequestMapping(value = "/api/preparations/{id}", method = DELETE, consumes = MediaType.ALL_VALUE, produces = TEXT_PLAIN_VALUE)
    @ApiOperation(value = "Delete a preparation by id", notes = "Delete a preparation content based on provided id. Id should be a UUID returned by the list operation. Not valid or non existing preparation id returns empty content.")
    @Timed
    public String deletePreparation(
            @ApiParam(name = "id", value = "The id of the preparation to delete.") @PathVariable("id") String id) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Deleting preparation (pool: {} )...", getConnectionStats());
        }
        PreparationDelete preparationDelete = getCommand(PreparationDelete.class, id);
        final String preparationId = preparationDelete.execute();
        if (LOG.isDebugEnabled()) {
            LOG.debug("Deleted preparation (pool: {} )...", getConnectionStats());
        }
        return preparationId;
    }


    /**
     * Copy a preparation from the given id
     *
     * @param id the preparation id to copy
     * @param destination where to copy the preparation to.
     * @param newName optional new name for the preparation.
     * @return The copied preparation id.
     */
    //@formatter:off
    @RequestMapping(value = "/api/preparations/{id}/copy", method = POST, produces = TEXT_PLAIN_VALUE)
    @ApiOperation(value = "Copy a preparation", produces = TEXT_PLAIN_VALUE, notes = "Copy a preparation based the provided id.")
    public String copy(
            @ApiParam(value = "Id of the preparation to copy") @PathVariable(value = "id") String id,
            @ApiParam(value = "Optional new name of the copied preparation, if not set the copy will get the original name.") @RequestParam(required = false) String newName,
            @ApiParam(value = "The destination path to create the entry.") @RequestParam(required = false) String destination) {
    //@formatter:on

        if (LOG.isDebugEnabled()) {
            LOG.debug("Copying preparation {} to '{}' with new name '{}' (pool: {} )...", id, destination, newName, getConnectionStats());
        }

        HystrixCommand<String> copy = getCommand(PreparationCopy.class, id, destination, newName);
        String copyId = copy.execute();

        LOG.info("Preparation {} copied to {}/{} done --> {}", id, destination, newName, copyId);

        return copyId;
    }


    /**
     * Move a preparation to another folder.
     *
     * @param id the preparation id to move.
     * @param folder where to find the preparation.
     * @param destination where to move the preparation.
     * @param newName optional new preparation name.
     */
    //@formatter:off
    @RequestMapping(value = "/api/preparations/{id}/move", method = PUT, produces = TEXT_PLAIN_VALUE)
    @ApiOperation(value = "Move a Preparation", produces = TEXT_PLAIN_VALUE, notes = "Move a preparation to another folder.")
    @Timed
    public void move(@PathVariable(value = "id") @ApiParam(name = "id", value = "Id of the preparation to move") String id,
                     @ApiParam(value = "The original folder path of the preparation.") @RequestParam(defaultValue = "", required = false) String folder,
                     @ApiParam(value = "The new folder path of the preparation.") @RequestParam() String destination,
                     @ApiParam(value = "The new name of the moved dataset.") @RequestParam(defaultValue = "", required = false) String newName) throws IOException {
    //@formatter:on

        if (LOG.isDebugEnabled()) {
            LOG.debug("Moving preparation (pool: {} )...", getConnectionStats());
        }

        HystrixCommand<Void> move = getCommand(PreparationMove.class, id, folder, destination, newName);
        move.execute();

        LOG.info("Preparation {} moved from {} to {}/'{}'", id, folder, destination, newName);
    }


    @RequestMapping(value = "/api/preparations/{id}/details", method = RequestMethod.GET, produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get a preparation by id and details.", notes = "Returns the preparation details.")
    @Timed
    public StreamingResponseBody getPreparation(@PathVariable(value = "id") @ApiParam(name = "id", value = "Preparation id.") String preparationId) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Retrieving preparation details (pool: {} )...", getConnectionStats());
        }

        // You cannot use Preparation object mapper here: to serialize steps & actions, you'd need a version
        // repository not available at API level. Code below copies command result direct to response.
        try {
            GenericCommand<InputStream> command = getCommand(PreparationDetailsGet.class, preparationId);
            return CommandHelper.toStreaming(command);
        } finally {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Retrieved preparation details (pool: {} )...", getConnectionStats());
            }
        }
    }

    @RequestMapping(value = "/api/preparations/{id}/content", method = RequestMethod.GET, produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get preparation content by id and at a given version.", notes = "Returns the preparation content at version.")
    @Timed
    public StreamingResponseBody getPreparation(@PathVariable(value = "id") @ApiParam(name = "id", value = "Preparation id.") String preparationId,
            @RequestParam(value = "version", defaultValue = "head") @ApiParam(name = "version", value = "Version of the preparation (can be 'origin', 'head' or the version id). Defaults to 'head'.") String version,
            @RequestParam(required = false, defaultValue = "full") @ApiParam(name = "sample", value = "Size of the wanted sample, if missing or 'full', the full preparation content is returned") String sample) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Retrieving preparation content (pool: {} )...", getConnectionStats());
        }
        Long sampleValue;
        try {
            sampleValue = Long.parseLong(sample);
        } catch (NumberFormatException e) {
            sampleValue = null;
        }

        try {
            final PreparationDetailsGet preparationDetailsGet = getCommand(PreparationDetailsGet.class, preparationId);
            HystrixCommand<InputStream> command = getCommand(PreparationGetContent.class, preparationId, version, sampleValue, preparationDetailsGet);
            return CommandHelper.toStreaming(command);
        } finally {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Retrieved preparation content (pool: {} )...", getConnectionStats());
            }
        }
    }

    //@formatter:off
    @RequestMapping(value = "/api/preparations/{id}/actions", method = POST, produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Adds an action at the end of preparation.", notes = "Does not return any value, client may expect successful operation based on HTTP status code.")
    @Timed
    public void addPreparationAction(@ApiParam(name = "id", value = "Preparation id.") @PathVariable(value = "id")  final String preparationId,
                                     @ApiParam("Action to add at end of the preparation.") @RequestBody final AppendStep step) {
    //@formatter:on

        if (LOG.isDebugEnabled()) {
            LOG.debug("Adding action to preparation (pool: {} )...", getConnectionStats());
        }

        // get the preparation
        Preparation preparation = internalGetPreparation(preparationId);

        // get the preparation actions
        final PreparationGetActions getActionsCommand = getCommand(PreparationGetActions.class, preparationId);

        // get the diff
        final DiffMetadata diffCommand = getCommand(DiffMetadata.class, preparation.getDataSetId(), preparationId,
                step.getActions(), getActionsCommand);

        // add the action
        final HystrixCommand<Void> command = getCommand(PreparationAddAction.class, preparationId, step, diffCommand);
        command.execute();
        if (LOG.isDebugEnabled()) {
            LOG.debug("Added action to preparation (pool: {} )...", getConnectionStats());
        }
    }

    //@formatter:off
    @RequestMapping(value = "/api/preparations/{preparationId}/actions/{stepId}", method = PUT, produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Updates an action in the preparation.", notes = "Does not return any value, client may expect successful operation based on HTTP status code.")
    @Timed
    public void updatePreparationAction(@ApiParam(name = "preparationId", value = "Preparation id.") @PathVariable(value = "preparationId") final String preparationId,
                                        @ApiParam(name = "stepId", value = "Step id in the preparation.") @PathVariable(value = "stepId") final String stepId,
                                        @ApiParam("New content for the action.") @RequestBody final AppendStep step) {
    //@formatter:on

        if (LOG.isDebugEnabled()) {
            LOG.debug("Updating preparation action at step #{} (pool: {} )...", stepId, getConnectionStats());
        }

        // get the preparation
        Preparation preparation = internalGetPreparation(preparationId);

        // get the preparation actions for up to the updated action
        final int stepIndex = preparation.getSteps().indexOf(stepId);
        final String parentStepId = preparation.getSteps().get(stepIndex - 1);
        final PreparationGetActions getActionsCommand = getCommand(PreparationGetActions.class, preparationId, parentStepId);

        // get the diff
        final DiffMetadata diffCommand = getCommand(DiffMetadata.class, preparation.getDataSetId(), preparationId,
                step.getActions(), getActionsCommand);

        // get the update action command and execute it
        final HystrixCommand<Void> command = getCommand(PreparationUpdateAction.class, preparationId, stepId, step, diffCommand);
        command.execute();

        if (LOG.isDebugEnabled()) {
            LOG.debug("Updated preparation action at step #{} (pool: {} )...", stepId, getConnectionStats());
        }
    }

    @RequestMapping(value = "/api/preparations/{id}/actions/{stepId}", method = DELETE, produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Delete an action in the preparation.", notes = "Does not return any value, client may expect successful operation based on HTTP status code.")
    @Timed
    public void deletePreparationAction(@PathVariable(value = "id")
    @ApiParam(name = "id", value = "Preparation id.")
    final String preparationId, @PathVariable(value = "stepId")
    @ApiParam(name = "stepId", value = "Step id to delete.")
    final String stepId) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Deleting preparation action at step #{} (pool: {} ) ...", stepId, //
                    getConnectionStats());
        }

        final HystrixCommand<Void> command = getCommand(PreparationDeleteAction.class, preparationId, stepId);
        command.execute();

        if (LOG.isDebugEnabled()) {
            LOG.debug("Deleted preparation action at step #{} (pool: {} ) ...", stepId, //
                    getConnectionStats());
        }
    }

    //@formatter:off
    @RequestMapping(value = "/api/preparations/{id}/head/{headId}", method = PUT, produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Delete an action in the preparation.", notes = "Does not return any value, client may expect successful operation based on HTTP status code.")
    @Timed
    public void setPreparationHead(@PathVariable(value = "id") @ApiParam(name = "id", value = "Preparation id.") final String preparationId,
                                   @PathVariable(value = "headId") @ApiParam(name = "headId", value = "New head step id") final String headId) {
    //@formatter:on

        if (LOG.isDebugEnabled()) {
            LOG.debug("Moving preparation #{} head to step '{}'...", preparationId, headId);
        }

        final HystrixCommand<Void> command = getCommand(PreparationMoveHead.class, preparationId, headId);
        command.execute();

        if (LOG.isDebugEnabled()) {
            LOG.debug("Moved preparation #{} head to step '{}'...", preparationId, headId);
        }
    }


    @RequestMapping(value = "/api/preparations/{preparationId}/lock", method = PUT, produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Mark a preparation as locked by a user.", notes = "Does not return any value, client may expect successful operation based on HTTP status code.")
    @Timed
    public void lockPreparation(@PathVariable(value = "preparationId")
                                   @ApiParam(name = "preparationId", value = "Preparation id.")
                                   final String preparationId) {

        if (LOG.isDebugEnabled()) {
            LOG.debug("Locking preparation #{}...", preparationId);
        }


        final HystrixCommand<Void> command = getCommand(PreparationLock.class, preparationId);
        command.execute();

        if (LOG.isDebugEnabled()) {
            LOG.debug("Locked preparation #{}...", preparationId);
        }
    }

    @RequestMapping(value = "/api/preparations/{preparationId}/unlock", method = PUT, produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Mark a preparation as unlocked by a user.", notes = "Does not return any value, client may expect successful operation based on HTTP status code.")
    @Timed
    public void unlockPreparation(@PathVariable(value = "preparationId")
                                @ApiParam(name = "preparationId", value = "Preparation id.")
                                final String preparationId) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Locking preparation #{}...", preparationId);
        }

        final HystrixCommand<Void> command = getCommand(PreparationUnlock.class, preparationId);
        command.execute();

        if (LOG.isDebugEnabled()) {
            LOG.debug("Locked preparation #{}...", preparationId);
        }
    }



    /**
     * Copy the steps from the another preparation to this one.
     *
     * This is only allowed if this preparation has no steps.
     *
     * @param id the preparation id to update.
     * @param from the preparation id to copy the steps from.
     */
    //@formatter:off
    @RequestMapping(value = "/api/preparations/{id}/steps/copy", method = PUT, produces = TEXT_PLAIN_VALUE)
    @ApiOperation(value = "Copy the steps from another preparation", notes = "Copy the steps from another preparation if this one has no steps.")
    @Timed
    public void copyStepsFrom(@ApiParam(value="the preparation id to update") @PathVariable("id")String id,
                              @ApiParam(value = "the preparation to copy the steps from.") @RequestParam String from) {
    //@formatter:on

        LOG.debug("copy preparations steps from {} to {}", from, id);

        final HystrixCommand<Void> command = getCommand(PreparationCopyStepsFrom.class, id, from);
        command.execute();

        LOG.info("preparation's steps copied from {} to {}", from, id);
    }


    // ---------------------------------------------------------------------------------
    // ----------------------------------------PREVIEW----------------------------------
    // ---------------------------------------------------------------------------------

    //@formatter:off
    @RequestMapping(value = "/api/preparations/preview/diff", method = POST, consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get a preview diff between 2 steps of the same preparation.")
    @Timed
    public StreamingResponseBody previewDiff(@RequestBody final PreviewDiffParameters input) {
    //@formatter:on

        // get preparation details
        final Preparation preparation = internalGetPreparation(input.getPreparationId());
        final List<Action> lastActiveStepActions = internalGetActions(preparation.getId(), input.getCurrentStepId());
        final List<Action> previewStepActions = internalGetActions(preparation.getId(), input.getPreviewStepId());

        final HystrixCommand<InputStream> transformation = getCommand(PreviewDiff.class, input, preparation, lastActiveStepActions, previewStepActions);
        return executePreviewCommand(transformation);
    }

    //@formatter:off
    @RequestMapping(value = "/api/preparations/preview/update", method = POST, consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get a preview diff between the same step of the same preparation but with one step update.")
    public StreamingResponseBody previewUpdate(@RequestBody final PreviewUpdateParameters input) {
    //@formatter:on

        // get preparation details
        final Preparation preparation = internalGetPreparation(input.getPreparationId());
        final List<Action> actions = internalGetActions(preparation.getId());

        final HystrixCommand<InputStream> transformation = getCommand(PreviewUpdate.class, input, preparation, actions);
        return executePreviewCommand(transformation);
    }

    //@formatter:off
    @RequestMapping(value = "/api/preparations/preview/add", method = POST, consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get a preview between the head step and a new appended transformation")
    public StreamingResponseBody previewAdd(@RequestBody @Valid final PreviewAddParameters input) {
    //@formatter:on

        Preparation preparation = null;
        List<Action> actions = new ArrayList<>(0);

        // get preparation details with dealing with preparations
        if (StringUtils.isNotBlank(input.getPreparationId())) {
            preparation = internalGetPreparation(input.getPreparationId());
            actions = internalGetActions(preparation.getId());
        }

        final HystrixCommand<InputStream> transformation = getCommand(PreviewAdd.class, input, preparation, actions);
        return executePreviewCommand(transformation);
    }

    private StreamingResponseBody executePreviewCommand(HystrixCommand<InputStream> transformation) {
        return CommandHelper.toStreaming(transformation);
    }

    /**
     * Helper method used to retrieve preparation actions via a hystrix command.
     *
     * @param preparationId the preparation id to get the actions from.
     * @return the preparation actions.
     */
    private List<Action> internalGetActions(String preparationId) {
        return internalGetActions(preparationId, "head");
    }

    /**
     * Helper method used to retrieve preparation actions via a hystrix command.
     *
     * @param preparationId the preparation id to get the actions from.
     * @param stepId the preparation version.
     * @return the preparation actions.
     */
    private List<Action> internalGetActions(String preparationId, String stepId) {
        final PreparationGetActions getActionsCommand = getCommand(PreparationGetActions.class, preparationId, stepId);
        try {
            return mapper.readerFor(new TypeReference<List<Action>>() {
            }).readValue(getActionsCommand.execute());
        } catch (IOException e) {
            throw new TDPException(APIErrorCodes.UNABLE_TO_GET_PREPARATION_DETAILS, e);
        }
    }


    /**
     * Helper method used to get a preparation for internal class use.
     *
     * @param preparationId the preparation id.
     * @return the preparation.
     */
    private Preparation internalGetPreparation(String preparationId) {
        try {
            GenericCommand<InputStream> command = getCommand(PreparationDetailsGet.class, preparationId);
            return mapper.readerFor(Preparation.class).readValue(command.execute());
        } catch (IOException e) {
            throw new TDPException(UNABLE_TO_READ_PREPARATION, e, withBuilder().put("id", preparationId).build());
        }
    }

}
