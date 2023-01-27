<%@ include file="/WEB-INF/fragments/workspace/javascript.jspf" %>
<%@ include file="/WEB-INF/fragments/progress-message.jspf" %>
<%@ include file="/WEB-INF/fragments/quick-navigation.jspf" %>

<script type="text/javascript" src="${structurizrConfiguration.cdnUrl}/js/d3-7.8.2.min.js"></script>
<script type="text/javascript" src="${structurizrConfiguration.cdnUrl}/js/structurizr-d3${structurizrConfiguration.versionSuffix}.js"></script>
<script type="text/javascript" src="${structurizrConfiguration.cdnUrl}/js/structurizr-fullscreen${structurizrConfiguration.versionSuffix}.js"></script>
<script type="text/javascript" src="${structurizrConfiguration.cdnUrl}/js/structurizr-ui${structurizrConfiguration.versionSuffix}.js"></script>

<div id="graphContent" >
    <div id="exploreGraph" style="width: 100%"></div>
    <div id="graphTitle" style="position: absolute; bottom: 10px; left: 10px; color: #aaaaaa; font-size: 13px; user-select: none; -moz-user-select: none; -khtml-user-select: none; -webkit-user-select: none; -o-user-select: none;"></div>

    <%@ include file="/WEB-INF/fragments/tooltip.jspf" %>

    <div id="embeddedControls" style="text-align: right; position: absolute; bottom: 10px; right: 10px; opacity: 0.1; z-index: 100;">
        <button class="btn btn-default" id="enterFullScreenButton" title="Enter Full Screen [f]" onclick="Structurizr.enterFullScreen('graphContent')"><img src="${structurizrConfiguration.cdnUrl}/bootstrap-icons/fullscreen.svg" class="icon-btn" /></button>
        <button class="btn btn-default hidden" id="exitFullScreenButton" title="Exit Full Screen [Escape]" onclick="Structurizr.exitFullScreen()"><img src="${structurizrConfiguration.cdnUrl}/bootstrap-icons/fullscreen-exit.svg" class="icon-btn" /></button>
        <c:if test="${embed eq true}">
            <button class="btn btn-default" title="Open graph in new window" onclick="openGraphInNewWindow()"><img src="${structurizrConfiguration.cdnUrl}/bootstrap-icons/link.svg" class="icon-btn" /></button>
        </c:if>
    </div>
</div>

