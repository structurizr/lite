package com.structurizr.lite.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WorkspaceLastModifiedController extends AbstractController {

    @RequestMapping(value = "/workspace/lastModified", method = RequestMethod.GET, produces = "plain/text; charset=UTF-8")
    @ResponseBody
    public String lastModified() {
        return "" + workspaceComponent.getLastModifiedDate();
    }

}