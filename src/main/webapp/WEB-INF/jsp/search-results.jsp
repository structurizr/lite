<div class="section">
    <div class="container">
        <h1>Search</h1>

        <c:if test="${not empty query}">
            <p class="centered" style="margin-top: 20px">
                Showing results for <code><c:out value="${query}" /></code>
            </p>
        </c:if>
    </div>
</div>

<div class="section">
    <div class="container">
        <c:choose>
            <c:when test="${not empty results}">
                <table class="table table-striped small">

                    <c:forEach var="result" items="${results}">
                        <tr>
                            <td width="90px">
                                <div>
                                    <img src="${urlPrefix}/images/thumbnail.png" width="70px" alt="<c:out value='${result.workspace.name}' escapeXml='true' />" class="thumbnail" onerror="this.onerror = null; this.src='/static/img/thumbnail-not-available.png';" />
                                </div>
                            </td>
                            <td>
                                <c:choose>
                                    <c:when test="${result.type eq 'workspace'}">
                                        <span class="label label-default"><img src="${structurizrConfiguration.cdnUrl}/glyphicons/glyphicons-basic-336-folder.svg" class="glyphicon-xs glyphicon-white" /></span>
                                    </c:when>
                                    <c:when test="${result.type eq 'documentation'}">
                                        <span class="label label-default"><img src="${structurizrConfiguration.cdnUrl}/glyphicons/glyphicons-basic-588-book-open-text.svg" class="glyphicon-xs glyphicon-white" /></span>
                                    </c:when>
                                    <c:when test="${result.type eq 'decision'}">
                                        <span class="label label-default"><img src="${structurizrConfiguration.cdnUrl}/glyphicons/glyphicons-basic-159-thumbnails-list.svg" class="glyphicon-xs glyphicon-white" /></span>
                                    </c:when>
                                    <c:when test="${result.type eq 'diagram'}">
                                        <span class="label label-default"><img src="${structurizrConfiguration.cdnUrl}/glyphicons/glyphicons-basic-95-vector-path.svg" class="glyphicon-xs glyphicon-white" /></span>
                                    </c:when>
                                </c:choose>
                                <a href="${urlPrefix}${result.url}"><c:out value="${result.name}" escapeXml="true" /></a>
                                <div class="smaller" style="margin-top: 5px">
                                    <c:out value="${result.description}" escapeXml="true" />
                                </div>
                            </td>
                        </tr>
                    </c:forEach>

                </table>
            </c:when>
            <c:otherwise>
                <p>
                    No results found.
                </p>
            </c:otherwise>
        </c:choose>
    </div>
</div>