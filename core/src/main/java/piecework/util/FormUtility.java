/*
 * Copyright 2013 University of Washington
 *
 * Licensed under the Educational Community License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.opensource.org/licenses/ecl1.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package piecework.util;

import org.apache.commons.lang.StringUtils;
import org.apache.http.Header;
import org.apache.log4j.Logger;
import piecework.common.ViewContext;
import piecework.enumeration.ActionType;
import piecework.enumeration.ActivityUsageType;
import piecework.exception.FormBuildingException;
import piecework.exception.MisconfiguredProcessException;
import piecework.exception.PieceworkException;
import piecework.form.FormDisposition;
import piecework.model.*;
import piecework.model.Process;
import piecework.persistence.ProcessDeploymentProvider;
import piecework.settings.UserInterfaceSettings;

import javax.ws.rs.core.Response;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;

/**
 * @author James Renfro
 */
public class FormUtility {

    private static final Logger LOG = Logger.getLogger(FormUtility.class);

    public static void addConfirmationNumber(Field.Builder fieldBuilder, String confirmationNumber) {
        String defaultValue = fieldBuilder.getDefaultValue();
        fieldBuilder.defaultValue(defaultValue.replaceAll("\\{\\{ConfirmationNumber\\}\\}", confirmationNumber));
    }

    public static void addStateOptions(Field.Builder fieldBuilder) {
        fieldBuilder.option(new Option.Builder().value("").label("").build())
                .option(new Option.Builder().value("AL").label("Alabama").build())
                .option(new Option.Builder().value("AK").label("Alaska").build())
                .option(new Option.Builder().value("AZ").label("Arizona").build())
                .option(new Option.Builder().value("AR").label("Arkansas").build())
                .option(new Option.Builder().value("CA").label("California").build())
                .option(new Option.Builder().value("CO").label("Colorado").build())
                .option(new Option.Builder().value("CT").label("Connecticut").build())
                .option(new Option.Builder().value("DE").label("Delaware").build())
                .option(new Option.Builder().value("DC").label("District Of Columbia").build())
                .option(new Option.Builder().value("FL").label("Florida").build())
                .option(new Option.Builder().value("GA").label("Georgia").build())
                .option(new Option.Builder().value("HI").label("Hawaii").build())
                .option(new Option.Builder().value("ID").label("Idaho").build())
                .option(new Option.Builder().value("IL").label("Illinois").build())
                .option(new Option.Builder().value("IN").label("Indiana").build())
                .option(new Option.Builder().value("IA").label("Iowa").build())
                .option(new Option.Builder().value("KS").label("Kansas").build())
                .option(new Option.Builder().value("KY").label("Kentucky").build())
                .option(new Option.Builder().value("LA").label("Louisiana").build())
                .option(new Option.Builder().value("ME").label("Maine").build())
                .option(new Option.Builder().value("MD").label("Maryland").build())
                .option(new Option.Builder().value("MA").label("Massachusetts").build())
                .option(new Option.Builder().value("MI").label("Michigan").build())
                .option(new Option.Builder().value("MN").label("Minnesota").build())
                .option(new Option.Builder().value("MS").label("Mississippi").build())
                .option(new Option.Builder().value("MO").label("Missouri").build())
                .option(new Option.Builder().value("MT").label("Montana").build())
                .option(new Option.Builder().value("NE").label("Nebraska").build())
                .option(new Option.Builder().value("NV").label("Nevada").build())
                .option(new Option.Builder().value("NH").label("New Hampshire").build())
                .option(new Option.Builder().value("NJ").label("New Jersey").build())
                .option(new Option.Builder().value("NM").label("New Mexico").build())
                .option(new Option.Builder().value("NY").label("New York").build())
                .option(new Option.Builder().value("NC").label("North Carolina").build())
                .option(new Option.Builder().value("ND").label("North Dakota").build())
                .option(new Option.Builder().value("OH").label("Ohio").build())
                .option(new Option.Builder().value("OK").label("Oklahoma").build())
                .option(new Option.Builder().value("OR").label("Oregon").build())
                .option(new Option.Builder().value("PA").label("Pennsylvania").build())
                .option(new Option.Builder().value("RI").label("Rhode Island").build())
                .option(new Option.Builder().value("SC").label("South Carolina").build())
                .option(new Option.Builder().value("SD").label("South Dakota").build())
                .option(new Option.Builder().value("TN").label("Tennessee").build())
                .option(new Option.Builder().value("TX").label("Texas").build())
                .option(new Option.Builder().value("UT").label("Utah").build())
                .option(new Option.Builder().value("VT").label("Vermont").build())
                .option(new Option.Builder().value("VA").label("Virginia").build())
                .option(new Option.Builder().value("WA").label("Washington").build())
                .option(new Option.Builder().value("WV").label("West Virginia").build())
                .option(new Option.Builder().value("WI").label("Wisconsin").build())
                .option(new Option.Builder().value("WY").label("Wyoming").build());
    }

