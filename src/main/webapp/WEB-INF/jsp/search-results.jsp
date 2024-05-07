<div class="section">
    <div class="container">
        <h1><c:out value="${query}" /></h1>

        <br />

        <c:choose>
            <c:when test="${not empty results}">
                <table class="table table-striped small">

                    <c:forEach var="result" items="${results}">
                        <tr style="height: 80px">
                            <td>
                                <c:choose>
                                    <c:when test="${result.type eq 'documentation'}">
                                        Documentation
                                    </c:when>
                                    <c:when test="${result.type eq 'decision'}">
                                        Decision
                                    </c:when>
                                    <c:when test="${result.type eq 'diagram'}">
                                        Diagram
                                    </c:when>
                                    <c:otherwise>
                                        Workspace
                                    </c:otherwise>
                                </c:choose>
                            </td>
                            <td>
                                <a href="${result.url}"><c:out value="${result.name}" escapeXml="true" /></a>
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