package com.structurizr.lite.web;

import com.structurizr.Workspace;
import com.structurizr.io.WorkspaceReaderException;
import com.structurizr.io.json.JsonReader;
import com.structurizr.lite.Configuration;
import com.structurizr.lite.component.workspace.WorkspaceComponentException;
import com.structurizr.util.StringUtils;
import com.structurizr.util.WorkspaceUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.StringReader;
import java.util.Base64;

/**
 * An implementation of the Structurizr web API, consisting of two operations to
 * get and put JSON workspace definitions.
 *
 *  - GET /api/workspace/{id}
 *  - PUT /api/workspace/{id}
 */
@RestController
public class ApiController extends AbstractController {

    private static Log log = LogFactory.getLog(ApiController.class);

    @RequestMapping(value = "/api/workspace/{workspaceId}", method = RequestMethod.GET, produces = "application/json; charset=UTF-8")
    public String getWorkspace(@PathVariable("workspaceId") long workspaceId,
                               HttpServletRequest request, HttpServletResponse response) {
        try {
            authoriseRequest("GET", getPath(request, workspaceId), null, request, response);

            Workspace workspace = workspaceComponent.getWorkspace(workspaceId);
            if (workspace != null) {
                return WorkspaceUtils.toJson(workspace, false);
            } else {
                throw new ApiException(workspaceComponent.getError());
            }
        } catch (Exception e) {
            log.error(e);
            throw new ApiException(e.getMessage());
        }
    }

    private String getPath(HttpServletRequest request, long workspaceId) {
        String contextPath = request.getContextPath();
        if (!contextPath.endsWith("/")) {
            contextPath = contextPath + "/";
        }

        return contextPath + "api/workspace/" + workspaceId;
    }

    @RequestMapping(value = "/api/workspace/{workspaceId}", method = RequestMethod.PUT, consumes = "application/json", produces = "application/json; charset=UTF-8")
    public @ResponseBody ApiResponse putWorkspace(@PathVariable("workspaceId")long workspaceId, @RequestBody String json, HttpServletRequest request, HttpServletResponse response) {
        try {
            authoriseRequest("PUT", getPath(request, workspaceId), json, request, response);

            Workspace workspace = WorkspaceUtils.fromJson(json);
            workspace.setId(workspaceId);
            workspaceComponent.putWorkspace(workspace);

            return new ApiResponse("OK");
        } catch (Exception e) {
            log.error(e);
            throw new ApiException(e.getMessage());
        }
    }

    private void authoriseRequest(String httpMethod, String path, String content, HttpServletRequest request, HttpServletResponse response) throws WorkspaceComponentException {
        try {
            String authorizationHeaderAsString = request.getHeader(HttpHeaders.X_AUTHORIZATION);
            if (authorizationHeaderAsString == null || authorizationHeaderAsString.trim().length() == 0) {
                throw new HttpUnauthorizedException("Authorization header must be provided");
            }

            HmacAuthorizationHeader hmacAuthorizationHeader = HmacAuthorizationHeader.parse(authorizationHeaderAsString);
            String apiKeyFromAuthorizationHeader = hmacAuthorizationHeader.getApiKey();

            if (!apiKeyFromAuthorizationHeader.equals(Configuration.getInstance().getApiKey())) {
                throw new HttpUnauthorizedException("Incorrect API key");
            }

            String contentType = request.getHeader(HttpHeaders.CONTENT_TYPE);
            if (StringUtils.isNullOrEmpty(contentType) || contentType.startsWith(";")) {
                contentType = "";
            } else if (!contentType.contains(" ")) {
                String[] parts = contentType.split(";");
                contentType = parts[0] + "; " + parts[1];
            }

            String nonce = request.getHeader(HttpHeaders.NONCE);
            if (nonce == null || nonce.length() == 0) {
                throw new HttpUnauthorizedException("Request header missing: " + HttpHeaders.NONCE);
            }

            String contentMd5InRequest;
            String contentMd5Header = request.getHeader(HttpHeaders.CONTENT_MD5);

            if (!StringUtils.isNullOrEmpty(content)) {
                if (contentMd5Header == null || contentMd5Header.length() == 0) {
                    throw new HttpUnauthorizedException("Request header missing: " + HttpHeaders.CONTENT_MD5);
                }

                contentMd5InRequest = new String(Base64.getDecoder().decode(contentMd5Header));

                String generatedContentMd5 = new Md5Digest().generate(content);
                if (!contentMd5InRequest.equals(generatedContentMd5)) {
                    // the content has been tampered with?
                    throw new HttpUnauthorizedException("MD5 hash doesn't match content");
                }
            } else {
                contentMd5InRequest = "d41d8cd98f00b204e9800998ecf8427e"; // this is the MD5 hash of an empty string
            }

            HashBasedMessageAuthenticationCode code = new HashBasedMessageAuthenticationCode(Configuration.getInstance().getApiSecret());
            String hmacInRequest = hmacAuthorizationHeader.getHmac();
            HmacContent hmacContent = new HmacContent(httpMethod, path, contentMd5InRequest, contentType, nonce);
            String generatedHmac = code.generate(hmacContent.toString());
            if (!hmacInRequest.equals(generatedHmac)) {
                throw new HttpUnauthorizedException("Authorization header doesn't match");
            }
        } catch (Exception e) {
            log.error(e);
            throw new HttpUnauthorizedException(e.getMessage());
        }
    }

    @ExceptionHandler(HttpUnauthorizedException.class)
    @ResponseBody
    public ApiResponse handleCustomException(HttpUnauthorizedException exception, HttpServletResponse response) {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        return new ApiResponse(exception);
    }

    @ExceptionHandler(ApiException.class)
    @ResponseBody
    public ApiResponse handleCustomException(ApiException exception, HttpServletResponse response) {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        return new ApiResponse(exception);
    }

    @ExceptionHandler(Throwable.class)
    @ResponseBody
    public ApiResponse error(Throwable t, HttpServletResponse response) {
        while (t.getCause() != null) {
            t = t.getCause();
        }
        t.printStackTrace();
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        return new ApiResponse(false, t.getMessage());
    }

}