    public static <P extends ProcessDeploymentProvider> FormDisposition disposition(P modelProvider, Activity activity, ActionType actionType, ViewContext context, Form.Builder builder) throws PieceworkException {
        Action action = activity.action(actionType);
        boolean revertToDefaultUI = false;

        // If there is no action defined, then revert to CREATE_TASK
        if (action == null) {
            action = activity.action(ActionType.CREATE);
            // If the action type was VIEW then revert to the default ui, use create as the action, but make it unmodifiable
            if (actionType == ActionType.VIEW) {
                revertToDefaultUI = true;
                if (builder != null)
                    builder.readonly();
            }
        }

        if (action == null)
            throw new MisconfiguredProcessException("Action is null for this activity and type " + actionType);

        Process process = modelProvider.process();
        ProcessDeployment deployment = modelProvider.deployment();
        FormDisposition formDisposition = FormDisposition.Builder.build(process, deployment, action, context);
        FormUtility.layout(builder, activity);

        return formDisposition;
    }

    public static void layout(Form.Builder builder, Activity activity) {
        if (activity == null || builder == null)
            return;

        ActivityUsageType usageType = activity.getUsageType() != null ? activity.getUsageType() : ActivityUsageType.USER_FORM;
        switch (usageType) {
            case MULTI_PAGE:
                builder.layout("multipage");
                break;
            case MULTI_STEP:
                builder.layout("multistep");
                break;
            case REVIEW_PAGE:
                builder.layout("review");
                break;
            default:
                builder.layout("normal");
                break;
        }
    }

    public static boolean isExternal(URI uri) {

        if (uri != null) {
            String scheme = uri.getScheme();
            return StringUtils.isNotEmpty(scheme) && (scheme.equals("http") || scheme.equals("https"));
        }

        return false;
    }

    public static Response createResponse(UserInterfaceSettings settings, ProcessDeploymentProvider deploymentProvider, URI location, boolean isAnonymous) throws PieceworkException {
        Response.ResponseBuilder builder = Response.created(location);

        addCrossOriginHeaders(settings, builder, deploymentProvider, null, isAnonymous);

        return builder.build();
    }

    public static Response okResponse(UserInterfaceSettings settings, ProcessDeploymentProvider deploymentProvider, Object entity, String contentType, boolean isAnonymous) throws PieceworkException {
        return allowCrossOriginResponse(settings, deploymentProvider, entity, contentType, Collections.<Header>emptyList(), isAnonymous);
    }

    public static Response okResponse(UserInterfaceSettings settings, ProcessDeploymentProvider deploymentProvider, Object entity, String contentType, List<Header> headers, boolean isAnonymous) throws PieceworkException {
        return allowCrossOriginResponse(settings, deploymentProvider, entity, contentType, headers, isAnonymous);
    }

    public static Response optionsResponse(UserInterfaceSettings settings, ProcessDeploymentProvider deploymentProvider, boolean isAnonymous, String... methods) throws PieceworkException {
        return allowCrossOriginResponse(settings, deploymentProvider, null, null, Collections.<Header>emptyList(), isAnonymous, methods);
    }

    public static Response noContentResponse(UserInterfaceSettings settings, ProcessDeploymentProvider deploymentProvider, boolean isAnonymous, String... methods) throws PieceworkException {
        return noContentResponse(settings, deploymentProvider, null, isAnonymous, methods);
    }