<script nonce="${scriptNonce}">
    var numberOfThemesToBeLoaded = undefined;
    var margin = 0;
    var width;
    var height;

    var graph;
    var simulation;
    var distance;
    var node;
    var link;
    var nodeHighlighted;

    const viewKey = '${view}';
    var view;

    const relationshipsBySourceAndDestination = [];

    const nodeSizes = {};
    nodeSizes[structurizr.constants.CUSTOM_ELEMENT_TYPE] = 20;
    nodeSizes[structurizr.constants.PERSON_ELEMENT_TYPE] = 20;
    nodeSizes[structurizr.constants.SOFTWARE_SYSTEM_ELEMENT_TYPE] = 20;
    nodeSizes[structurizr.constants.CONTAINER_ELEMENT_TYPE] = 17;
    nodeSizes[structurizr.constants.COMPONENT_ELEMENT_TYPE] = 11;


    progressMessage.show('<p>Loading workspace...</p>');

    function workspaceLoaded() {
        if (numberOfThemesToBeLoaded === undefined) {

            try {
                if (structurizr.workspace.getViews().configuration.themes.length > 0) {
                    numberOfThemesToBeLoaded = structurizr.workspace.getViews().configuration.themes.length;

                    structurizr.workspace.getViews().configuration.themes.forEach(function(theme) {
                        applyThemeFrom(theme);
                    });
                } else {
                    numberOfThemesToBeLoaded = 0;
                }
            } catch (e) {
                console.log(e);
            }
        }

        if (numberOfThemesToBeLoaded > 0) {
            setTimeout(workspaceLoaded, 100);
        } else {
            setWidthAndHeight();
            createGraph();
            renderGraph();
            addEventHandlers();
        }

        structurizr.ui.applyBranding(structurizr.workspace.getViews().configuration.branding);
        progressMessage.hide();
    }

    function applyThemeFrom(themeUrl) {
        $.get(themeUrl, undefined, function(data) {
            try {
                //Structurizr.workspace.addTheme(JSON.parse(data)); // todo
                numberOfThemesToBeLoaded--;
            } catch (e) {
                console.log('Could not load theme from ' + themeUrl);
                console.log(e);
                numberOfThemesToBeLoaded--;
            }
        }, 'text')
            .fail(function(xhr, textStatus, errorThrown) {
                const errorMessage = 'Could not load theme from ' + themeUrl + '; error ' + xhr.status + ' (' + xhr.statusText + ')';
                console.log(errorMessage);
                alert(errorMessage);
                numberOfThemesToBeLoaded--;
            });
    }

    function createGraph() {
        graph = { nodes: [], links: []};
        const elements = [];
        const elementIds = [];

        if (viewKey !== undefined) {
            view = structurizr.workspace.findViewByKey(viewKey);
            if (view !== undefined) {
                if (view.type === structurizr.constants.FILTERED_VIEW_TYPE) {
                    // find the base view instead
                    view = structurizr.workspace.findViewByKey(view.baseViewKey);
                }

                if (view.type === structurizr.constants.CUSTOM_VIEW_TYPE ||
                    view.type === structurizr.constants.SYSTEM_LANDSCAPE_VIEW_TYPE ||
                    view.type === structurizr.constants.SYSTEM_CONTEXT_VIEW_TYPE ||
                    view.type === structurizr.constants.CONTAINER_VIEW_TYPE ||
                    view.type === structurizr.constants.COMPONENT_VIEW_TYPE) {

                    view.elements.forEach(function(elementView) {
                        const element = structurizr.workspace.findElementById(elementView.id);
                        if (element) {
                            elements.push(element);
                            elementIds.push(element.id);
                        }
                    });
                }

                $('#graphTitle').text(Structurizr.workspace.getTitleForView(view));
            }
        }

        elements.sort(function(a, b) {
            return a.name.localeCompare(b.name);
        });

        elements.forEach(function(element) {
            var elementStyle = Structurizr.workspace.findElementStyle(element);
            graph.nodes.push({
                id: element.id,
                name: element.name,
                description: element.description,
                style: elementStyle,
                type: element.type,
                tags: element.tags,
                element: element
            });

            registerRelationship(element.id, element.id); // this prevents the nodes in the graph linking to themselves

            const elementType = structurizr.workspace.getTerminologyFor(element);
            const html = '<span class="label smaller" style="background: ' + elementStyle.background + '; color: ' + elementStyle.color + '"> ' + structurizr.util.escapeHtml(elementType) + '</span> ' + structurizr.util.escapeHtml(element.name);
            quickNavigation.addHandler(html, function() {
                highlightNode(element.id);
            });

            if (element.relationships) {
                element.relationships.forEach(function(relationship) {
                    if (elementIds.indexOf(relationship.sourceId) > -1 && elementIds.indexOf(relationship.destinationId) > -1) {
                        registerRelationship(relationship.sourceId, relationship.destinationId);

                        graph.links.push({
                            id: relationship.id,
                            source: relationship.sourceId,
                            target: relationship.destinationId,
                            type: 'Relationship',
                            description: relationship.description,
                            style: Structurizr.workspace.findRelationshipStyle(relationship),
                            relationship: relationship
                        });
                    }
                });
            }
        });
    }

    function renderGraph() {
        $("#exploreGraph").height(height);
        $("#exploreGraph").empty();

        if (view === undefined) {
            $("#exploreGraph").html('<div style="text-align: center; vertical-align: center; margin-top: 200px">No view with key <code>${view}</code>.</div>');
            return;
        }

        if (view.type === structurizr.constants.DYNAMIC_VIEW_TYPE) {
            $("#exploreGraph").html('<div style="text-align: center; vertical-align: center; margin-top: 200px">Graphs are not available for dynamic views.</div>');
            return;
        }

        if (view.type === structurizr.constants.DEPLOYMENT_VIEW_TYPE) {
            $("#exploreGraph").html('<div style="text-align: center; vertical-align: center; margin-top: 200px">Graphs are not available for deployment views.</div>');
            return;
        }

        var svg = d3.select("#exploreGraph").append("svg")
            .attr("width", width)
            .attr("height", height)
            .append("g");

        var endMarkerColors = [];

        graph.links.forEach(function(link) {
            var color = link.style.color.toLowerCase().replace('#', '');

            if (endMarkerColors.indexOf(color) === -1) {
                svg.append("svg:defs").selectAll("marker")
                    .data(["arrow" + color])
                    .enter().append("svg:marker")
                    .attr("id", String)
                    .attr("viewBox", "0 0 12 12")
                    .attr("refX", 32)
                    .attr("refY", 6)
                    .attr("markerWidth", 12)
                    .attr("markerHeight", 12)
                    .attr("orient", "auto")
                    .append("svg:path")
                    .attr("d", "M0,0 L12,6 L0,12 L0,0")
                    .attr("fill", link.style.color);

                endMarkerColors.push(color);
            }
        });

        distance = Math.min(width, height) / 3;

        simulation = d3.forceSimulation(graph.nodes)
            .force("link", d3.forceLink().distance(distance).id(function(d) { return d.id; }))
            .force("charge", d3.forceManyBody().strength(-distance))
            .force("center", d3.forceCenter(width / 2, height / 2))
            .stop();

        link = svg.selectAll(".link")
            .data(graph.links)
            .enter()
            .append("svg:path")
            .attr("class", "link")
            .attr("stroke", function(d) {
                return d.style.color;
            })
            .attr("id", function(d) {
                return 'relationship' + d.id;
            })
            .attr("marker-end", function(d) {
                return "url(#arrow" + d.style.color.toLowerCase().replace('#', '') + ")";
            })
            .on("mouseover", showTooltipForRelationship)
            .on("mousemove", moveTooltip)
            .on("mouseout", hideTooltip);

        link.filter(function(d) {
            return d.style.dashed === true;
        }).attr("stroke-dasharray", function(d) {
            return (d.style.dashed === true) ? '4' : '0';
        });

        node = svg.selectAll(".node")
            .data(graph.nodes)
            .enter()
            .append("g")
            .attr("class", "node")
            .call(d3.drag()
                .on("start", dragStarted)
                .on("drag", dragged)
                .on("end", dragEnded)
            )
            .on("click", click)
            .on("mouseover", showTooltipForElement)
            .on("mousemove", moveTooltip)
            .on("mouseout", hideTooltip);

        node.append("circle")
            .attr("id", function(d) {
                return 'element' + d.id;
            })
            .attr("r", function(d) {
                return nodeSizes[d.type];
            })
            .style("fill", function(d) {
                return d.style.background;
            })
            .style("stroke", function(d) {
                if (d.style.stroke) {
                    return d.style.stroke;
                } else {
                    return structurizr.util.shadeColor(d.style.background, -20);
                }
            });

        node.append("text")
            .attr("class", "nodeName")
            .attr("dy", function(d) {
                return 0;
            })
            .attr("dx", function(d) {
                return nodeSizes[d.type] + 5;
            })
            .text(function (d) { return d.name; });

        node.append("text")
            .attr("class", "nodeMetadata")
            .attr("dy", function(d) {
                return 10;
            })
            .attr("dx", function(d) {
                return nodeSizes[d.type] + 5;
            })
            .text(function (d) { return '[' + structurizr.workspace.getTerminologyFor(d.element) + ']'; });

        node.filter(function(d) {
            return d.style.icon !== undefined;
        }).append("image")
            .attr("width", function(d) {
                return nodeSizes[d.type];
            })
            .attr("height", function(d) {
                return nodeSizes[d.type];
            })
            .attr("x", function(d) {
                return -(nodeSizes[d.type]/2);
            })
            .attr("y", function(d) {
                return -(nodeSizes[d.type]/2);
            })
            .attr("href", function(d) {
                return d.style.icon;
            });

        simulation.nodes(graph.nodes).on("tick", ticked);

        simulation.force("link").links(graph.links);

        simulation.alphaTarget(0.3);

        for (var i = 0; i < 300; ++i) {
            simulation.tick();
        }

        simulation.alphaTarget(0).restart();

        function ticked() {
            link.attr("d", function(d) {
                var links = getLinks(d.source, d.target);
                if ((hasRelationship(d.source.id, d.target.id) && hasRelationship(d.target.id, d.source.id)) || links.length > 1) {
                    // bi-directional or multiple relationships
                    var dx = d.target.x - d.source.x,
                        dy = d.target.y - d.source.y,
                        dr = Math.sqrt(dx * dx + dy * dy) * 2;

                    var delta = (dr / (links.length + 1));
                    dr -= Math.sqrt(links.indexOf(d.relationship) + 1) * (delta * 1.25);

                    return "M " +
                        d.source.x + "," +
                        d.source.y +
                        " A " +
                        dr + "," + dr + " 0 0,1 " +
                        d.target.x + "," +
                        d.target.y;
                } else {
                    // uni-directional relationship
                    return "M" + d.source.x + "," + d.source.y + " " + "L" + d.target.x + "," + d.target.y;
                }

            });

            node.attr("transform", function(d) {
                return "translate(" + Math.max(margin, Math.min(width - margin, d.x)) + "," + Math.max(margin, Math.min(height - margin, d.y)) + ")";
            });
        }
    }

    function showTooltipForElement(event, d) {
        tooltip.showTooltipForElement(d.element, d.style, event.pageX, event.pageY, '');
    }

    function showTooltipForRelationship(event, d) {
        tooltip.showTooltipForRelationship(d.relationship, {}, d.style, event.pageX, event.pageY);
    }

    function moveTooltip(event) {
        tooltip.reposition(event.pageX, event.pageY);
    }

    function hideTooltip() {
        tooltip.hide();
    }

    function showConnectedNodes(nodeClicked, reset) {
        if (reset === true) {
            node.style("stroke-width", "2px");
            node.style("opacity", '1');
            link.style("opacity", '1');

            if (!nodeHighlighted) {
                // do nothing
            } else if (nodeHighlighted === nodeClicked) {
                // the same node has been clicked again, so return everything to it's normal state
                nodeHighlighted = undefined;

                return;
            }

            nodeHighlighted = nodeClicked;
        } else {
            nodeHighlighted = undefined;
        }

        $(nodeClicked).css("stroke-width", "4px");

        // reduce the opacity of all but the neighbouring nodes
        var d = d3.select(nodeClicked).node().__data__;

        node.style("opacity", function (o) {
            var currentOpacity = $('#element' + o.id).css('opacity');
            return hasRelationship(d.id, o.id) || hasRelationship(o.id, d.id) ? 1 : (reset === true ? 0.1 : currentOpacity);
        });

        link.style("opacity", function (o) {
            var currentOpacity = $('#relationship' + o.id).css('opacity');
            return (d.id === o.source.id || d.id === o.target.id) ? 1 : (reset === true ? 0.1 : currentOpacity);
        });
    }

    function click() {
        showConnectedNodes(this, true);
    }

    function setWidthAndHeight() {
        var navHeight = 0;

        <c:if test="${empty iframe}">
        if (Structurizr.isFullScreen()) {
            navHeight = 0;
        } else {
            navHeight = $('#topNavigation').outerHeight();
        }
        </c:if>

        width = window.innerWidth - margin;
        height = window.innerHeight - navHeight - margin;
        distance = Math.min(width, height) / 4;
    }

    function addEventHandlers() {
        $(document).keypress(function(e) {
            const plus = 43;
            const equals = 61;
            const minus = 45;

            var distanceDelta = 50;

            if (e.which === plus || e.which === equals) {
                // increase the distance between nodes
                distance = Math.min(distance + distanceDelta, Math.min(width, height)/2);
                simulation.force("link").distance(distance);
                simulation.alpha(0.3).restart();
            } else if (e.which === minus) {
                // decrease the distance between nodes
                distance = Math.max(distance - distanceDelta, 100);
                simulation.force("link").distance(distance);
                simulation.alpha(0.3).restart();
            }
        });

        window.addEventListener("resize", function() {
            setWidthAndHeight();

            $("#exploreGraph").height(height);
            $("#exploreGraph svg").attr('width', width);
            $("#exploreGraph svg").attr('height', height);

            simulation.force("center")
                .x(width / 2)
                .y(height / 2);

            simulation.force("link").distance(distance);
            simulation.alpha(0.3).restart();
        }, false);
    }

    function getLinks(source, target) {
        const links = [];
        for (var i = 0; i < graph.links.length; ++i){
            if (graph.links[i].source.id === source.id && graph.links[i].target.id === target.id) {
                links.push(graph.links[i].relationship);
            }
        }
        return links;
    }

    function dragStarted(event, d) {
        if (!event.active) {
            simulation.alphaTarget(0.3).restart();
        }
        d.fx = d.x;
        d.fy = d.y;
    }

    function dragged(event, d) {
        d.fx = event.x;
        d.fy = event.y;
    }

    function dragEnded(event, d) {
        if (!event.active) {
            simulation.alphaTarget(0);
        }
        d.fx = null;
        d.fy = null;
    }

    function highlightNode(elementId) {
        node.filter(function(d) {
            return d.element.id === elementId;
        }).nodes().forEach(function (d, i) {
            showConnectedNodes(d, true);
        });
    }

    function hasRelationship(sourceId, destinationId) {
        const key = sourceId + "," + destinationId;
        return relationshipsBySourceAndDestination.indexOf(key) > -1;
    }

    function registerRelationship(sourceId, destinationId) {
        const key = sourceId + "," + destinationId;
        relationshipsBySourceAndDestination.push(key);
    }

    function isRendered() {
        return true;
    }

    function isExportable() {
        return false;
    }

    $('#embeddedControls').hover(
        function() {
            $('#embeddedControls').css('opacity', '1.0');
        },
        function() {
            $('#embeddedControls').css('opacity', '0.1');
        }
    );

    function openGraphInNewWindow() {
        window.open('${urlPrefix}/explore/graph?view=' + encodeURIComponent(viewKey));
    }
</script>

<c:choose>
    <c:when test="${loadWorkspaceFromParent eq true}">
        <script nonce="${scriptNonce}">
            loadWorkspaceFromParent();
        </script>
    </c:when>
    <c:when test="${not empty workspaceAsJson}">
        <%@ include file="/WEB-INF/fragments/workspace/load-via-inline.jspf" %>
    </c:when>
    <c:otherwise>
        <%@ include file="/WEB-INF/fragments/workspace/load-via-api.jspf" %>
    </c:otherwise>
</c:choose>