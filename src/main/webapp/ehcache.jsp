<%--
  Created by IntelliJ IDEA.
  User: anna.manukyan
  Date: 6/29/12
  Time: 11:17 AM
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page language="java" import="org.hibernate.*,org.hibernate.stat.*"%>
<%@ page import="iBook.utils.Utils" %>
<html>
<head>
    <title></title>
</head>
<body>

<%
String entityCacheRegion="entity";

SessionFactory sessionFactory = Utils.getInstance().getSessionFactory();
Statistics statistics = sessionFactory.getStatistics();
SecondLevelCacheStatistics secondLevelCacheStatistics = statistics.getSecondLevelCacheStatistics(entityCacheRegion);

%>
<h2>Query Cache Statistics</h2>
<b>Query hit count:</b> <%=statistics.getQueryCacheHitCount()%><br/>
<b>Query miss count:</b> <%=statistics.getQueryCacheMissCount()%><br/>
<b>Query put count:</b> <%=statistics.getQueryCachePutCount()%><br/>
<h2>Entity Cache Statistics</h2>
<b>Entity hit count:</b> <%=secondLevelCacheStatistics.getHitCount()%><br/>
<b>Entity miss count:</b> <%=secondLevelCacheStatistics.getMissCount()%><br/>
<b>Entity put count:</b> <%=secondLevelCacheStatistics.getPutCount()%><br/>
</body>
</html>