    public static Response noContentResponse(UserInterfaceSettings settings, ProcessDeploymentProvider deploymentProvider, List<Header> headers, boolean isAnonymous, String... methods) throws PieceworkException {
        Response.ResponseBuilder builder = Response.noContent();

        addCrossOriginHeaders(settings, builder, deploymentProvider, null, isAnonymous, methods);

        if (headers != null) {
            for (Header header : headers) {
                if (StringUtils.isNotEmpty(header.getName()) && StringUtils.isNotEmpty(header.getValue()))
                    builder.header(header.getName(), header.getValue());
            }
        }

        return builder.build();
    }

//    public static Response allowCrossOriginResponse(UserInterfaceSettings settings, ProcessDeploymentProvider deploymentProvider, Object entity, boolean isAnonymous) throws PieceworkException{
//        return allowCrossOriginResponse(settings, deploymentProvider, entity, null, Collections.<Header>emptyList(), isAnonymous);
//    }

//    public static Response allowCrossOriginOptionsResponse(UserInterfaceSettings settings, ProcessDeploymentProvider deploymentProvider, boolean isAnonymous,  String... methods) throws PieceworkException {
//        return allowCrossOriginResponse(settings, deploymentProvider, null, null, Collections.<Header>emptyList(), isAnonymous, methods);
//    }

    public static Response allowCrossOriginResponse(UserInterfaceSettings settings, ProcessDeploymentProvider deploymentProvider, Object entity, String contentType, List<Header> headers, boolean isAnonymous, String... methods) throws PieceworkException {
        Response.ResponseBuilder builder = entity != null ? Response.ok(entity, contentType) : Response.ok();

        addCrossOriginHeaders(settings, builder, deploymentProvider, entity, isAnonymous, methods);

        if (headers != null) {
            for (Header header : headers) {
                if (StringUtils.isNotEmpty(header.getName()) && StringUtils.isNotEmpty(header.getValue()))
                    builder.header(header.getName(), header.getValue());
            }
        }

        return builder.build();
    }

    public static void addCrossOriginHeaders(UserInterfaceSettings settings, Response.ResponseBuilder builder, ProcessDeploymentProvider deploymentProvider, Object entity, boolean isAnonymous, String... methods) throws PieceworkException {
        String methodHeader = null;

        if (entity != null && methods != null && methods.length > 0)
            methodHeader = StringUtils.join(methods, ",");

        // Never allow CORS for anonymous resources
        if (!isAnonymous) {

            URI remoteHost = remoteHost(settings, deploymentProvider);

            if (remoteHost != null) {
                String hostUri = remoteHost.toString();
                if (LOG.isDebugEnabled())
                    LOG.debug("Setting Access-Control-Allow-Origin to " + hostUri);
                builder.header("Access-Control-Allow-Origin", hostUri);
                builder.header("Access-Control-Allow-Credentials", "true");
                // For file upload
                builder.header("Access-Control-Allow-Headers", "Accept, Content-Type, Content-Range, Content-Disposition, Content-Description");
                if (StringUtils.isNotEmpty(methodHeader))
                    builder.header("Access-Control-Allow-Methods", methodHeader);
            }
        }

        if (StringUtils.isNotEmpty(methodHeader))
            builder.header("Allow", methodHeader);
    }

    public static URI remoteHost(UserInterfaceSettings settings, ProcessDeploymentProvider deploymentProvider) throws PieceworkException {
        URI remoteHostUri = null;
        ProcessDeployment deployment = deploymentProvider.deployment();
        String remoteHost = deployment.getRemoteHost();
        String serverHost = settings.getHostUri();

        if (StringUtils.isNotEmpty(remoteHost) && StringUtils.isNotEmpty(serverHost)) {
            try {
                remoteHostUri = new URI(remoteHost);
                URI serverHostUri = new URI(serverHost);

                // Don't return a remote host if in fact the remote host is the same as the server host
                // because it means that we're not doing CORS for this request
                if (remoteHostUri.equals(serverHostUri))
                    return null;

            } catch (URISyntaxException iae) {
                LOG.error("Failed to convert remote host or server host location into uri:" + remoteHost + ", " + serverHost, iae);
            }
        }
        return remoteHostUri;
    }

    public static URI safeUri(String remoteHost, Action action) {
        URI uri = null;
        String location = StringUtils.isNotEmpty(remoteHost) ? remoteHost + action.getLocation() : action.getLocation();
        try {
            if (StringUtils.isNotEmpty(location))
                uri = new URI(location);
        } catch (URISyntaxException iae) {
            LOG.error("Failed to convert location into uri:" + location, iae);
        }
        return uri;
    }
}
